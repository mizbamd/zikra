package com.mizbamd.zikra.repo

import com.mizbamd.zikra.db.Database
import com.mizbamd.zikra.db.queryOne
import com.mizbamd.zikra.db.queryList
import com.mizbamd.zikra.db.toTimestamp
import com.mizbamd.zikra.db.update
import com.mizbamd.zikra.models.DailyCountDto
import com.mizbamd.zikra.models.FrameDto
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class UserRow(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val googleId: String?,
)

class UserRepo(private val db: Database) {
    fun findByEmail(email: String): UserRow? = db.withConnection {
        queryOne("SELECT id, email, password_hash, google_id FROM users WHERE email = ?", email.lowercase()) {
            it.toUser()
        }
    }

    fun findById(id: UUID): UserRow? = db.withConnection {
        queryOne("SELECT id, email, password_hash, google_id FROM users WHERE id = ?", id) {
            it.toUser()
        }
    }

    fun insert(email: String, passwordHash: String): UserRow = db.withConnection {
        queryOne(
            """
            INSERT INTO users (email, password_hash)
            VALUES (?, ?)
            RETURNING id, email, password_hash, google_id
            """.trimIndent(),
            email.lowercase(),
            passwordHash,
        ) { it.toUser() } ?: error("insert user failed")
    }

    private fun ResultSet.toUser() = UserRow(
        id = getObject("id", UUID::class.java),
        email = getString("email"),
        passwordHash = getString("password_hash"),
        googleId = getString("google_id"),
    )
}

class FrameRepo(private val db: Database) {
    fun listForUser(userId: UUID): List<FrameDto> = db.withConnection {
        queryList(
            """
            SELECT id, arabic, transliteration, target, lifetime_count, sort_order,
                   created_at, updated_at, deleted_at
            FROM frames WHERE user_id = ?
            ORDER BY sort_order, created_at
            """.trimIndent(),
            userId,
        ) { it.toFrame() }
    }

    fun upsert(userId: UUID, frame: FrameDto) {
        val incomingUpdated = Instant.parse(frame.updatedAt)
        db.withConnection {
            val existing = queryOne(
                "SELECT updated_at FROM frames WHERE id = ? AND user_id = ?",
                UUID.fromString(frame.id),
                userId,
            ) { it.getTimestamp("updated_at").toInstant() }

            if (existing != null && !incomingUpdated.isAfter(existing)) return@withConnection

            update(
                """
                INSERT INTO frames (
                    id, user_id, arabic, transliteration, target, lifetime_count,
                    sort_order, created_at, updated_at, deleted_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    arabic = EXCLUDED.arabic,
                    transliteration = EXCLUDED.transliteration,
                    target = EXCLUDED.target,
                    lifetime_count = EXCLUDED.lifetime_count,
                    sort_order = EXCLUDED.sort_order,
                    updated_at = EXCLUDED.updated_at,
                    deleted_at = EXCLUDED.deleted_at
                WHERE frames.user_id = EXCLUDED.user_id
                """.trimIndent(),
                UUID.fromString(frame.id),
                userId,
                frame.arabic,
                frame.transliteration,
                frame.target,
                frame.lifetimeCount,
                frame.sortOrder,
                Timestamp.from(Instant.parse(frame.createdAt)),
                incomingUpdated.toTimestamp(),
                if (frame.deleted) incomingUpdated.toTimestamp() else null,
            )
        }
    }

    private fun ResultSet.toFrame() = FrameDto(
        id = getObject("id", UUID::class.java).toString(),
        arabic = getString("arabic"),
        transliteration = getString("transliteration"),
        target = getObject("target") as Int?,
        lifetimeCount = getInt("lifetime_count"),
        sortOrder = getInt("sort_order"),
        createdAt = getTimestamp("created_at").toInstant().toString(),
        updatedAt = getTimestamp("updated_at").toInstant().toString(),
        deleted = getTimestamp("deleted_at") != null,
    )
}

class DailyCountRepo(private val db: Database) {
    fun listForUser(userId: UUID): List<DailyCountDto> = db.withConnection {
        queryList(
            """
            SELECT id, frame_id, date, count, updated_at
            FROM daily_counts WHERE user_id = ?
            ORDER BY date DESC
            """.trimIndent(),
            userId,
        ) { it.toCount() }
    }

    fun upsert(userId: UUID, count: DailyCountDto) {
        val incomingUpdated = Instant.parse(count.updatedAt)
        db.withConnection {
            val existing = queryOne(
                "SELECT updated_at FROM daily_counts WHERE user_id = ? AND frame_id = ? AND date = ?",
                userId,
                UUID.fromString(count.frameId),
                Date.valueOf(LocalDate.parse(count.date)),
            ) { it.getTimestamp("updated_at").toInstant() }

            if (existing != null && !incomingUpdated.isAfter(existing)) return@withConnection

            update(
                """
                INSERT INTO daily_counts (id, user_id, frame_id, date, count, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id, frame_id, date) DO UPDATE SET
                    count = EXCLUDED.count,
                    updated_at = EXCLUDED.updated_at,
                    id = EXCLUDED.id
                """.trimIndent(),
                UUID.fromString(count.id),
                userId,
                UUID.fromString(count.frameId),
                Date.valueOf(LocalDate.parse(count.date)),
                count.count,
                incomingUpdated.toTimestamp(),
            )
        }
    }

    private fun ResultSet.toCount() = DailyCountDto(
        id = getObject("id", UUID::class.java).toString(),
        frameId = getObject("frame_id", UUID::class.java).toString(),
        date = getDate("date").toLocalDate().toString(),
        count = getInt("count"),
        updatedAt = getTimestamp("updated_at").toInstant().toString(),
    )
}
