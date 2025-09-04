package com.example.pomodosync.persentation.pomodoro

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodosync.data.local.entity.PomodoroSession
import com.example.pomodosync.data.local.entity.Task
import com.example.pomodosync.data.preferences.PomodoroSettings
import com.example.pomodosync.data.preferences.PomodoroSettingsRepository
import com.example.pomodosync.data.repo.PomodoroSessionRepo
import com.example.pomodosync.data.repo.TaskRepo
import com.example.pomodosync.resources.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// --- Constants for Pomodoro ---
const val WORK_DURATION_MINUTES = 25
const val SHORT_BREAK_DURATION_MINUTES = 5
const val LONG_BREAK_DURATION_MINUTES = 15
const val SESSIONS_BEFORE_LONG_BREAK = 4

enum class SessionType(val displayName: String) {
    WORK("Work"), SHORT_BREAK("Short Break"), LONG_BREAK("Long Break")
}

enum class TimerStatus {
    STOPPED, // Initial or paused
    RUNNING, PAUSED, // Specifically paused by user
    FINISHED // Session completed naturally
}

data class PomodoroUiState(
    val currentTask: Task? = null,
    val timeLeftInMillis: Long = WORK_DURATION_MINUTES * 60 * 1000L,
    val initialDurationMillis: Long = WORK_DURATION_MINUTES * 60 * 1000L,
    val currentSessionType: SessionType = SessionType.WORK,
    val timerStatus: TimerStatus = TimerStatus.STOPPED,
    val workSessionsCompletedThisCycle: Int = 0, // For long break logic
    val totalTaskPomodorosCompleted: Int = 0, // For the current task
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val taskRepo: TaskRepo,
    private val sessionRepo: PomodoroSessionRepo,
    private val settingsRepository: PomodoroSettingsRepository,
    savedStateHandle: SavedStateHandle // To get taskId from navigation
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentSessionStartTime: Long = 0L
    private var currentActiveTaskId: Long? =
        savedStateHandle.get<Long>("taskId")!! // Assumes "taskId" is the nav arg name

    // Store current settings, initialized from DataStore
    private var currentPomodoroSettings: PomodoroSettings = PomodoroSettings( // Default placeholder
        PomodoroSettingsRepository.DEFAULT_WORK_DURATION,
        PomodoroSettingsRepository.DEFAULT_SHORT_BREAK_DURATION,
        PomodoroSettingsRepository.DEFAULT_LONG_BREAK_DURATION
    )

    init {
        viewModelScope.launch {
            currentPomodoroSettings = settingsRepository.pomodoroSettingsFlow.first()
            if (currentActiveTaskId != null && currentActiveTaskId != 0L) { // If a specific task ID was passed
                loadTaskDetails(currentActiveTaskId!!)
            } else if (currentActiveTaskId == 0L) { // If 0L is passed for generic session
                startGenericPomodoroSession() // Start a new generic session
            } else {
                _uiState.update { it.copy(error = "No task selected.", isLoading = false) }
            }
        }
    }

    private fun loadTaskDetails(taskId: Long) {
        if (taskId == 0L) { // Or handle invalid ID appropriately
            _uiState.update { it.copy(error = "Invalid Task ID", isLoading = false) }
            return
        }
        viewModelScope.launch {
            val result = taskRepo.getTaskById(taskId.toInt())
            when (result) {
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message, isLoading = false) }
                }

                is Result.Success -> {
                    if (result.data != null) {
                        _uiState.update {
                            it.copy(
                                currentTask = result.data,
                                totalTaskPomodorosCompleted = result.data.completedPomodoros,
                                timeLeftInMillis = getDurationMillisForType(SessionType.WORK),
                                initialDurationMillis = getDurationMillisForType(SessionType.WORK),
                                currentSessionType = SessionType.WORK,
                                timerStatus = TimerStatus.STOPPED,
                                isLoading = false,
                                workSessionsCompletedThisCycle = result.data.completedPomodoros % SESSIONS_BEFORE_LONG_BREAK
                            )
                        }
                    } else {
                        _uiState.update { it.copy(error = "Task not found", isLoading = false) }
                    }
                }
            }
        }
    }

    // New function to initiate a Pomodoro session without a pre-selected task
    fun startGenericPomodoroSession() {
        viewModelScope.launch {
            // Option B: Get or Create a default "Quick Pomodoro" task
            val genericTask =
                taskRepo.getOrCreateDefaultTask() // You'll need to implement this in TaskRepo

            currentActiveTaskId = genericTask.taskId // Set the active task ID
            _uiState.update {
                it.copy(
                    currentTask = genericTask,
                    totalTaskPomodorosCompleted = genericTask.completedPomodoros, // Likely 0 for a new/generic task
                    timeLeftInMillis = getDurationMillisForType(SessionType.WORK),
                    initialDurationMillis = getDurationMillisForType(SessionType.WORK),
                    currentSessionType = SessionType.WORK,
                    timerStatus = TimerStatus.STOPPED, // Ready to be started by user
                    isLoading = false,
                    error = null,
                    workSessionsCompletedThisCycle = 0
                )
            }
            // After setting up the state, you could auto-start or let the user click play.
            // For a bottom bar play button, usually, it means start immediately.
            startTimer()
        }
    }

    fun startTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING || _uiState.value.currentTask == null) return

        if (_uiState.value.timerStatus == TimerStatus.STOPPED || _uiState.value.timerStatus == TimerStatus.FINISHED) {
            currentSessionStartTime = System.currentTimeMillis()
            // Reset timeLeft to initial duration if starting a new session
            _uiState.update { it.copy(timeLeftInMillis = it.initialDurationMillis) }
        }
        // If PAUSED, it will resume from timeLeftInMillis

        _uiState.update { it.copy(timerStatus = TimerStatus.RUNNING, error = null) }

        timerJob?.cancel() // Cancel any existing job
        timerJob = viewModelScope.launch {
            var mutableTimeLeft = _uiState.value.timeLeftInMillis
            while (mutableTimeLeft > 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) {
                delay(1000) // Update every second
                mutableTimeLeft -= 1000
                _uiState.update { it.copy(timeLeftInMillis = mutableTimeLeft) }
            }
            if (mutableTimeLeft <= 0 && _uiState.value.timerStatus == TimerStatus.RUNNING) { // Check status again in case it was stopped/paused
                onSessionFinished()
            }
        }
    }

    fun pauseTimer() {
        if (_uiState.value.timerStatus == TimerStatus.RUNNING) {
            timerJob?.cancel()
            _uiState.update { it.copy(timerStatus = TimerStatus.PAUSED) }
        }
    }

    fun stopTimer(isInterrupted: Boolean = true) { // Also used for skip
        timerJob?.cancel()
        val previousStatus = _uiState.value.timerStatus
        _uiState.update {
            it.copy(
                timerStatus = TimerStatus.STOPPED, timeLeftInMillis = it.initialDurationMillis
            )
        }

        if (previousStatus == TimerStatus.RUNNING || previousStatus == TimerStatus.PAUSED) {
            // Only save if it was running or paused, not if already stopped or finished.
            // And if it's a WORK session and was actually running for some time.
            if (_uiState.value.currentSessionType == SessionType.WORK && isInterrupted && currentSessionStartTime > 0) {
                saveSession(status = "INTERRUPTED")
            }
        }
        // If stopping/skipping a break, don't save it as "INTERRUPTED" unless specific logic requires.
        // After stopping, decide what's next (e.g., reset to next work session or allow manual start)
        // For simplicity here, it just stops and resets the current session timer.
        // If you skip, you might want to immediately call `determineNextSessionTypeAndStart()`
    }


    private suspend fun onSessionFinished() {
        _uiState.update { it.copy(timerStatus = TimerStatus.FINISHED) }
        saveSession(status = "COMPLETED")

        var newWorkSessionsCompleted = _uiState.value.workSessionsCompletedThisCycle
        var newTotalTaskPomodoros = _uiState.value.totalTaskPomodorosCompleted

        if (_uiState.value.currentSessionType == SessionType.WORK) {
            newWorkSessionsCompleted++
            newTotalTaskPomodoros++

            _uiState.value.currentTask?.let { task ->
                val updatedTask = task.copy(completedPomodoros = newTotalTaskPomodoros)
                taskRepo.updateTask(updatedTask) // Update task in DB
                // No need to update _uiState.currentTask directly here as taskRepo.getTaskById flow should emit
            }
        }

        _uiState.update {
            it.copy(
                workSessionsCompletedThisCycle = newWorkSessionsCompleted,
                totalTaskPomodorosCompleted = newTotalTaskPomodoros
            )
        }

        determineNextSessionTypeAndStart()
    }

    private fun determineNextSessionTypeAndStart() {
        val currentTask = _uiState.value.currentTask ?: return
        if (currentTask.completedPomodoros >= currentTask.estimatedPomodoros && _uiState.value.currentSessionType == SessionType.WORK) {
            // Task completed all estimated pomodoros
            _uiState.update {
                it.copy(
                    error = "Task completed!", timerStatus = TimerStatus.STOPPED
                )
            }
            // Optionally, mark task as fully completed if not already
            if (!currentTask.isCompleted) {
                viewModelScope.launch { taskRepo.updateTask(currentTask.copy(isCompleted = true)) }
            }
            return // Stop here, user can go back or choose another task
        }


        val nextSessionType = if (_uiState.value.currentSessionType == SessionType.WORK) {
            if (_uiState.value.workSessionsCompletedThisCycle % SESSIONS_BEFORE_LONG_BREAK == 0) {
                SessionType.LONG_BREAK
            } else {
                SessionType.SHORT_BREAK
            }
        } else { // If current session was a break
            SessionType.WORK
        }

        _uiState.update {
            it.copy(
                currentSessionType = nextSessionType,
                initialDurationMillis = getDurationMillisForType(nextSessionType),
                timeLeftInMillis = getDurationMillisForType(nextSessionType),
                timerStatus = TimerStatus.STOPPED // Ready to start next session
            )
        }
        // Optionally auto-start the next session:
        // startTimer()
    }

    fun skipSession() {
        // Stop current session (don't save as "COMPLETED" or increment pomodoros for work)
        stopTimer(isInterrupted = false) // Mark as not interrupted from user perspective for saving logic
        // Determine and set up the next session type immediately
        determineNextSessionTypeAndStart()
        // Optionally, auto-start the timer for the new session
        // startTimer()
    }


    private fun saveSession(status: String) {
        val task = _uiState.value.currentTask ?: return
        val sessionType = _uiState.value.currentSessionType
        val activeTaskId = currentActiveTaskId ?: return // Guard against null taskId if not set
        val currentTaskName = _uiState.value.currentTask?.name ?: "Quick Session" // Fallback name
        val duration =
            (_uiState.value.initialDurationMillis - _uiState.value.timeLeftInMillis) / (60 * 1000) // Duration in minutes

        // Ensure session duration is meaningful, especially for interrupted work sessions
        if (sessionType == SessionType.WORK && status == "INTERRUPTED" && duration < 1) {
            // Don't save very short, immediately interrupted work sessions
            return
        }


        val session = PomodoroSession(
            associatedTaskId = activeTaskId,
            startTime = currentSessionStartTime,
            endTime = System.currentTimeMillis(),
            durationMinutes = if (status == "COMPLETED") getDurationMinutesForType(_uiState.value.currentSessionType) else (_uiState.value.initialDurationMillis - _uiState.value.timeLeftInMillis).toInt() / (60000),
            sessionType = _uiState.value.currentSessionType.name, // Store as String "WORK", "SHORT_BREAK", etc.
            status = status
        )
        currentSessionStartTime = 0L // Reset for next session

        viewModelScope.launch {
            when (val result = sessionRepo.insertSession(session)) {
                is Result.Success -> { /* Log or handle success if needed */
                }

                is Result.Error -> _uiState.update { it.copy(error = result.exception.message) }
                is Result.Loading -> { /* Optionally handle loading if insertSession is long */
                }
            }
        }
    }

    // Modify getDurationMillisForType and getDurationMinutesForType to use currentPomodoroSettings
    private fun getDurationMillisForType(type: SessionType): Long {
        val minutes = when (type) {
            SessionType.WORK -> currentPomodoroSettings.workDurationMinutes
            SessionType.SHORT_BREAK -> currentPomodoroSettings.shortBreakDurationMinutes
            SessionType.LONG_BREAK -> currentPomodoroSettings.longBreakDurationMinutes
        }
        return minutes * 60 * 1000L
    }

    private fun getDurationMinutesForType(type: SessionType): Int {
        return when (type) {
            SessionType.WORK -> currentPomodoroSettings.workDurationMinutes
            SessionType.SHORT_BREAK -> currentPomodoroSettings.shortBreakDurationMinutes
            SessionType.LONG_BREAK -> currentPomodoroSettings.longBreakDurationMinutes
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel() // Clean up coroutine
    }
}
