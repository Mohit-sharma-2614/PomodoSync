package com.example.pomodosync.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pomodosync.data.local.dao.PomodoroSessionDao
import com.example.pomodosync.data.local.dao.TaskDao
import com.example.pomodosync.data.local.entity.PomodoroSession
import com.example.pomodosync.data.local.entity.Task

@Database(
    entities = [
        Task::class,
        PomodoroSession::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PomodoDb : RoomDatabase() { // Must extend RoomDatabase

    // Tell the database about each DAO
    abstract fun taskDao(): TaskDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    // Companion object to create a single instance of the database (Singleton pattern)
//    companion object {
//        @Volatile
//        private var INSTANCE: PomodoDb? = null
//
//        fun getDatabase(context: Context): PomodoDb {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    PomodoDb::class.java,
//                    "pomodoro_database" // The actual name of the database file
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
}