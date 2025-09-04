package com.example.pomodosync.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pomodosync.data.local.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {

    // Insert a single session.
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSession(session: PomodoroSession)

    // Delete a session.
    @Delete
    suspend fun deleteSession(session: PomodoroSession)

    // Get all sessions for a specific task, ordered by the most recent first.
    // This is the primary way you'll use the foreign key.
    @Query("SELECT * FROM sessions WHERE associatedTaskId = :taskId ORDER BY startTime DESC")
    fun getSessionsForTask(taskId: Long): Flow<List<PomodoroSession>>

    // Get a count of all completed 'WORK' sessions for a specific task.
    // Useful for updating the UI or the Task entity's progress.
    @Query("SELECT COUNT(*) FROM sessions WHERE associatedTaskId = :taskId AND sessionType = 'WORK' AND status = 'COMPLETED'")
    fun getCompletedSessionCountForTask(taskId: Long): Flow<Int>
}