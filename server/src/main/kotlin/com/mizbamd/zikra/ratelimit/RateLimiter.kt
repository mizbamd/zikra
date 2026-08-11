package com.mizbamd.zikra.ratelimit

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

sealed class RateLimitResult {
    data object Allowed : RateLimitResult()
    data class Denied(val retryAfterSeconds: Long) : RateLimitResult()
}

/**
 * Sliding-window limiter. Fine for a single Fly machine (~100 users). No Redis.
 */
class RateLimiter(
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private val hits = ConcurrentHashMap<String, ArrayDeque<Long>>()

    fun tryConsume(key: String, limit: Int, windowMs: Long): RateLimitResult {
        require(limit > 0 && windowMs > 0)
        val now = clock()
        val cutoff = now - windowMs
        val q = hits.getOrPut(key) { ArrayDeque() }
        synchronized(q) {
            while (q.isNotEmpty() && q.first() <= cutoff) q.removeFirst()
            if (q.size >= limit) {
                val retryMs = (q.first() + windowMs - now).coerceAtLeast(1L)
                return RateLimitResult.Denied(ceil(retryMs / 1000.0).toLong().coerceAtLeast(1L))
            }
            q.addLast(now)
            return RateLimitResult.Allowed
        }
    }
}
