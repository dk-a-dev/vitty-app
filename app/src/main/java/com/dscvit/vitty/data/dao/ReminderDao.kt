package com.dscvit.vitty.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dscvit.vitty.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE courseId = :courseId ORDER BY dateMillis ASC")
    fun getRemindersByCourse(courseId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY dateMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE dateMillis >= :startTime AND dateMillis <= :endTime AND isCompleted = 0")
    suspend fun getRemindersInRange(
        startTime: Long,
        endTime: Long,
    ): List<ReminderEntity>

    @Insert
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE courseId = :courseId")
    suspend fun deleteRemindersByCourse(courseId: String)

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletedStatus(
        id: Long,
        isCompleted: Boolean,
    )

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dateMillis ASC")
    suspend fun getAllPendingReminders(): List<ReminderEntity>
}
