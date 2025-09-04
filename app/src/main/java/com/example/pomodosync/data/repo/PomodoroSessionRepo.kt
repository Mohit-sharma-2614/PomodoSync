package com.example.pomodosync.data.repo

import com.example.pomodosync.data.local.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow
import com.example.pomodosync.resources.*

interface PomodoroSessionRepo {
    suspend fun insertSession(session: PomodoroSession): com.example.pomodosync.resources.Result<Unit>
    suspend fun deleteSession(session: PomodoroSession): Result<Unit>
    fun getSessionsForTask(taskId: Long): Flow<List<PomodoroSession>>
    fun getCompletedSessionCountForTask(taskId: Long): Flow<Int>
}