package com.example.pomodosync.data.repo

import com.example.pomodosync.data.local.entity.Task
import com.example.pomodosync.data.local.relation.TaskWithSessions
import com.example.pomodosync.resources.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface TaskRepo {
    suspend fun insertTask(task: Task): Result<Long>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(task: Task): Result<Unit>
    suspend fun getOrCreateDefaultTask(): Task
    suspend fun getTaskById(taskId: Int): Result<Task?>

    fun getAllTasks(): Flow<List<Task>>
    fun getTaskByCompletionStatus(isCompleted: Boolean): Flow<List<Task>>
    fun getTaskByCreationDate(creationDate: Long): Flow<List<Task>>
    fun getTaskWithSessions(taskId: Long): Flow<TaskWithSessions>
}