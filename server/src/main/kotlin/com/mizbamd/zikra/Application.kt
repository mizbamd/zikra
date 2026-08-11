package com.mizbamd.zikra

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mizbamd.zikra.auth.LogMailer
import com.mizbamd.zikra.auth.OtpCodes
import com.mizbamd.zikra.auth.OtpHasher
import com.mizbamd.zikra.auth.ResendMailer
import com.mizbamd.zikra.auth.Security
import com.mizbamd.zikra.catalog.DhikrCatalog
import com.mizbamd.zikra.config.Env
import com.mizbamd.zikra.db.Database
import com.mizbamd.zikra.models.ErrorResponse
import com.mizbamd.zikra.ratelimit.RateLimitRules
import com.mizbamd.zikra.ratelimit.RateLimiter
import com.mizbamd.zikra.ratelimit.ZikraRateLimit
import com.mizbamd.zikra.repo.DailyCountRepo
import com.mizbamd.zikra.repo.FrameRepo
import com.mizbamd.zikra.repo.UserRepo
import com.mizbamd.zikra.routes.configureRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.net.URI

fun main() {
    val env = Env.load()
    val log = LoggerFactory.getLogger("zikra")
    log.info("Starting Zikra API on {}:{}", env.host, env.port)
    embeddedServer(Netty, port = env.port, host = env.host) {
        zikraModule(env)
    }.start(wait = true)
}

fun Application.zikraModule(env: Env = Env.load()) {
    val catalog = DhikrCatalog::class.java.getResourceAsStream("/dhikr.json")
        ?: error("dhikr.json missing from classpath (shared catalog/)")
    DhikrCatalog.load(catalog)

    val db = Database(env)
    db.migrate()

    val security = Security(env.jwtSecret)
    val users = UserRepo(db)
    val frames = FrameRepo(db)
    val dailyCounts = DailyCountRepo(db)
    val limiter = RateLimiter()
    val otpCodes = OtpCodes(OtpHasher(env.jwtSecret))
    val mailer = if (env.emailConfigured) {
        ResendMailer(env.resendApiKey!!, env.otpFromEmail!!)
    } else {
        LogMailer()
    }

    install(CallLogging) {
        level = Level.INFO
        // Path only — never URI/query, Authorization, JWT, passwords, or OTP codes.
        format { call ->
            "${call.request.httpMethod.value} ${call.request.path()} - ${call.response.status()?.value}"
        }
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
        )
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        if (env.corsOrigins.isEmpty()) {
            anyHost()
        } else {
            env.corsOrigins.forEach { raw ->
                val uri = runCatching { URI(raw) }.getOrNull()
                if (uri?.host != null) {
                    allowHost(uri.host, schemes = listOfNotNull(uri.scheme).ifEmpty { listOf("https") })
                } else {
                    allowHost(raw.removePrefix("https://").removePrefix("http://"), schemes = listOf("https"))
                }
            }
        }
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            LoggerFactory.getLogger("zikra").error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(if (env.production) "Internal error" else (cause.message ?: "Internal error")),
            )
        }
    }
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWT.require(Algorithm.HMAC256(env.jwtSecret)).withIssuer(Security.ISSUER).build())
            validate { credential ->
                credential.payload.subject?.let { JWTPrincipal(credential.payload) }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Sign in required."))
            }
        }
    }
    install(ZikraRateLimit) {
        this.limiter = limiter
        this.rules = RateLimitRules.defaults
    }

    configureRoutes(env, security, users, frames, dailyCounts, otpCodes, mailer, limiter)
}
