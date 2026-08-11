package com.mizbamd.zikra.util

import com.mizbamd.zikra.data.local.FrameEntity
import com.mizbamd.zikra.data.local.GUEST_USER_ID

data class DefaultFrame(
    val key: String,
    val arabic: String,
    val transliteration: String,
    val target: Int,
)

object Defaults {
    /** Seeded signed-in dhikr. Must stay under [com.mizbamd.zikra.entitlements.FrameLimitPolicy.DEFAULT_MAX_FRAMES]. */
    val signedIn = listOf(
        DefaultFrame("subhan", "سبحان الله", "SubhanAllah", 33),
        DefaultFrame("hamd", "الحمد لله", "Alhamdulillah", 33),
        DefaultFrame("akbar", "الله أكبر", "Allahu Akbar", 34),
        DefaultFrame("astaghfir", "أستغفر الله", "Astaghfirullah", 100),
    )

    val guest = signedIn.first()

    fun frameId(userId: String, key: String) = "seed:$userId:$key"

    fun seedGuest(): List<FrameEntity> = listOf(toEntity(guest, GUEST_USER_ID, 0))

    fun seedSignedIn(userId: String): List<FrameEntity> =
        signedIn.mapIndexed { index, frame -> toEntity(frame, userId, index) }

    fun toEntity(frame: DefaultFrame, userId: String, order: Int): FrameEntity {
        val now = ZikraTime.nowIso()
        return FrameEntity(
            id = frameId(userId, frame.key),
            userId = userId,
            arabic = frame.arabic,
            transliteration = frame.transliteration,
            target = frame.target,
            lifetimeCount = 0,
            sortOrder = order,
            createdAt = now,
            updatedAt = now,
            deleted = false,
            dirty = true,
        )
    }
}
