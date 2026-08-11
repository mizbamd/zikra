package com.mizbamd.zikra.ratelimit

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RateLimiterTest {
    @Test
    fun allowsUpToLimitThenDenies() {
        var now = 1_000_000L
        val limiter = RateLimiter { now }
        repeat(3) {
            assertIs<RateLimitResult.Allowed>(limiter.tryConsume("k", 3, 60_000))
        }
        val denied = limiter.tryConsume("k", 3, 60_000)
        assertIs<RateLimitResult.Denied>(denied)
        assertTrue(denied.retryAfterSeconds >= 1)
    }

    @Test
    fun windowExpiryAllowsAgain() {
        var now = 1_000_000L
        val limiter = RateLimiter { now }
        limiter.tryConsume("k", 1, 60_000)
        now += 60_001
        assertIs<RateLimitResult.Allowed>(limiter.tryConsume("k", 1, 60_000))
    }

    @Test
    fun keysAreIndependent() {
        val limiter = RateLimiter { 1L }
        assertIs<RateLimitResult.Allowed>(limiter.tryConsume("a", 1, 60_000))
        assertIs<RateLimitResult.Allowed>(limiter.tryConsume("b", 1, 60_000))
        assertIs<RateLimitResult.Denied>(limiter.tryConsume("a", 1, 60_000))
    }
}
