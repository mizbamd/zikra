package com.mizbamd.zikra.db

import com.mizbamd.zikra.config.Env
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

class Database(env: Env) {
    private val log = LoggerFactory.getLogger(Database::class.java)

    val dataSource: HikariDataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = env.databaseUrl
            username = env.databaseUser
            password = env.databasePassword
            maximumPoolSize = 8
            poolName = "zikra"
            isAutoCommit = true
        },
    )

    fun migrate() {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
        val result = flyway.migrate()
        log.info("Flyway migrated {} migrations (schema version {})", result.migrationsExecuted, result.targetSchemaVersion)
    }

    fun <T> withConnection(block: Connection.() -> T): T =
        dataSource.connection.use { it.block() }

    fun <T> withTransaction(block: Connection.() -> T): T =
        dataSource.connection.use { conn ->
            val previous = conn.autoCommit
            conn.autoCommit = false
            try {
                val result = conn.block()
                conn.commit()
                result
            } catch (t: Throwable) {
                conn.rollback()
                throw t
            } finally {
                runCatching { conn.autoCommit = previous }
            }
        }
}

fun Connection.query(sql: String, vararg params: Any?, map: (ResultSet) -> Unit) {
    prepareStatement(sql).use { stmt ->
        params.forEachIndexed { i, p -> stmt.setObject(i + 1, p) }
        stmt.executeQuery().use { rs ->
            while (rs.next()) map(rs)
        }
    }
}

fun <T> Connection.queryList(sql: String, vararg params: Any?, map: (ResultSet) -> T): List<T> {
    val out = mutableListOf<T>()
    query(sql, *params) { out += map(it) }
    return out
}

fun <T> Connection.queryOne(sql: String, vararg params: Any?, map: (ResultSet) -> T): T? =
    queryList(sql, *params, map = map).firstOrNull()

fun Connection.update(sql: String, vararg params: Any?): Int {
    prepareStatement(sql).use { stmt ->
        params.forEachIndexed { i, p -> stmt.setObject(i + 1, p) }
        return stmt.executeUpdate()
    }
}

fun uuid(): UUID = UUID.randomUUID()

fun Instant.toTimestamp(): Timestamp = Timestamp.from(this)
