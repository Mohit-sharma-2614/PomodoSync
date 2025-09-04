package com.example.pomodosync.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val taskId: Long = 0,
    val name: String,
    val creationDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val estimatedPomodoros: Int = 1,
    val completedPomodoros: Int = 0
)
