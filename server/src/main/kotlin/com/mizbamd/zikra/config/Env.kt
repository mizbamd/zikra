package com.mizbamd.zikra.config

import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class DatabaseTarget(
    val jdbcUrl: String,
    val user: String,
    val password: String,
)

data class Env(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val jwtSecret: String,
    val port: Int,
    val host: String,
    val googleWebClientId: String?,
    val corsOrigins: List<String>,
    val production: Boolean,
) {
    companion object {
        const val LOCAL_JWT_DEFAULT = "zikra-local-dev-change-me-please-32chars"
        const val MIN_PROD_JWT_LENGTH = 32

        fun load(workingDir: File = File(".")): Env {
            val file = mapOf<String, String>().toMutableMap()
            val envFile = File(workingDir, ".env").takeIf { it.exists() }
                ?: File(".env").takeIf { it.exists() }
            envFile?.readLines()?.forEach { raw ->
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) return@forEach
                val idx = line.indexOf('=')
                file[line.substring(0, idx).trim()] = line.substring(idx + 1).trim().trim('"')
            }

            fun v(key: String, default: String): String =
                System.getenv(key)?.ifBlank { null } ?: file[key] ?: default

            val production = v("ZIKRA_ENV", "development").equals("production", ignoreCase = true)
            val jwtSecret = v("JWT_SECRET", LOCAL_JWT_DEFAULT)
            if (production) {
                require(jwtSecret != LOCAL_JWT_DEFAULT) {
                    "JWT_SECRET must be a long random value in production (not the local default)."
                }
                require(jwtSecret.length >= MIN_PROD_JWT_LENGTH) {
                    "JWT_SECRET must be at least $MIN_PROD_JWT_LENGTH characters in production."
                }
            }

            val parsed = parseDatabaseUrl(
                v("DATABASE_URL", "jdbc:postgresql://localhost:5432/zikra"),
                v("DATABASE_USER", "samreen"),
                v("DATABASE_PASSWORD", ""),
            )
            val corsOrigins = v("CORS_ORIGINS", "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            return Env(
                databaseUrl = parsed.jdbcUrl,
                databaseUser = parsed.user,
                databasePassword = parsed.password,
                jwtSecret = jwtSecret,
                port = v("PORT", "8080").toInt(),
                host = v("HOST", "0.0.0.0"),
                googleWebClientId = v("GOOGLE_WEB_CLIENT_ID", "").ifBlank { null },
                corsOrigins = corsOrigins,
                production = production,
            )
        }

        /**
         * Accepts jdbc:postgresql://… or postgres://user:pass@host:5432/db (Fly / Railway).
         */
        fun parseDatabaseUrl(url: String, user: String, password: String): DatabaseTarget {
            val trimmed = url.trim()
            if (!trimmed.startsWith("postgres://") && !trimmed.startsWith("postgresql://")) {
                return DatabaseTarget(trimmed, user, password)
            }
            val normalized = trimmed.replaceFirst(Regex("^postgres://"), "postgresql://")
            val uri = URI(normalized)
            val userInfo = uri.userInfo
            val parsedUser: String
            val parsedPass: String
            if (!userInfo.isNullOrBlank()) {
                val parts = userInfo.split(":", limit = 2)
                parsedUser = decode(parts[0])
                parsedPass = decode(parts.getOrElse(1) { "" })
            } else {
                parsedUser = user
                parsedPass = password
            }
            val host = uri.host ?: "localhost"
            val port = if (uri.port > 0) uri.port else 5432
            val db = uri.path.trimStart('/')
            val query = uri.query?.let { "?$it" }.orEmpty()
            return DatabaseTarget("jdbc:postgresql://$host:$port/$db$query", parsedUser, parsedPass)
        }

        private fun decode(value: String): String =
            URLDecoder.decode(value, StandardCharsets.UTF_8)
    }
}
