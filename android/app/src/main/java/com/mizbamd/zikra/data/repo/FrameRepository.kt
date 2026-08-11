package com.mizbamd.zikra.data.repo

import com.mizbamd.zikra.data.local.DailyCountDao
import com.mizbamd.zikra.data.local.DailyCountEntity
import com.mizbamd.zikra.data.local.FrameDao
import com.mizbamd.zikra.data.local.FrameEntity
import com.mizbamd.zikra.data.local.GUEST_USER_ID
import com.mizbamd.zikra.data.local.HistoryRetention
import com.mizbamd.zikra.data.local.ResetAt
import com.mizbamd.zikra.data.local.Settings
import com.mizbamd.zikra.data.local.SettingsStore
import com.mizbamd.zikra.data.remote.DailyCountDto
import com.mizbamd.zikra.data.remote.FrameDto
import com.mizbamd.zikra.data.remote.SyncPushRequest
import com.mizbamd.zikra.data.remote.ZikraApi
import com.mizbamd.zikra.entitlements.FrameLimitPolicy
import com.mizbamd.zikra.util.Defaults
import com.mizbamd.zikra.util.DhikrLexicon
import com.mizbamd.zikra.util.SAMPLE_LAT
import com.mizbamd.zikra.util.SAMPLE_LON
import com.mizbamd.zikra.util.ZikraTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class FrameToday(
    val frame: FrameEntity,
    val todayCount: Int,
    val todayId: String?,
) {
    val target: Int? get() = frame.target
    val justHitTarget: Boolean get() = target != null && todayCount == target
}

