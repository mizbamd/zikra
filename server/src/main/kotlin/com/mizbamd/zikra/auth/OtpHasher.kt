package com.mizbamd.zikra.auth

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * HMAC-SHA256 of `email:code` keyed with [JWT_SECRET]. Codes are never stored in plaintext.
 */
class OtpHasher(secret: String) {
    private val key = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), HMAC)

    fun hash(email: String, code: String): ByteArray {
        val mac = Mac.getInstance(HMAC)
        mac.init(key)
        return mac.doFinal("${email.lowercase()}:$code".toByteArray(StandardCharsets.UTF_8))
    }

    fun matches(email: String, code: String, stored: ByteArray): Boolean {
        val computed = hash(email, code)
        return MessageDigest.isEqual(computed, stored)
    }

    companion object {
        private const val HMAC = "HmacSHA256"
        private val random = SecureRandom()

        fun randomCode(): String = "%06d".format(random.nextInt(1_000_000))
    }
}
