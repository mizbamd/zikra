package com.mizbamd.zikra.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mizbamd.zikra.data.local.DailyCountEntity
import com.mizbamd.zikra.data.local.FrameEntity
import com.mizbamd.zikra.data.local.GUEST_USER_ID
import com.mizbamd.zikra.data.local.ResetAt
import com.mizbamd.zikra.data.local.SessionMode
import com.mizbamd.zikra.data.local.Settings
import com.mizbamd.zikra.data.local.SettingsStore
import com.mizbamd.zikra.data.repo.AuthRepository
import com.mizbamd.zikra.data.repo.FrameRepository
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.entitlements.FrameLimitPolicy
import com.mizbamd.zikra.notify.DailyReminder
import com.mizbamd.zikra.util.CountTick
import com.mizbamd.zikra.util.SAMPLE_LAT
import com.mizbamd.zikra.util.SAMPLE_LON
import com.mizbamd.zikra.util.VolumeUpBus
import com.mizbamd.zikra.util.ZikraTime
import com.mizbamd.zikra.util.tapHaptic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val settings: Settings = Settings(),
    val frames: List<FrameToday> = emptyList(),
    val history: List<DailyCountEntity> = emptyList(),
    val authError: String? = null,
    val authBusy: Boolean = false,
    val doneFrameId: String? = null,
    val maxFrames: Int = FrameLimitPolicy.DEFAULT_MAX_FRAMES,
    val canAddFrame: Boolean = true,
    val streakDays: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ZikraViewModel(
    app: Application,
    private val settingsStore: SettingsStore,
    private val frames: FrameRepository,
    private val auth: AuthRepository,
) : AndroidViewModel(app) {

    private val authError = MutableStateFlow<String?>(null)
    private val authBusy = MutableStateFlow(false)
    private val doneFrameId = MutableStateFlow<String?>(null)

    private val todayFrames = settingsStore.settings.flatMapLatest { s ->
        when (s.mode) {
            SessionMode.GUEST -> frames.observeToday(GUEST_USER_ID)
            SessionMode.SIGNED_IN -> frames.observeToday(s.userId)
            SessionMode.WELCOME -> flowOf(emptyList())
        }
    }
    private val historyFlow = settingsStore.settings.flatMapLatest { s ->
        when (s.mode) {
            SessionMode.GUEST -> frames.observeHistory(GUEST_USER_ID)
            SessionMode.SIGNED_IN -> frames.observeHistory(s.userId)
            SessionMode.WELCOME -> flowOf(emptyList())
        }
    }
    private val authBits = combine(authError, authBusy, doneFrameId) { err, busy, done ->
        Triple(err, busy, done)
    }

    val state: StateFlow<UiState> = combine(
        settingsStore.settings,
        todayFrames,
        historyFlow,
        authBits,
    ) { settings: Settings, today: List<FrameToday>, history: List<DailyCountEntity>, bits: Triple<String?, Boolean, String?> ->
        val signedIn = settings.mode == SessionMode.SIGNED_IN
        val maxFrames = FrameLimitPolicy.maxFramesFor(signedIn)
        val todayKey = ZikraTime.todayKey(
            settings.resetAt,
            settings.lat ?: SAMPLE_LAT,
            settings.lon ?: SAMPLE_LON,
        )
        UiState(
            settings = settings,
            frames = today,
            history = history,
            authError = bits.first,
            authBusy = bits.second,
            doneFrameId = bits.third,
            maxFrames = maxFrames,
            canAddFrame = FrameLimitPolicy.canAdd(today.size, signedIn),
            streakDays = settings.streakToShow(
                userId = settings.userId,
                today = todayKey,
                onlyIfCountedToday = settings.mode == SessionMode.GUEST,
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        viewModelScope.launch {
            val first = settingsStore.settings.first()
            if (first.lat == null) settingsStore.setLocation(SAMPLE_LAT, SAMPLE_LON, false)
        }
        viewModelScope.launch {
            settingsStore.settings.collect { s ->
                if (s.mode == SessionMode.GUEST) frames.ensureSeeded(GUEST_USER_ID, false)
                if (s.mode == SessionMode.SIGNED_IN) {
                    frames.ensureSeeded(s.userId, true)
                    frames.syncQuietly()
                }
            }
        }
        viewModelScope.launch {
            VolumeUpBus.ticks.collect { frameId ->
                if (state.value.settings.volumeUpIncrement) increment(frameId)
            }
        }
        viewModelScope.launch { frames.pruneOldCounts() }
        viewModelScope.launch {
            val s = settingsStore.settings.first()
            DailyReminder.apply(
                getApplication(),
                s.reminderEnabled,
                s.reminderHour,
                s.reminderMinute,
            )
        }
    }

    fun increment(frameId: String) {
        viewModelScope.launch {
            val s = state.value.settings
            if (s.haptics) getApplication<Application>().tapHaptic()
            if (s.tickSound) CountTick.play()
            val result = frames.increment(s.userId, frameId)
            if (result?.justHitTarget == true) doneFrameId.value = frameId
            if (result != null && result.todayCount == 1) {
                val today = ZikraTime.todayKey(
                    s.resetAt,
                    s.lat ?: SAMPLE_LAT,
                    s.lon ?: SAMPLE_LON,
                )
                settingsStore.recordDailyCount(s.userId, today)
            }
        }
    }

    fun clearDone() {
        doneFrameId.value = null
    }

    fun undo(frameId: String) {
        viewModelScope.launch { frames.undo(state.value.settings.userId, frameId) }
    }

    fun resetToday(frameId: String) {
        viewModelScope.launch { frames.resetToday(state.value.settings.userId, frameId) }
    }

    fun resetLifetime(frameId: String) {
        viewModelScope.launch { frames.resetLifetime(state.value.settings.userId, frameId) }
    }

    fun continueGuest() {
        viewModelScope.launch { auth.continueGuest() }
    }

    fun register(email: String, password: String) = authenticate {
        auth.register(email, password)
    }

    fun login(email: String, password: String) = authenticate {
        auth.login(email, password)
    }

    fun signOut() {
        viewModelScope.launch { auth.signOut() }
    }

    fun deleteAccount() = authenticate {
        DailyReminder.cancel(getApplication())
        auth.deleteAccount()
    }

    fun saveFrame(id: String?, arabic: String, transliteration: String, target: Int?) {
        viewModelScope.launch {
            frames.saveFrame(state.value.settings.userId, id, arabic, transliteration, target)
        }
    }

    fun deleteFrame(id: String) {
        viewModelScope.launch { frames.deleteFrame(id) }
    }

    suspend fun frame(id: String): FrameEntity? = frames.getFrame(id)

    fun setHaptics(v: Boolean) = viewModelScope.launch { settingsStore.setHaptics(v) }
    fun setVolumeUp(v: Boolean) = viewModelScope.launch { settingsStore.setVolumeUp(v) }
    fun setTickSound(v: Boolean) = viewModelScope.launch { settingsStore.setTickSound(v) }
    fun setResetAt(v: ResetAt) = viewModelScope.launch { settingsStore.setResetAt(v) }
    fun setLocationEnabled(v: Boolean) = viewModelScope.launch { settingsStore.setLocationEnabled(v) }

    fun setReminderEnabled(v: Boolean) = viewModelScope.launch {
        settingsStore.setReminderEnabled(v)
        val s = settingsStore.settings.first()
        DailyReminder.apply(getApplication(), v, s.reminderHour, s.reminderMinute)
    }

    fun setReminderTime(hour: Int, minute: Int) = viewModelScope.launch {
        settingsStore.setReminderTime(hour, minute)
        val s = settingsStore.settings.first()
        DailyReminder.apply(getApplication(), s.reminderEnabled, hour, minute)
    }

    fun setLanguage(tag: String) {
        viewModelScope.launch {
            settingsStore.setLanguage(tag)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }

    fun setCoordinates(lat: Double, lon: Double, real: Boolean) {
        viewModelScope.launch { settingsStore.setLocation(lat, lon, real) }
    }

    fun useSampleLocation() {
        viewModelScope.launch { settingsStore.setLocation(SAMPLE_LAT, SAMPLE_LON, false) }
    }

    fun displayDates() = ZikraTime.displayDates(
        lat = state.value.settings.lat ?: SAMPLE_LAT,
        lon = state.value.settings.lon ?: SAMPLE_LON,
        hasRealLocation = state.value.settings.hasRealLocation,
    )

    private fun authenticate(block: suspend () -> Unit) {
        viewModelScope.launch {
            authBusy.value = true
            authError.value = null
            runCatching { block() }
                .onFailure { authError.value = it.message }
            authBusy.value = false
        }
    }

    fun clearAuthError() {
        authError.value = null
    }
}
