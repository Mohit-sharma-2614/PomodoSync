package com.example.pomodosync.di

import android.content.Context
import androidx.room.Room
import com.example.pomodosync.data.local.dao.PomodoroSessionDao
import com.example.pomodosync.data.local.dao.TaskDao
import com.example.pomodosync.data.local.db.PomodoDb
import com.example.pomodosync.data.preferences.PomodoroSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun getDatabase(@ApplicationContext context: Context): PomodoDb {
        return Room.databaseBuilder(
            context.applicationContext, PomodoDb::class.java, "pomodo_db"
        ).build()
    }

    @Provides
    fun provideTaskDao(db: PomodoDb): TaskDao {
        return db.taskDao()
    }

    @Provides
    fun providePomodoroSessionDao(db: PomodoDb): PomodoroSessionDao {
        return db.pomodoroSessionDao()
    }

    @Singleton
    @Provides
    fun providePomodoroSettingsRepository(@ApplicationContext context: Context): PomodoroSettingsRepository {
        return PomodoroSettingsRepository(context)
    }
}