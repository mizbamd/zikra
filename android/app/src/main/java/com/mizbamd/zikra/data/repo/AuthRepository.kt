package com.mizbamd.zikra.data.repo

import com.mizbamd.zikra.data.local.SettingsStore
import com.mizbamd.zikra.data.remote.ApiException
import com.mizbamd.zikra.data.remote.ZikraApi
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val api: ZikraApi,
    private val settings: SettingsStore,
    private val frames: FrameRepository,
) {
    suspend fun register(email: String, password: String) {
        val res = runCatching { api.register(email.trim(), password) }
            .getOrElse { throw wrap(it) }
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
        is ApiException -> t
        else -> ApiException("Can’t reach the Zikra server. Check that it is running, or continue as guest.")
    }
}
