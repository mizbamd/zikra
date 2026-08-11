package com.mizbamd.zikra.routes

import com.mizbamd.zikra.auth.Mailer
import com.mizbamd.zikra.auth.MailerException
import com.mizbamd.zikra.auth.OtpCodes
import com.mizbamd.zikra.auth.Security
import com.mizbamd.zikra.config.Env
import com.mizbamd.zikra.catalog.DhikrCatalog
import com.mizbamd.zikra.entitlements.FrameLimitPolicy
import com.mizbamd.zikra.models.AccountDeletedResponse
import com.mizbamd.zikra.models.AuthResponse
import com.mizbamd.zikra.models.ErrorResponse
import com.mizbamd.zikra.models.GoogleSignInRequest
import com.mizbamd.zikra.models.HealthResponse
import com.mizbamd.zikra.models.LoginRequest
import com.mizbamd.zikra.models.OtpRequest
import com.mizbamd.zikra.models.OtpRequestResponse
import com.mizbamd.zikra.models.OtpVerifyRequest
import com.mizbamd.zikra.models.RegisterRequest
import com.mizbamd.zikra.models.SyncPullResponse
import com.mizbamd.zikra.models.SyncPushRequest
import com.mizbamd.zikra.models.SyncPushResponse
import com.mizbamd.zikra.ratelimit.RateLimitResult
import com.mizbamd.zikra.ratelimit.RateLimiter
import com.mizbamd.zikra.repo.DailyCountRepo
import com.mizbamd.zikra.repo.FrameRepo
import com.mizbamd.zikra.repo.UserRepo
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import java.util.UUID

