package com.mizbamd.zikra.data.repo

import com.mizbamd.zikra.data.local.SettingsStore
import com.mizbamd.zikra.data.remote.ApiException
import com.mizbamd.zikra.data.remote.ZikraApi
import kotlinx.coroutines.flow.first

sealed class OtpRequestOutcome {
    data object Sent : OtpRequestOutcome()
    data class PasswordFallback(val message: String) : OtpRequestOutcome()
}

class AuthRepository(
    private val api: ZikraApi,
    private val settings: SettingsStore,
    private val frames: FrameRepository,
) {
    suspend fun requestOtp(email: String): OtpRequestOutcome {
        runCatching { api.requestOtp(email.trim()) }
            .onSuccess { return OtpRequestOutcome.Sent }
            .onFailure { err ->
                if (err is ApiException && (err.status == 404 || err.status == 503)) {
                    val wrapped = wrap(err) as ApiException
                    return OtpRequestOutcome.PasswordFallback(
                        wrapped.message ?: HUMANIZED_OTP_UNAVAILABLE,
                    )
                }
                throw wrap(err)
            }
        error("unreachable")
    }

    suspend fun verifyOtp(email: String, code: String) {
        val res = runCatching { api.verifyOtp(email.trim(), code.trim()) }
            .getOrElse { throw wrap(it) }
        settings.signIn(res.userId, res.email, res.token)
        frames.onSignedIn(res.userId)
    }

    suspend fun register(email: String, password: String) {
        val trimmed = email.trim()
        val res = runCatching { api.register(trimmed, password) }.getOrElse { err ->
            if (err is ApiException && err.message?.contains("already exists", ignoreCase = true) == true) {
                return login(trimmed, password)
            }
            throw wrap(err)
        }
        settings.signIn(res.userId, res.email, res.token)
        frames.onSignedIn(res.userId)
    }

    suspend fun login(email: String, password: String) {
        val res = runCatching { api.login(email.trim(), password) }
            .getOrElse { throw wrap(it) }
        settings.signIn(res.userId, res.email, res.token)
        frames.onSignedIn(res.userId)
    }

    suspend fun continueGuest() {
        settings.becomeGuest()
        frames.ensureSeeded(com.mizbamd.zikra.data.local.GUEST_USER_ID, signedIn = false)
    }

    suspend fun signOut() {
        settings.signOut()
    }

    suspend fun deleteAccount() {
        val s = settings.settings.first()
        if (!s.isSignedIn) return
        runCatching { api.deleteAccount(s.token) }
            .getOrElse { throw wrap(it) }
        frames.wipeLocalUser(s.userId)
        settings.wipe()
    }

    private fun wrap(t: Throwable): Throwable = when (t) {
        is ApiException -> ApiException(humanize(t), t.status)
        else -> ApiException(OFFLINE)
    }

    private fun humanize(e: ApiException): String {
        val msg = e.message.orEmpty()
        val blankOrGeneric = msg.isBlank() || msg.startsWith("Request failed")
        return when (e.status) {
            401 -> if (blankOrGeneric) "Invalid email or password." else msg
            404 -> HUMANIZED_OTP_UNAVAILABLE
            503 -> when {
                msg.contains("email not configured", ignoreCase = true) -> HUMANIZED_OTP_UNAVAILABLE
                blankOrGeneric -> "Zikra is temporarily unavailable. Try again shortly."
                else -> msg
            }
            else -> msg.ifBlank { "Request failed (${e.status ?: "?"})" }
        }
    }

    companion object {
        private const val OFFLINE =
            "Can’t reach Zikra online. Turn on Wi‑Fi or mobile data, then try again — or continue as guest."
        const val HUMANIZED_OTP_UNAVAILABLE =
            "Email one-time codes aren’t configured on the server yet. Use your password, or continue as guest."

        fun isOtpUnavailable(err: Throwable): Boolean {
            val msg = err.message.orEmpty()
            return msg.contains("email not configured", ignoreCase = true) ||
                msg.contains("Use your password", ignoreCase = true) ||
                msg.contains("one-time codes", ignoreCase = true)
        }
    }
}
