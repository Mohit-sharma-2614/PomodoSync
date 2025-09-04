package com.example.pomodosync.persentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pomodosync.navigation.Settings
import com.example.pomodosync.navigation.Tasks

// (Keep your WavyBottomBarShape class exactly as it was)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomNavComponent(
    navController: NavHostController, isPlaying: Boolean, onPlayPauseClick: () -> Unit
) {
    val playButtonGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    BottomAppBar(
        actions = {
            // Task Item
            NavigationBarItem(
                selected = currentRoute == Tasks.route,
                onClick = {
                    navController.navigate(Tasks.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
                label = { Text("Tasks") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Settings Item
            NavigationBarItem(
                selected = currentRoute == Settings.route,
                onClick = {
                    navController.navigate(Settings.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onPlayPauseClick,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {

                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = {
                        scaleIn(initialScale = 0.7f) + fadeIn() togetherWith
                                scaleOut(targetScale = 0.7f) + fadeOut()
                    },
                    label = "fab_icon_animation"
                ) { targetIsPlaying ->
                    val icon = Icons.Default.Timer
                    Icon(
                        imageVector = icon,
                        contentDescription = if (targetIsPlaying) "Timer" else "Timer",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    )
}


// A custom shape for our wavy bottom bar
//class WavyBottomBarShape(
//    private val waveHeight: Dp = 30.dp
//) : Shape {
//    override fun createOutline(
//        size: Size, layoutDirection: LayoutDirection, density: Density
//    ): Outline {
//        val waveHeightPx = with(density) { waveHeight.toPx() }
//        val curveOffset = size.width / 2f
//
//        val path = Path().apply {
//            moveTo(0f, waveHeightPx)
//            lineTo(0f, size.height)
//            lineTo(size.width, size.height)
//            lineTo(size.width, waveHeightPx)
//            // The two curves that form the "dip"
//            quadraticTo(size.width * 0.75f, 0f, curveOffset, waveHeightPx)
//            quadraticTo(size.width * 0.25f, 0f, 0f, waveHeightPx)
//            close()
//        }
//        return Outline.Generic(path)
//    }
//}
//
//@Composable
//fun BottomNavComponent(
//    navController: NavHostController, isPlaying: Boolean, onPlayPauseClick: () -> Unit
//) {
//    val playButtonGradient = Brush.verticalGradient(
//        colors = listOf(
//            MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary
//        )
//    )
//
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    // Use the standard BottomAppBar for better structure
//    BottomAppBar(
//        modifier = Modifier
//            .shadow(elevation = 10.dp) // Optional shadow for depth
//            .graphicsLayer {
//                // Apply our custom wavy shape and clip the content to it
//                shape = WavyBottomBarShape()
//                clip = true
//            },
//        containerColor = MaterialTheme.colorScheme.surface,
//        contentColor = MaterialTheme.colorScheme.onSurface,
//        tonalElevation = 10.dp
//    ) {
//        NavigationBar(
//            modifier = Modifier.weight(1f),
//            containerColor = Color.Transparent
//        ) {
//            // Task Item
//            NavigationBarItem(
//                selected = currentRoute == Tasks.route,
//                onClick = {
//                    navController.navigate(Tasks.route) {
//                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                },
//                icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
//                label = { Text("Tasks") }, // This adds the label back!
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = MaterialTheme.colorScheme.primary,
//                    selectedTextColor = MaterialTheme.colorScheme.primary,
//                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            )
//
//            // Spacer to create a gap for the FAB
//            Spacer(modifier = Modifier.weight(0.4f))
//
//            // Settings Item
//            NavigationBarItem(
//                selected = currentRoute == Settings.route,
//                onClick = {
//                    navController.navigate(Settings.route) {
//                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                },
//                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
//                label = { Text("Settings") }, // And here too!
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = MaterialTheme.colorScheme.primary,
//                    selectedTextColor = MaterialTheme.colorScheme.primary,
//                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            )
//        }
//    }
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(IntrinsicSize.Min), // Height wraps content
//        contentAlignment = Alignment.TopCenter // Align FAB to the top-center
//    ) {
//        FloatingActionButton(
//            onClick = onPlayPauseClick,
//            containerColor = Color.Transparent, // Transparent container to show our custom Box
//            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(80.dp)
//                    .clip(CircleShape)
//                    .background(playButtonGradient),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Timer,
//                    contentDescription = if (isPlaying) "Pause" else "Play",
//                    tint = MaterialTheme.colorScheme.onPrimary,
//                    modifier = Modifier.size(48.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun NavigationIcon(
//    isSelected: Boolean,
//    onClick: () -> Unit,
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    contentDescription: String
//) {
//    IconButton(onClick = onClick) {
//        Icon(
//            imageVector = icon,
//            contentDescription = contentDescription,
//            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
//            modifier = Modifier.size(32.dp)
//        )
//    }
//}

@Preview(showBackground = true)
@Composable
fun BottomNavComponentPreview() {
    BottomNavComponent(
        navController = NavHostController(LocalContext.current),
        isPlaying = false,
        onPlayPauseClick = {}
    )
}