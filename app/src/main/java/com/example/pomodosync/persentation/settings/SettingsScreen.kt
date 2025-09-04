package com.example.pomodosync.persentation.settings


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.settingsUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "Pomodoro Durations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    // 3. Use the primary theme color for section headers
                    color = MaterialTheme.colorScheme.primary
                )

                DurationSettingItem(
                    label = "Work Session",
                    durationMinutes = uiState.workDuration,
                    onDurationChange = { viewModel.updateWorkDuration(it) },
                    valueRange = 1f..120f, // 1 min to 2 hours
                    steps = 118 // (120-1-1) for discrete steps; 0 for continuous
                )

                DurationSettingItem(
                    label = "Short Break",
                    durationMinutes = uiState.shortBreakDuration,
                    onDurationChange = { viewModel.updateShortBreakDuration(it) },
                    valueRange = 1f..30f,
                    steps = 28
                )

                DurationSettingItem(
                    label = "Long Break",
                    durationMinutes = uiState.longBreakDuration,
                    onDurationChange = { viewModel.updateLongBreakDuration(it) },
                    valueRange = 5f..60f,
                    steps = 54
                )

                Spacer(Modifier.padding(vertical = 100.dp))


            }
        }
    }
}

@Composable
fun DurationSettingItem(
    label: String,
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int // Number of discrete steps for the slider. (rangeEnd - rangeStart - 1 for integer steps)
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface // Explicitly set text color
            )
            Text(
                "$durationMinutes min",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value = durationMinutes.toFloat(), onValueChange = { newValue ->
                onDurationChange(newValue.roundToInt())
            }, valueRange = valueRange, steps = steps, // For discrete values (e.g., every 1 minute)
            modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(onBackClick = {})
}