class FrameRepository(
    private val frames: FrameDao,
    private val counts: DailyCountDao,
    private val settings: SettingsStore,
    private val api: ZikraApi,
) {
    private val seedLock = Mutex()

    fun observeToday(userId: String): Flow<List<FrameToday>> =
        combine(
            frames.observeActive(userId),
            settings.settings,
            counts.observeAll(userId),
        ) { list: List<FrameEntity>, s: Settings, allCounts: List<DailyCountEntity> ->
            val date = todayKey(s.resetAt, s.lat, s.lon)
            list.map { frame ->
                val row = allCounts.firstOrNull { it.frameId == frame.id && it.date == date }
                FrameToday(frame, row?.count ?: 0, row?.id)
            }
        }

    fun observeHistory(userId: String): Flow<List<DailyCountEntity>> = counts.observeAll(userId)

    suspend fun ensureSeeded(userId: String, signedIn: Boolean) {
        seedLock.withLock {
            if (!signedIn) {
                if (frames.countActive(userId) == 0) frames.upsertAll(Defaults.seedGuest())
                return
            }
            reconcileSignedInDefaults(userId)
        }
    }

    /**
     * Keep exactly the four default dhikr for a signed-in user.
     * Dedupes races from double-seed, and inserts any missing (e.g. Astaghfirullah).
     */
    private suspend fun reconcileSignedInDefaults(userId: String) {
        val existing = frames.listActive(userId)
        val now = ZikraTime.nowIso()
        val claimed = mutableSetOf<String>()
        Defaults.signedIn.forEachIndexed { index, def ->
            val matches = existing.filter { it.arabic.trim() == def.arabic && it.id !in claimed }
            if (matches.isEmpty()) {
                val stable = existing.find { it.id == Defaults.frameId(userId, def.key) }
                if (stable == null &&
                    FrameLimitPolicy.canAdd(frames.countActive(userId), signedIn = true)
                ) {
                    frames.upsert(Defaults.toEntity(def, userId, index))
                }
                return@forEachIndexed
            }
            val keep = matches.maxWith(
                compareBy<FrameEntity> { it.lifetimeCount }.thenByDescending { it.createdAt },
            )
            claimed += keep.id
            if (keep.sortOrder != index || keep.target != def.target) {
                frames.update(
                    keep.copy(sortOrder = index, target = def.target, updatedAt = now, dirty = true),
                )
            }
            matches.filter { it.id != keep.id }.forEach { extra ->
                claimed += extra.id
                frames.update(extra.copy(deleted = true, updatedAt = now, dirty = true))
            }
        }
    }

    suspend fun increment(userId: String, frameId: String): FrameToday? {
        val s = settings.settings.first()
        val date = todayKey(s.resetAt, s.lat, s.lon)
        val now = ZikraTime.nowIso()
        val frame = frames.get(frameId) ?: return null
        val existing = counts.get(userId, frameId, date)
        val newCount = (existing?.count ?: 0) + 1
        val row = DailyCountEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            userId = userId,
            frameId = frameId,
            date = date,
            count = newCount,
            updatedAt = now,
            dirty = true,
        )
        counts.upsert(row)
        frames.update(
            frame.copy(
                lifetimeCount = frame.lifetimeCount + 1,
                updatedAt = now,
                dirty = true,
            ),
        )
        syncQuietly()
        return FrameToday(frame.copy(lifetimeCount = frame.lifetimeCount + 1), newCount, row.id)
    }

    suspend fun undo(userId: String, frameId: String) {
        val s = settings.settings.first()
        val date = todayKey(s.resetAt, s.lat, s.lon)
        val now = ZikraTime.nowIso()
        val frame = frames.get(frameId) ?: return
        val existing = counts.get(userId, frameId, date) ?: return
        if (existing.count <= 0) return
        counts.upsert(existing.copy(count = existing.count - 1, updatedAt = now, dirty = true))
        frames.update(
            frame.copy(
                lifetimeCount = (frame.lifetimeCount - 1).coerceAtLeast(0),
                updatedAt = now,
                dirty = true,
            ),
        )
        syncQuietly()
    }

    suspend fun resetToday(userId: String, frameId: String) {
        val s = settings.settings.first()
        val date = todayKey(s.resetAt, s.lat, s.lon)
        val now = ZikraTime.nowIso()
        val frame = frames.get(frameId) ?: return
        val existing = counts.get(userId, frameId, date) ?: return
        counts.upsert(existing.copy(count = 0, updatedAt = now, dirty = true))
        frames.update(
            frame.copy(
                lifetimeCount = (frame.lifetimeCount - existing.count).coerceAtLeast(0),
                updatedAt = now,
                dirty = true,
            ),
        )
        syncQuietly()
    }

    suspend fun saveFrame(
        userId: String,
        id: String?,
        arabic: String,
        transliteration: String,
        target: Int?,
    ) {
        val now = ZikraTime.nowIso()
        val existing = id?.let { frames.get(it) }
        val unchanged = existing != null &&
            existing.arabic == arabic &&
            existing.transliteration == transliteration
        val catalog = DhikrLexicon.matchPair(arabic, transliteration)
        if (!unchanged && catalog == null) return
        val persistArabic = if (unchanged) arabic else catalog!!.arabic
        val persistLatin = if (unchanged) transliteration else catalog!!.latin
        if (id == null) {
            val signedIn = userId != GUEST_USER_ID
            if (!FrameLimitPolicy.canAdd(frames.countActive(userId), signedIn)) return
            val order = frames.listActive(userId).size
            frames.upsert(
                FrameEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    arabic = persistArabic,
                    transliteration = persistLatin,
                    target = target,
                    lifetimeCount = 0,
                    sortOrder = order,
                    createdAt = now,
                    updatedAt = now,
                    deleted = false,
                    dirty = true,
                ),
            )
        } else {
            val row = existing ?: return
            frames.update(
                row.copy(
                    arabic = persistArabic,
                    transliteration = persistLatin,
                    target = target,
                    updatedAt = now,
                    dirty = true,
                ),
            )
        }
        syncQuietly()
    }

    suspend fun deleteFrame(frameId: String) {
        val existing = frames.get(frameId) ?: return
        frames.update(
            existing.copy(
                deleted = true,
                updatedAt = ZikraTime.nowIso(),
                dirty = true,
            ),
        )
        syncQuietly()
    }

    suspend fun getFrame(id: String): FrameEntity? = frames.get(id)

    suspend fun wipeLocalUser(userId: String) {
        counts.deleteForUser(userId)
        frames.deleteForUser(userId)
    }

    suspend fun pruneOldCounts() {
        counts.deleteOlderThan(HistoryRetention.cutoffDate())
    }

    suspend fun syncQuietly() {
        runCatching { sync() }
    }

    suspend fun sync() {
        pruneOldCounts()
        val s = settings.settings.first()
        if (!s.isSignedIn) return
        val pull = api.pull(s.token)
        mergeRemote(s.userId, pull.frames, pull.dailyCounts)
        val dirtyFrames = frames.dirty(s.userId)
        val dirtyCounts = counts.dirty(s.userId)
        if (dirtyFrames.isEmpty() && dirtyCounts.isEmpty()) return
        val pushed = api.push(
            s.token,
            SyncPushRequest(
                frames = dirtyFrames.map { it.toDto() },
                dailyCounts = dirtyCounts.map { it.toDto() },
            ),
        )
        mergeRemote(s.userId, pushed.frames, pushed.dailyCounts)
        dirtyFrames.forEach { frames.update(it.copy(dirty = false)) }
        dirtyCounts.forEach { counts.upsert(it.copy(dirty = false)) }
        pruneOldCounts()
    }

    suspend fun onSignedIn(userId: String) {
        ensureSeeded(userId, signedIn = true)
        runCatching {
            val s = settings.settings.first()
            val pull = api.pull(s.token)
            if (pull.frames.none { !it.deleted }) {
                sync()
            } else {
                mergeRemote(userId, pull.frames, pull.dailyCounts)
            }
        }
        ensureSeeded(userId, signedIn = true)
        syncQuietly()
    }

    private suspend fun mergeRemote(
        userId: String,
        remoteFrames: List<FrameDto>,
        remoteCounts: List<DailyCountDto>,
    ) {
        remoteFrames.forEach { dto ->
            val local = frames.get(dto.id)
            if (local == null || dto.updatedAt >= local.updatedAt) {
                frames.upsert(
                    FrameEntity(
                        id = dto.id,
                        userId = userId,
                        arabic = dto.arabic,
                        transliteration = dto.transliteration,
                        target = dto.target,
                        lifetimeCount = dto.lifetimeCount,
                        sortOrder = dto.sortOrder,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        deleted = dto.deleted,
                        dirty = false,
                    ),
                )
            }
        }
        remoteCounts.forEach { dto ->
            val local = counts.get(userId, dto.frameId, dto.date)
            if (local == null || dto.updatedAt >= local.updatedAt) {
                counts.upsert(
                    DailyCountEntity(
                        id = dto.id,
                        userId = userId,
                        frameId = dto.frameId,
                        date = dto.date,
                        count = dto.count,
                        updatedAt = dto.updatedAt,
                        dirty = false,
                    ),
                )
            }
        }
    }

    private fun todayKey(resetAt: ResetAt, lat: Double?, lon: Double?): String =
        ZikraTime.todayKey(resetAt, lat ?: SAMPLE_LAT, lon ?: SAMPLE_LON)

    private fun FrameEntity.toDto() = FrameDto(
        id, arabic, transliteration, target, lifetimeCount, sortOrder, createdAt, updatedAt, deleted,
    )

    private fun DailyCountEntity.toDto() = DailyCountDto(id, frameId, date, count, updatedAt)
}

fun FrameRepository.guestToday(): Flow<List<FrameToday>> = observeToday(GUEST_USER_ID)