fun Application.configureRoutes(
    env: Env,
    security: Security,
    users: UserRepo,
    frames: FrameRepo,
    dailyCounts: DailyCountRepo,
    otpCodes: OtpCodes,
    mailer: Mailer,
    limiter: RateLimiter,
) {
    val log = LoggerFactory.getLogger("zikra.auth")
    routing {
        get("/health") {
            call.respond(HealthResponse())
        }

        route("/v1/auth") {
            post("/register") {
                val body = call.receive<RegisterRequest>()
                val email = body.email.trim().lowercase()
                val password = body.password
                if (!isValidEmail(email) || password.length < 8) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email and a password of 8+ characters are required."))
                    return@post
                }
                if (users.findByEmail(email) != null) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("An account with that email already exists."))
                    return@post
                }
                val user = users.insert(email, security.hashPassword(password))
                call.respond(
                    HttpStatusCode.Created,
                    AuthResponse(
                        token = security.issueToken(user.id, user.email),
                        userId = user.id.toString(),
                        email = user.email,
                    ),
                )
            }

            post("/login") {
                val body = call.receive<LoginRequest>()
                val user = users.findByEmail(body.email.trim().lowercase())
                val hash = user?.passwordHash
                if (user == null || hash.isNullOrBlank() || !security.verifyPassword(body.password, hash)) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid email or password."))
                    return@post
                }
                call.respond(
                    AuthResponse(
                        token = security.issueToken(user.id, user.email),
                        userId = user.id.toString(),
                        email = user.email,
                    ),
                )
            }

            post("/otp/request") {
                val body = call.receive<OtpRequest>()
                val email = body.email.trim().lowercase()
                if (!isValidEmail(email)) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("A valid email is required."))
                    return@post
                }
                when (val emailLimit = limiter.tryConsume("otp-email:$email", 1, 60_000L)) {
                    RateLimitResult.Allowed -> Unit
                    is RateLimitResult.Denied -> {
                        call.response.header(HttpHeaders.RetryAfter, emailLimit.retryAfterSeconds.toString())
                        call.respond(
                            HttpStatusCode.TooManyRequests,
                            ErrorResponse("Too many requests. Try again later."),
                        )
                        return@post
                    }
                }
                if (!mailer.configured && env.production) {
                    call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("email not configured"))
                    return@post
                }
                val issued = otpCodes.issue(email)
                try {
                    mailer.sendOtp(email, issued.code)
                } catch (_: MailerException) {
                    otpCodes.invalidate(email)
                    log.warn("OTP email send failed")
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        ErrorResponse("Could not send email. Try again later."),
                    )
                    return@post
                }
                call.respond(OtpRequestResponse(ok = true, expiresInSeconds = issued.expiresInSeconds.toInt()))
            }

            post("/otp/verify") {
                val body = call.receive<OtpVerifyRequest>()
                val email = body.email.trim().lowercase()
                val code = body.code.trim()
                if (!isValidEmail(email) || !OTP_CODE.matches(code)) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email and a 6-digit code are required."))
                    return@post
                }
                if (!otpCodes.consume(email, code)) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired code."))
                    return@post
                }
                val user = users.findOrCreateByEmail(email)
                call.respond(
                    AuthResponse(
                        token = security.issueToken(user.id, user.email),
                        userId = user.id.toString(),
                        email = user.email,
                    ),
                )
            }

            // Google Sign-In stub. Wire GOOGLE_WEB_CLIENT_ID and verify idToken with Google
            // tokeninfo / GoogleIdTokenVerifier when a client ID is provisioned.
            post("/google") {
                call.receive<GoogleSignInRequest>()
                val message = if (env.googleWebClientId.isNullOrBlank()) {
                    "Google Sign-In is not configured. Add GOOGLE_WEB_CLIENT_ID and verify the ID token."
                } else {
                    "Google Sign-In token verification is not implemented in v1 yet."
                }
                call.respond(HttpStatusCode.NotImplemented, ErrorResponse(message))
            }
        }

        authenticate("auth-jwt") {
            get("/v1/me") {
                val userId = call.userId() ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Sign in required."))
                    return@get
                }
                val user = users.findById(userId)
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Unknown user."))
                    return@get
                }
                call.respond(AuthResponse(token = "", userId = user.id.toString(), email = user.email))
            }

            get("/v1/sync") {
                val userId = call.userId() ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Sign in required."))
                    return@get
                }
                call.respond(
                    SyncPullResponse(
                        frames = frames.listForUser(userId),
                        dailyCounts = dailyCounts.listForUser(userId),
                    ),
                )
            }

            post("/v1/sync") {
                val userId = call.userId() ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Sign in required."))
                    return@post
                }
                val body = call.receive<SyncPushRequest>()
                val frameResult = frames.upsertAll(userId, body.frames)
                body.dailyCounts
                    .filter { it.frameId !in frameResult.rejectedFrameIds }
                    .forEach { dailyCounts.upsert(userId, it) }
                if (frameResult.overLimit) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse(FrameLimitPolicy.message()),
                    )
                    return@post
                }
                if (frameResult.offCatalog) {
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        ErrorResponse(DhikrCatalog.message()),
                    )
                    return@post
                }
                call.respond(
                    SyncPushResponse(
                        frames = frames.listForUser(userId),
                        dailyCounts = dailyCounts.listForUser(userId),
                    ),
                )
            }

            delete("/v1/account") { call.deleteAccount(users) }
            post("/v1/account/delete") { call.deleteAccount(users) }
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.deleteAccount(users: UserRepo) {
    val userId = userId() ?: run {
        respond(HttpStatusCode.Unauthorized, ErrorResponse("Sign in required."))
        return
    }
    if (!users.deleteById(userId)) {
        respond(HttpStatusCode.NotFound, ErrorResponse("Unknown user."))
        return
    }
    respond(HttpStatusCode.OK, AccountDeletedResponse())
}

private fun io.ktor.server.application.ApplicationCall.userId(): UUID? {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.subject?.let { UUID.fromString(it) }
}

internal fun isValidEmail(email: String): Boolean {
    val at = email.indexOf('@')
    if (at <= 0 || at == email.lastIndex) return false
    val domain = email.substring(at + 1)
    return '.' in domain && ' ' !in email
}

private val OTP_CODE = Regex("^\\d{6}$")
