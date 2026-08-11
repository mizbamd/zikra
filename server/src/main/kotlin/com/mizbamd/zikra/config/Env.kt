package com.mizbamd.zikra.config

import java.io.File

data class Env(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val jwtSecret: String,
    val port: Int,
    val host: String,
    val googleWebClientId: String?,
) {
    companion object {
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

            return Env(
                databaseUrl = v("DATABASE_URL", "jdbc:postgresql://localhost:5432/zikra"),
                databaseUser = v("DATABASE_USER", "samreen"),
                databasePassword = v("DATABASE_PASSWORD", ""),
                jwtSecret = v("JWT_SECRET", "zikra-local-dev-change-me-please-32chars"),
                port = v("PORT", "8080").toInt(),
                host = v("HOST", "0.0.0.0"),
                googleWebClientId = v("GOOGLE_WEB_CLIENT_ID", "").ifBlank { null },
            )
        }
    }
}
