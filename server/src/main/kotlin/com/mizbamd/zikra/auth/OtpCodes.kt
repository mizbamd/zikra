package com.mizbamd.zikra.auth

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * One active hashed OTP per email. In-memory is enough for a single Fly machine.
 */
class OtpCodes(
    private val hasher: OtpHasher,
    private val clock: () -> Instant = { Instant.now() },
    private val generateCode: () -> String = { OtpHasher.randomCode() },
) {
    private val byEmail = ConcurrentHashMap<String, Entry>()

    data class Issue(
        val code: String,
        val expiresInSeconds: Long = TTL.seconds,
    )

    fun issue(email: String): Issue {
        val key = email.lowercase()
        val code = generateCode()
        val now = clock()
        byEmail[key] = Entry(
            hash = hasher.hash(key, code),
            expiresAt = now.plus(TTL),
            attempts = 0,
        )
        return Issue(code)
    }

    /**
     * @return true if the code is valid. Consumes the code on success.
     * Invalidates after [MAX_ATTEMPTS] failures or expiry.
     */
    fun consume(email: String, code: String): Boolean {
        val key = email.lowercase()
        val entry = byEmail[key] ?: return false
        val now = clock()
        if (!now.isBefore(entry.expiresAt)) {
            byEmail.remove(key)
            return false
        }
        if (entry.attempts >= MAX_ATTEMPTS) {
            byEmail.remove(key)
            return false
        }
        if (!hasher.matches(key, code, entry.hash)) {
            val next = entry.attempts + 1
            if (next >= MAX_ATTEMPTS) {
                byEmail.remove(key)
            } else {
                byEmail[key] = entry.copy(attempts = next)
            }
            return false
        }
        byEmail.remove(key)
        return true
    }

    fun invalidate(email: String) {
        byEmail.remove(email.lowercase())
    }

    private data class Entry(
        val hash: ByteArray,
        val expiresAt: Instant,
        val attempts: Int,
    )

    companion object {
        val TTL: Duration = Duration.ofMinutes(10)
        const val MAX_ATTEMPTS = 5
    }
}
