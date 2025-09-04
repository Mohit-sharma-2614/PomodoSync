package com.example.pomodosync.data.repo.implementation

import com.example.pomodosync.data.local.dao.TaskDao
import com.example.pomodosync.data.local.entity.Task
import com.example.pomodosync.data.local.relation.TaskWithSessions
import com.example.pomodosync.data.repo.TaskRepo
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import com.example.pomodosync.resources.*
import kotlinx.coroutines.flow.catch

class TaskRepoImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepo {
    override suspend fun insertTask(task: Task): Result<Long> {
        return try {
            val rowId = dao.insertTask(task)
            Result.Success(rowId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            dao.updateTask(task)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteTask(task: Task): Result<Unit> {
        return try {
            dao.deleteTask(task)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getOrCreateDefaultTask(): Task {
        val defaultTaskName = "Quick Pomodoro"
        var task = dao.getTaskByName(defaultTaskName)
        if (task == null) {
            task = Task(
                name = "Quick Pomodoro",
                estimatedPomodoros = 4,
                completedPomodoros = 0,
                isCompleted = false,
                creationDate = System.currentTimeMillis(),
            )
            val newId = dao.insertTask(task)
            task = task.copy(taskId = newId)
        }
        return task
    }

    override suspend fun getTaskById(taskId: Int): Result<Task?> {
        return try {
            val task = dao.getTaskById(taskId)
            Result.Success(task)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return dao.getAllTasks()
//            .catch { e ->
//                emit(emptyList()) // Emit empty list on error
//            }
    }

    override fun getTaskByCompletionStatus(isCompleted: Boolean): Flow<List<Task>> {
        return dao.getTaskByCompletionStatus(isCompleted)
//            .catch { e ->
//                emit(emptyList()) // Emit empty list on error
//            }
    }

    override fun getTaskByCreationDate(creationDate: Long): Flow<List<Task>> {
        return dao.getTaskByCreationDate(creationDate)
//            .catch { e ->
//                emit(emptyList()) // Emit empty list on error
//            }
    }

    override fun getTaskWithSessions(taskId: Long): Flow<TaskWithSessions> {
        return dao.getTaskWithSessions(taskId)
//            .catch { e ->
//                throw e // Rethrow or handle differently based on requirements
//                // Alternative: emit(null) if TaskWithSessions is nullable
//            }
    }
}