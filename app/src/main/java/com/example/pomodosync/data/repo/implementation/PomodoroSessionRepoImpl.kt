package com.example.pomodosync.data.repo.implementation

import com.example.pomodosync.data.local.dao.PomodoroSessionDao
import com.example.pomodosync.data.local.entity.PomodoroSession
import com.example.pomodosync.data.repo.PomodoroSessionRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.example.pomodosync.resources.*


class PomodoroSessionRepoImpl @Inject constructor(
    private val pomodoroSessionDao: PomodoroSessionDao
): PomodoroSessionRepo {
    override suspend fun insertSession(session: PomodoroSession): Result<Unit> {
        return try {
            Result.Loading
            pomodoroSessionDao.insertSession(session)
            Result.Success(Unit)
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    override suspend fun deleteSession(session: PomodoroSession): Result<Unit> {
        return try {
            Result.Loading
            pomodoroSessionDao.deleteSession(session)
            Result.Success(Unit)
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    override fun getSessionsForTask(taskId: Long): Flow<List<PomodoroSession>> {
        return getSessionsForTask(taskId)
    }

    override fun getCompletedSessionCountForTask(taskId: Long): Flow<Int> {
        return getCompletedSessionCountForTask(taskId)
    }

}