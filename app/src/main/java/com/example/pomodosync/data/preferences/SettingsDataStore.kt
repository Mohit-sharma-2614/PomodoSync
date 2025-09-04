package com.example.pomodosync.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance, tied to the Context's lifecycle
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "pomodoro_settings")

class PomodoroSettingsRepository(private val context: Context) {

    companion object {
        val WORK_DURATION_KEY = intPreferencesKey("work_duration_minutes")
        val SHORT_BREAK_DURATION_KEY = intPreferencesKey("short_break_duration_minutes")
        val LONG_BREAK_DURATION_KEY = intPreferencesKey("long_break_duration_minutes")

        // Default values
        const val DEFAULT_WORK_DURATION = 25
        const val DEFAULT_SHORT_BREAK_DURATION = 5
        const val DEFAULT_LONG_BREAK_DURATION = 15
    }

    val pomodoroSettingsFlow: Flow<PomodoroSettings> = context.settingsDataStore.data
        .map { preferences ->
            PomodoroSettings(
                workDurationMinutes = preferences[WORK_DURATION_KEY] ?: DEFAULT_WORK_DURATION,
                shortBreakDurationMinutes = preferences[SHORT_BREAK_DURATION_KEY] ?: DEFAULT_SHORT_BREAK_DURATION,
                longBreakDurationMinutes = preferences[LONG_BREAK_DURATION_KEY] ?: DEFAULT_LONG_BREAK_DURATION
            )
        }

    suspend fun updateWorkDuration(minutes: Int) {
        context.settingsDataStore.edit { settings ->
            settings[WORK_DURATION_KEY] = minutes
        }
    }

    suspend fun updateShortBreakDuration(minutes: Int) {
        context.settingsDataStore.edit { settings ->
            settings[SHORT_BREAK_DURATION_KEY] = minutes
        }
    }

    suspend fun updateLongBreakDuration(minutes: Int) {
        context.settingsDataStore.edit { settings ->
            settings[LONG_BREAK_DURATION_KEY] = minutes
        }
    }
}

data class PomodoroSettings(
    val workDurationMinutes: Int,
    val shortBreakDurationMinutes: Int,
    val longBreakDurationMinutes: Int
)

