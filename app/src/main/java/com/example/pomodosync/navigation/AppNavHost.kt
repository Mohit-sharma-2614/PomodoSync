package com.example.pomodosync.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pomodosync.persentation.component.BottomNavComponent
import com.example.pomodosync.persentation.pomodoro.PomodoroScreen
import com.example.pomodosync.persentation.settings.SettingsScreen
import com.example.pomodosync.persentation.tasks.TaskScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController: NavHostController = rememberNavController()
    var isGenericPomodoroPlaying by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if(currentRoute == Tasks.route){
                    TopAppBar(
                        title = { Text(text = "Tasks") }, colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
            }
        },
        bottomBar = {
            BottomNavComponent(
                navController = navController,
                isPlaying = isGenericPomodoroPlaying,
                onPlayPauseClick = {
                    navController.navigate("${PomodoroTimer.route}/0") {
                        // Prevent multiple copies of the same destination
                        launchSingleTop = true
                        restoreState = true

                        // Pop up to the start destination of the graph to avoid building up a huge stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                },
            )
        }) { innerPadding ->
        NavHost(
            navController = navController,
            modifier = Modifier,
            startDestination = Tasks.route,
        ) {
            composable(Tasks.route) {
                TaskScreen(
                    onTaskClick = { taskId ->
                        navController.navigate("${PomodoroTimer.route}/$taskId") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId){
                                saveState = true
                            }
                        }
                    },
                    modifier = Modifier.padding(innerPadding))
            }
            composable(Settings.route) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    })
            }
            composable(
                route = "${PomodoroTimer.route}/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId")
                if (taskId != null) {
                    PomodoroScreen(
                        modifier = Modifier
                            .padding(innerPadding),
                        onBackClick = {
                            navController.popBackStack()
                        })
                } else {
                    Text(text = "Invalid Task ID")
                }
            }
        }
    }
}