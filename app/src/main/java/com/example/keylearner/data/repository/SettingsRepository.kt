package com.example.keylearner.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.keylearner.data.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for persisting and retrieving user settings using DataStore
 */
class SettingsRepository(private val context: Context) {

    companion object {
        // DataStore instance
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        // Preference keys
        private val MAJOR_KEYS = stringPreferencesKey("major_keys")
        private val MINOR_KEYS = stringPreferencesKey("minor_keys")
        private val COUNT = intPreferencesKey("count")
        private val DELAY = floatPreferencesKey("delay")
        private val LIMIT_CHOICES = booleanPreferencesKey("limit_choices")
    }

    /**
     * Flow of current settings
     */
    val settingsFlow: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            majorKeys = preferences[MAJOR_KEYS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            minorKeys = preferences[MINOR_KEYS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            count = preferences[COUNT] ?: 10,
            delay = preferences[DELAY] ?: 10f,
            limitChoices = preferences[LIMIT_CHOICES] ?: true
        )
    }

    /**
     * Save settings to DataStore
     */
    suspend fun saveSettings(settings: Settings) {
        context.dataStore.edit { preferences ->
            preferences[MAJOR_KEYS] = settings.majorKeys.joinToString(",")
            preferences[MINOR_KEYS] = settings.minorKeys.joinToString(",")
            preferences[COUNT] = settings.count
            preferences[DELAY] = settings.delay
            preferences[LIMIT_CHOICES] = settings.limitChoices
        }
    }

    /**
     * Update only the major keys
     */
    suspend fun updateMajorKeys(keys: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[MAJOR_KEYS] = keys.joinToString(",")
        }
    }

    /**
     * Update only the minor keys
     */
    suspend fun updateMinorKeys(keys: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[MINOR_KEYS] = keys.joinToString(",")
        }
    }

    /**
     * Update only the count
     */
    suspend fun updateCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[COUNT] = count
        }
    }

    /**
     * Update only the delay
     */
    suspend fun updateDelay(delay: Float) {
        context.dataStore.edit { preferences ->
            preferences[DELAY] = delay
        }
    }

    /**
     * Update only the limit choices setting
     */
    suspend fun updateLimitChoices(limitChoices: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LIMIT_CHOICES] = limitChoices
        }
    }

    /**
     * Clear all settings
     */
    suspend fun clearSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
