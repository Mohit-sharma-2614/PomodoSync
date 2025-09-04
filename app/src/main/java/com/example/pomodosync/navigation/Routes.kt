package com.example.pomodosync.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pomodosync.R

interface Routes {
    val name: String
    val route: String
    val icon: ImageVector
}

object PomodoroTimer: Routes{
    override val name: String = "Pomodoro"
    override val route: String = "pomodoro"
    override val icon: ImageVector = Icons.Default.Timer
}

object Settings: Routes{
    override val name: String = "Settings"
    override val route: String = "settings"
    override val icon: ImageVector = Icons.Default.Settings
}

object Tasks: Routes{
    override val name: String = "Tasks"
    override val route: String = "tasks"
    override val icon: ImageVector = Icons.Default.Home
}


val bottomNavItems = listOf(Tasks, PomodoroTimer, Settings)