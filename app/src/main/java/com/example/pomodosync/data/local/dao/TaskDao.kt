package com.example.pomodosync.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.pomodosync.data.local.entity.Task
import com.example.pomodosync.data.local.relation.TaskWithSessions
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE name = 'Quick Pomodoro'")
    suspend fun getDefaultTask(): Task?

    @Query("SELECT * FROM tasks WHERE name = :name LIMIT 1")
    suspend fun getTaskByName(name: String): Task?

    @Query("SELECT * FROM tasks WHERE taskId = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted")
    fun getTaskByCompletionStatus(isCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE creationDate = :creationDate")
    fun getTaskByCreationDate(creationDate: Long): Flow<List<Task>>

    // This is the powerful part. Get a specific Task AND all its PomodoroSessions in one query.
    // The @Transaction annotation is crucial to ensure this is done safely (atomically).
    @Transaction
    @Query("SELECT * FROM tasks WHERE taskId = :taskId")
    fun getTaskWithSessions(taskId: Long): Flow<TaskWithSessions>

}