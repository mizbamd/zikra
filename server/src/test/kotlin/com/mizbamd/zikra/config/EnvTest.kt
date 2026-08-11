package com.mizbamd.zikra.config

import kotlin.test.Test
import kotlin.test.assertEquals

class EnvTest {
    @Test
    fun postgresUrlBecomesJdbc() {
        val parsed = Env.parseDatabaseUrl(
            "postgres://zikra:s3cret@db.example:5432/zikra?sslmode=require",
            "ignored",
            "ignored",
        )
        assertEquals("jdbc:postgresql://db.example:5432/zikra?sslmode=require", parsed.jdbcUrl)
        assertEquals("zikra", parsed.user)
        assertEquals("s3cret", parsed.password)
    }

    @Test
    fun jdbcUrlIsUnchanged() {
        val parsed = Env.parseDatabaseUrl(
            "jdbc:postgresql://localhost:5432/zikra",
            "samreen",
            "",
        )
        assertEquals("jdbc:postgresql://localhost:5432/zikra", parsed.jdbcUrl)
        assertEquals("samreen", parsed.user)
        assertEquals("", parsed.password)
    }

    @Test
    fun emailConfiguredRequiresBoth() {
        fun env(key: String?, from: String?) = Env(
            databaseUrl = "jdbc:postgresql://localhost:5432/zikra",
            databaseUser = "samreen",
            databasePassword = "",
            jwtSecret = Env.LOCAL_JWT_DEFAULT,
            port = 8080,
            host = "0.0.0.0",
            googleWebClientId = null,
            corsOrigins = emptyList(),
            production = false,
            resendApiKey = key,
            otpFromEmail = from,
        )
        assertEquals(true, env("re_test", "Zikra <noreply@example.com>").emailConfigured)
        assertEquals(false, env("re_test", null).emailConfigured)
        assertEquals(false, env(null, "noreply@example.com").emailConfigured)
        assertEquals(false, env("", "noreply@example.com").emailConfigured)
    }
}
