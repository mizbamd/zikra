package com.mizbamd.zikra.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "frames")
data class FrameEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val arabic: String,
    val transliteration: String,
    val target: Int?,
    val lifetimeCount: Int,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
    val deleted: Boolean = false,
    val dirty: Boolean = true,
)

@Entity(
    tableName = "daily_counts",
    indices = [Index(value = ["userId", "frameId", "date"], unique = true)],
)
data class DailyCountEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val frameId: String,
    val date: String,
    val count: Int,
    val updatedAt: String,
    val dirty: Boolean = true,
)

@Dao
interface FrameDao {
    @Query("SELECT * FROM frames WHERE userId = :userId AND deleted = 0 ORDER BY sortOrder, createdAt")
    fun observeActive(userId: String): Flow<List<FrameEntity>>

    @Query("SELECT * FROM frames WHERE userId = :userId AND deleted = 0 ORDER BY sortOrder, createdAt")
    suspend fun listActive(userId: String): List<FrameEntity>

    @Query("SELECT * FROM frames WHERE id = :id LIMIT 1")
    suspend fun get(id: String): FrameEntity?

    @Query("SELECT * FROM frames WHERE userId = :userId AND dirty = 1")
    suspend fun dirty(userId: String): List<FrameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(frame: FrameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(frames: List<FrameEntity>)

    @Update
    suspend fun update(frame: FrameEntity)

    @Query("SELECT COUNT(*) FROM frames WHERE userId = :userId AND deleted = 0")
    suspend fun countActive(userId: String): Int

    @Query("DELETE FROM frames WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)
}

@Dao
interface DailyCountDao {
    @Query("SELECT * FROM daily_counts WHERE userId = :userId AND frameId = :frameId AND date = :date LIMIT 1")
    suspend fun get(userId: String, frameId: String, date: String): DailyCountEntity?

    @Query("SELECT * FROM daily_counts WHERE userId = :userId ORDER BY date DESC")
    fun observeAll(userId: String): Flow<List<DailyCountEntity>>

    @Query("SELECT * FROM daily_counts WHERE userId = :userId ORDER BY date DESC")
    suspend fun listAll(userId: String): List<DailyCountEntity>

    @Query("SELECT * FROM daily_counts WHERE userId = :userId AND dirty = 1")
    suspend fun dirty(userId: String): List<DailyCountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: DailyCountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<DailyCountEntity>)

    @Query("DELETE FROM daily_counts WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)

    /** ISO `yyyy-MM-dd` strings compare lexicographically. See [HistoryRetention]. */
    @Query("DELETE FROM daily_counts WHERE date < :cutoff")
    suspend fun deleteOlderThan(cutoff: String)
}

@Database(
    entities = [FrameEntity::class, DailyCountEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ZikraDatabase : RoomDatabase() {
    abstract fun frames(): FrameDao
    abstract fun dailyCounts(): DailyCountDao
}
