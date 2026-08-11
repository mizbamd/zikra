package com.mizbamd.zikra.auth

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtpHasherTest {
    private val hasher = OtpHasher("test-secret-please-use-a-long-value")

    @Test
    fun sameInputSameHash() {
        val a = hasher.hash("a@example.com", "123456")
        val b = hasher.hash("a@example.com", "123456")
        assertTrue(a.contentEquals(b))
    }

    @Test
    fun differentEmailDifferentHash() {
        val a = hasher.hash("a@example.com", "123456")
        val b = hasher.hash("b@example.com", "123456")
        assertFalse(a.contentEquals(b))
    }

    @Test
    fun matchesIgnoresEmailCase() {
        val stored = hasher.hash("user@example.com", "654321")
        assertTrue(hasher.matches("USER@example.com", "654321", stored))
        assertFalse(hasher.matches("user@example.com", "000000", stored))
    }

    @Test
    fun randomCodeIsSixDigits() {
        repeat(20) {
            assertTrue(OtpHasher.randomCode().matches(Regex("^\\d{6}$")))
        }
    }
}

class OtpCodesTest {
    @Test
    fun consumeSucceedsOnce() {
        val codes = OtpCodes(OtpHasher("secret"), generateCode = { "123456" })
        codes.issue("user@example.com")
        assertTrue(codes.consume("user@example.com", "123456"))
        assertFalse(codes.consume("user@example.com", "123456"))
    }

    @Test
    fun wrongCodeThenMaxAttemptsInvalidates() {
        val codes = OtpCodes(OtpHasher("secret"), generateCode = { "123456" })
        codes.issue("user@example.com")
        repeat(OtpCodes.MAX_ATTEMPTS) {
            assertFalse(codes.consume("user@example.com", "000000"))
        }
        assertFalse(codes.consume("user@example.com", "123456"))
    }

    @Test
    fun expiredCodeFails() {
        var now = Instant.parse("2026-08-11T12:00:00Z")
        val codes = OtpCodes(
            OtpHasher("secret"),
            clock = { now },
            generateCode = { "123456" },
        )
        codes.issue("user@example.com")
        now = now.plus(OtpCodes.TTL)
        assertFalse(codes.consume("user@example.com", "123456"))
    }

    @Test
    fun newIssueReplacesPreviousCode() {
        var n = 0
        val codes = OtpCodes(
            OtpHasher("secret"),
            generateCode = { if (n++ == 0) "111111" else "222222" },
        )
        codes.issue("user@example.com")
        codes.issue("user@example.com")
        assertFalse(codes.consume("user@example.com", "111111"))
        assertTrue(codes.consume("user@example.com", "222222"))
    }
}
