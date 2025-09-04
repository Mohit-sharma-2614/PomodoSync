package com.example.pomodosync.di

import com.example.pomodosync.data.repo.PomodoroSessionRepo
import com.example.pomodosync.data.repo.TaskRepo
import com.example.pomodosync.data.repo.implementation.PomodoroSessionRepoImpl
import com.example.pomodosync.data.repo.implementation.TaskRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepo(
        taskRepoImpl: TaskRepoImpl
    ): TaskRepo

    @Binds
    @Singleton
    abstract fun bindPomodoroRepo(
        pomodoroSessionRepoImpl: PomodoroSessionRepoImpl
    ): PomodoroSessionRepo


}