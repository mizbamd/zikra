package com.mizbamd.zikra.ratelimit

import com.mizbamd.zikra.models.ErrorResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond

data class RateLimitRule(
    val name: String,
    val method: HttpMethod,
    val path: String,
    val limit: Int,
    val windowMs: Long,
)

class ZikraRateLimitConfig {
    lateinit var limiter: RateLimiter
    var rules: List<RateLimitRule> = emptyList()
}

val ZikraRateLimit = createApplicationPlugin(
    name = "ZikraRateLimit",
    createConfiguration = ::ZikraRateLimitConfig,
) {
    val limiter = pluginConfig.limiter
    val rules = pluginConfig.rules
    onCall { call ->
        val rule = rules.find {
            it.method == call.request.httpMethod && it.path == call.request.path()
        } ?: return@onCall
        when (val result = limiter.tryConsume("${rule.name}:${call.clientIp()}", rule.limit, rule.windowMs)) {
            RateLimitResult.Allowed -> Unit
            is RateLimitResult.Denied -> {
                call.response.header(HttpHeaders.RetryAfter, result.retryAfterSeconds.toString())
                call.respond(
                    HttpStatusCode.TooManyRequests,
                    ErrorResponse("Too many requests. Try again later."),
                )
            }
        }
    }
}

fun ApplicationCall.clientIp(): String {
    request.headers["Fly-Client-IP"]?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    request.headers[HttpHeaders.XForwardedFor]
        ?.split(",")
        ?.firstOrNull()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { return it }
    return request.origin.remoteHost
}

object RateLimitRules {
    private const val MINUTE = 60_000L
    private const val FIFTEEN_MIN = 15 * MINUTE
    private const val HOUR = 60 * MINUTE

    val defaults: List<RateLimitRule> = listOf(
        RateLimitRule("otp-request-ip", HttpMethod.Post, "/v1/auth/otp/request", limit = 5, windowMs = HOUR),
        RateLimitRule("otp-verify-ip", HttpMethod.Post, "/v1/auth/otp/verify", limit = 10, windowMs = FIFTEEN_MIN),
        RateLimitRule("register-ip", HttpMethod.Post, "/v1/auth/register", limit = 5, windowMs = FIFTEEN_MIN),
        RateLimitRule("login-ip", HttpMethod.Post, "/v1/auth/login", limit = 5, windowMs = FIFTEEN_MIN),
        RateLimitRule("sync-ip", HttpMethod.Post, "/v1/sync", limit = 30, windowMs = MINUTE),
        RateLimitRule("account-delete-ip", HttpMethod.Delete, "/v1/account", limit = 5, windowMs = HOUR),
        RateLimitRule("account-delete-post-ip", HttpMethod.Post, "/v1/account/delete", limit = 5, windowMs = HOUR),
    )
}
