package com.example.pomodosync.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    indices = [
        Index(
            value = ["associatedTaskId"],
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["taskId"],
            childColumns = ["associatedTaskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0,
    val associatedTaskId: Long, // Foreign Key
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val sessionType: String, // "WORK", "SHORT_BREAK", "LONG_BREAK"
    val status: String // "COMPLETED", "INTERRUPTED"
)
