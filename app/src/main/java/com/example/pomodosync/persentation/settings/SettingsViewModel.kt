package com.example.pomodosync.persentation.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodosync.data.preferences.PomodoroSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val workDuration: Int = PomodoroSettingsRepository.DEFAULT_WORK_DURATION,
    val shortBreakDuration: Int = PomodoroSettingsRepository.DEFAULT_SHORT_BREAK_DURATION,
    val longBreakDuration: Int = PomodoroSettingsRepository.DEFAULT_LONG_BREAK_DURATION,
    val isLoading: Boolean = true // Added isLoading to match your original state
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: PomodoroSettingsRepository
) : ViewModel() {

    val settingsUiState: StateFlow<SettingsUiState> =
        settingsRepository.pomodoroSettingsFlow.map { pomodoroSettings -> // Map PomodoroSettings to SettingsUiState
                SettingsUiState(
                    workDuration = pomodoroSettings.workDurationMinutes,
                    shortBreakDuration = pomodoroSettings.shortBreakDurationMinutes,
                    longBreakDuration = pomodoroSettings.longBreakDurationMinutes,
                    isLoading = false // Data is loaded
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SettingsUiState(isLoading = true) // Initial state while loading
            )

    fun updateWorkDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateWorkDuration(minutes.coerceIn(1, 120))
        }
    }

    fun updateShortBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateShortBreakDuration(minutes.coerceIn(1, 30))
        }
    }

    fun updateLongBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateLongBreakDuration(minutes.coerceIn(5, 60))
        }
    }
}
