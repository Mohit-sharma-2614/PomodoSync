package com.example.pomodosync.persentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodosync.data.local.entity.PomodoroSession
import com.example.pomodosync.data.local.entity.Task
import com.example.pomodosync.data.repo.PomodoroSessionRepo
import com.example.pomodosync.data.repo.TaskRepo
import com.example.pomodosync.resources.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val taskInput: String = "",
    val sessionCount: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTask: Task? = null,
    val selectedPomodoroSession: PomodoroSession? = null
)


@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepo: TaskRepo, private val sessionRepo: PomodoroSessionRepo
) : ViewModel() {


    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState

    private fun generateTaskModelFromCurrentState(): Task { // Renamed for clarity
        return Task(
            taskId = _uiState.value.selectedTask?.taskId ?: 0,
            name = _uiState.value.taskInput, // Use _uiState for current value
            creationDate = System.currentTimeMillis(),
            isCompleted = false,
            estimatedPomodoros = _uiState.value.sessionCount, // Use _uiState for current value
            completedPomodoros = 0,
        )
    }

    fun getAllTasks() {
        viewModelScope.launch {
            taskRepo.getAllTasks().onStart {
                    _uiState.update { it.copy(isLoading = true) }
                }.catch { exception ->
                    _uiState.update { it.copy(error = exception.message, isLoading = false) }
                }.collect { tasks ->
                    // This is crucial: This collect block should automatically update
                    // the UI when new tasks are inserted if taskRepo.getAllTasks()
                    // returns a Flow from Room that observes changes.
                    _uiState.update { it.copy(tasks = tasks, isLoading = false) }
                }
        }
    }

    fun startEditingTask(task: Task) {
        _uiState.update {
            it.copy(
                selectedTask = task, taskInput = task.name, sessionCount = task.estimatedPomodoros
            )
        }
    }

    fun clearEditingState() {
        _uiState.update {
            it.copy(
                selectedTask = null, taskInput = "", sessionCount = 1
            )
        }
    }

    fun saveTask() {
        // Create the Task object HERE, using the current uiState.value
        val taskToSave = generateTaskModelFromCurrentState()

        // Optional: Basic validation before inserting
        if (taskToSave.name.isBlank()) {
            _uiState.update { it.copy(error = "Task name cannot be empty") }
            return
        }

        viewModelScope.launch {
            // Use the newly created taskToInsert

            val result = if (_uiState.value.selectedTask != null) {
                taskRepo.updateTask(taskToSave)
            } else {
                taskRepo.insertTask(taskToSave)
            }

            when (result) {
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }

                is Result.Success -> {
                    // Clear the input fields in the UI state after successful insertion
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedTask = null,
                            taskInput = "",       // Reset task input
                            sessionCount = 1,     // Reset session count
                            error = null          // Clear any previous error
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(error = result.exception.message, isLoading = false) }
                }
            }
        }
    }

    // Function to update the completion status directly from TaskComponent
    fun updateTaskCompletion(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = isCompleted)
            // Optionally, update completedPomodoros if logic dictates
            // For example, if completing a task means all pomodoros are done:
            // val finalUpdatedTask = if (isCompleted) updatedTask.copy(completedPomodoros = updatedTask.estimatedPomodoros) else updatedTask
            taskRepo.updateTask(updatedTask)
            // UI will update via the getAllTasks flow collection
        }
    }

    fun updateTaskInput(input: String) {
        _uiState.update { it.copy(taskInput = input) }
    }

    fun updateSessionCount(count: Int) {
        _uiState.update { it.copy(sessionCount = count) }
    }

    init {
        getAllTasks()
    }

}