package com.example.nammasantheledger.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore-backed user preferences manager.
 * Single source of truth for app settings like dark mode, language, etc.
 */
@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val DAILY_REMINDER_KEY = booleanPreferencesKey("daily_reminder")
        val REMINDER_LANGUAGE_KEY = stringPreferencesKey("reminder_language")
    }

    // ── Dark Mode ────────────────────────────────────────────────────────────

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    // ── Dynamic Color ────────────────────────────────────────────────────────

    val isDynamicColor: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DYNAMIC_COLOR_KEY] ?: true
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    // ── Daily Reminder ───────────────────────────────────────────────────────

    val isDailyReminderEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DAILY_REMINDER_KEY] ?: false
    }

    suspend fun setDailyReminder(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DAILY_REMINDER_KEY] = enabled
        }
    }

    // ── Reminder Language ────────────────────────────────────────────────────

    val reminderLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[REMINDER_LANGUAGE_KEY] ?: "KANNADA"
    }

    suspend fun setReminderLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[REMINDER_LANGUAGE_KEY] = language
        }
    }
}
