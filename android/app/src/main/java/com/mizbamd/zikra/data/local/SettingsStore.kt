package com.mizbamd.zikra.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("zikra_settings")

enum class SessionMode { WELCOME, GUEST, SIGNED_IN }
enum class ResetAt { MIDNIGHT, FAJR }

data class Settings(
    val mode: SessionMode = SessionMode.WELCOME,
    val userId: String = GUEST_USER_ID,
    val email: String = "",
    val token: String = "",
    val haptics: Boolean = true,
    val volumeUpIncrement: Boolean = false,
    val resetAt: ResetAt = ResetAt.MIDNIGHT,
    val language: String = "en",
    val locationEnabled: Boolean = true,
    val calendarMethod: String = "umm_al_qura",
    val lat: Double? = null,
    val lon: Double? = null,
    val hasRealLocation: Boolean = false,
) {
    val isSignedIn: Boolean get() = mode == SessionMode.SIGNED_IN && token.isNotBlank()
    val isGuest: Boolean get() = mode == SessionMode.GUEST
}

const val GUEST_USER_ID = "guest"

class SettingsStore(private val context: Context) {
    val settings: Flow<Settings> = context.dataStore.data.map { it.toSettings() }

    suspend fun becomeGuest() {
        context.dataStore.edit {
            it[Keys.mode] = SessionMode.GUEST.name
            it[Keys.userId] = GUEST_USER_ID
            it[Keys.email] = ""
            it[Keys.token] = ""
        }
    }

    suspend fun signIn(userId: String, email: String, token: String) {
        context.dataStore.edit {
            it[Keys.mode] = SessionMode.SIGNED_IN.name
            it[Keys.userId] = userId
            it[Keys.email] = email
            it[Keys.token] = token
        }
    }

    suspend fun signOut() {
        context.dataStore.edit {
            it[Keys.mode] = SessionMode.WELCOME.name
            it[Keys.userId] = GUEST_USER_ID
            it[Keys.email] = ""
            it[Keys.token] = ""
        }
    }

    /** Clears all preferences after account deletion. */
    suspend fun wipe() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun setHaptics(value: Boolean) = context.dataStore.edit { it[Keys.haptics] = value }
    suspend fun setVolumeUp(value: Boolean) = context.dataStore.edit { it[Keys.volumeUp] = value }
    suspend fun setResetAt(value: ResetAt) = context.dataStore.edit { it[Keys.resetAt] = value.name }
    suspend fun setLanguage(value: String) = context.dataStore.edit { it[Keys.lang] = value }
    suspend fun setLocationEnabled(value: Boolean) = context.dataStore.edit { it[Keys.location] = value }

    suspend fun setLocation(lat: Double, lon: Double, real: Boolean) {
        context.dataStore.edit {
            it[Keys.lat] = lat
            it[Keys.lon] = lon
            it[Keys.hasRealLocation] = real
        }
    }

    private object Keys {
        val mode: Key<String> = stringPreferencesKey("mode")
        val userId: Key<String> = stringPreferencesKey("userId")
        val email: Key<String> = stringPreferencesKey("email")
        val token: Key<String> = stringPreferencesKey("token")
        val haptics: Key<Boolean> = booleanPreferencesKey("haptics")
        val volumeUp: Key<Boolean> = booleanPreferencesKey("volumeUp")
        val resetAt: Key<String> = stringPreferencesKey("resetAt")
        val lang: Key<String> = stringPreferencesKey("language")
        val location: Key<Boolean> = booleanPreferencesKey("location")
        val calendar: Key<String> = stringPreferencesKey("calendar")
        val lat: Key<Double> = doublePreferencesKey("lat")
        val lon: Key<Double> = doublePreferencesKey("lon")
        val hasRealLocation: Key<Boolean> = booleanPreferencesKey("hasRealLocation")
    }

    private fun Preferences.str(key: Key<String>, default: String): String = this[key] ?: default
    private fun Preferences.bool(key: Key<Boolean>, default: Boolean): Boolean = this[key] ?: default

    private fun Preferences.toSettings() = Settings(
        mode = runCatching { SessionMode.valueOf(str(Keys.mode, SessionMode.WELCOME.name)) }
            .getOrDefault(SessionMode.WELCOME),
        userId = str(Keys.userId, GUEST_USER_ID),
        email = str(Keys.email, ""),
        token = str(Keys.token, ""),
        haptics = bool(Keys.haptics, true),
        volumeUpIncrement = bool(Keys.volumeUp, false),
        resetAt = runCatching { ResetAt.valueOf(str(Keys.resetAt, ResetAt.MIDNIGHT.name)) }
            .getOrDefault(ResetAt.MIDNIGHT),
        language = str(Keys.lang, "en"),
        locationEnabled = bool(Keys.location, true),
        calendarMethod = str(Keys.calendar, "umm_al_qura"),
        lat = this[Keys.lat],
        lon = this[Keys.lon],
        hasRealLocation = bool(Keys.hasRealLocation, false),
    )
}
