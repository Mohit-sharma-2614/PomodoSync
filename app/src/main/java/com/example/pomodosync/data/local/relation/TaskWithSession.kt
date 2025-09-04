package com.example.pomodosync.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pomodosync.data.local.entity.PomodoroSession
import com.example.pomodosync.data.local.entity.Task

// This class represents the one-to-many relationship
data class TaskWithSessions(
    // The parent entity
    @Embedded
    val task: Task,

    // The list of child entities
    @Relation(
        parentColumn = "taskId",       // Primary key of the Task entity
        entityColumn = "associatedTaskId" // Foreign key in the PomodoroSession entity
    )
    val sessions: List<PomodoroSession>
)
