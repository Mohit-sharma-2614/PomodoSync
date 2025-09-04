package com.example.pomodosync.persentation.pomodoro

import android.widget.Toast
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pomodosync.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            // Consider adding viewModel.clearError() if you want errors to be dismissable
        }
    }

    Scaffold(
        topBar = {
            PomodoroTopBar(
                title = uiState.currentTask?.name ?: "Pomodoro", onBackClick = onBackClick,
                onRefreshClick = { viewModel.stopTimer(isInterrupted = false) /* Or a more specific reset function */ })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .verticalScroll(rememberScrollState())
                .padding(16.dp), // Add overall padding for content
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.currentTask == null && uiState.error == null) {
                Text("Loading task details...") // Or if taskId was invalid from start
            } else if (uiState.currentTask == null && uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                // Task and Session Info
                uiState.currentTask?.let { task ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.currentSessionType.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.totalTaskPomodorosCompleted} Pomodoros",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.5f)) // Pushes timer display down a bit

                // Use your existing PomodoroTimerDisplay, but pass values from ViewModel
                PomodoroTimerDisplay(
                    // Convert timeLeftInMillis to seconds for your formatTime function
                    timeRemaining = uiState.timeLeftInMillis / 1000,
                    progress = if (uiState.initialDurationMillis > 0) {
                        uiState.timeLeftInMillis.toFloat() / uiState.initialDurationMillis.toFloat()
                    } else 0f,
                    sessionType = uiState.currentSessionType // Pass session type for color
                )

                Spacer(modifier = Modifier.padding(8.dp)) // Pushes controls to bottom

                // Timer Controls
                PomodoroControls(
                    timerStatus = uiState.timerStatus,
                    onStartClick = { viewModel.startTimer() },
                    onPauseClick = { viewModel.pauseTimer() },
                    onStopClick = { viewModel.stopTimer() }, // Or reset
                    onSkipClick = { viewModel.skipSession() })
            }
            Spacer(Modifier.padding(vertical = 50.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroTopBar(
    title: String,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit, // Consider what refresh should do
    modifier: Modifier = Modifier
) {
    TopAppBar( // Using Material3 TopAppBar
        title = {
            Text(
                text = title.uppercase(), // POMODORO or Task Name
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }, navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                )
            }
        }, actions = {
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Timer",
                    // tint = colorResource(R.color.gray_text) // Use theme colors if possible
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        ), modifier = modifier
    )
}


@Composable
fun PomodoroTimerDisplay(
    timeRemaining: Long,
    progress: Float,
    sessionType: SessionType,
    modifier: Modifier = Modifier
) {
    val progressColor = when (sessionType) {
        SessionType.WORK -> colorResource(R.color.purple_light)
        SessionType.SHORT_BREAK -> colorResource(R.color.green_accent)
        SessionType.LONG_BREAK -> colorResource(R.color.blue_accent)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "progressAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(250.dp)
    ) {
        val colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = colors,
                    radius = size.minDimension / 2f
                )
            )

            // Progress Arc
            val desiredArcPaddingPx = 15.dp.toPx()
            val strokeWidthPx = 10.dp.toPx()

            // Calculate available space for the arc itself after padding
            val effectiveWidthForArc = size.width - (2 * desiredArcPaddingPx)
            val effectiveHeightForArc = size.height - (2 * desiredArcPaddingPx)

            // Only draw the arc if there's enough space for it to be meaningful
            if (effectiveWidthForArc > strokeWidthPx && effectiveHeightForArc > strokeWidthPx) {
                // The inset lambda provides a new DrawScope with adjusted size and transform
                inset(
                    left = desiredArcPaddingPx,
                    top = desiredArcPaddingPx,
                    right = desiredArcPaddingPx,
                    bottom = desiredArcPaddingPx
                ) {
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = 360 * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }
            }
        }
        Text(
            text = formatTime(timeRemaining),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun PomodoroControls(
    timerStatus: TimerStatus,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val secondaryIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        IconButton(
            onClick = onStopClick,
            enabled = timerStatus != TimerStatus.STOPPED && timerStatus != TimerStatus.FINISHED
        ) {
            Icon(
                Icons.Filled.Stop,
                contentDescription = "Stop/Reset Timer",
                modifier = Modifier.size(36.dp),
                tint = secondaryIconColor
            )
        }

        FilledIconButton(
            onClick = {
                if (timerStatus == TimerStatus.RUNNING) {
                    onPauseClick()
                } else {
                    onStartClick()
                }
            },
            modifier = Modifier.size(72.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (timerStatus == TimerStatus.RUNNING) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Icon(
                imageVector = if (timerStatus == TimerStatus.RUNNING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (timerStatus == TimerStatus.RUNNING) "Pause Timer" else "Start Timer",
                modifier = Modifier.size(48.dp)
            )
        }

        IconButton(
            onClick = onSkipClick,
            // Enable skip unless the task is fully completed or in a similar final state
            enabled = timerStatus != TimerStatus.STOPPED || timerStatus == TimerStatus.FINISHED // Enable if stopped to skip to next or if finished
        ) {
            Icon(
                Icons.Filled.SkipNext,
                contentDescription = "Skip Session",
                modifier = Modifier.size(36.dp),
                tint = secondaryIconColor
            )
        }
    }
}

// helper function for format (time is in seconds)
private fun formatTime(totalSeconds: Long): String {
    if (totalSeconds < 0) return "00:00" // Handle potential negative values if timer overshoots
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
