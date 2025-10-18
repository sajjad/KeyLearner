package com.example.keylearner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keylearner.data.model.Settings
import com.example.keylearner.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Start Screen
 *
 * Manages game settings and provides methods to update them.
 * Settings are persisted using DataStore and restored when the app restarts.
 */
class StartScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    init {
        // Load persisted settings on initialization
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { loadedSettings ->
                _settings.value = loadedSettings
            }
        }
    }

    /**
     * Toggle a major key selection
     */
    fun toggleMajorKey(key: String) {
        val currentKeys = _settings.value.majorKeys
        val updatedKeys = if (currentKeys.contains(key)) {
            currentKeys - key
        } else {
            currentKeys + key
        }
        updateMajorKeys(updatedKeys)
    }

    /**
     * Toggle a minor key selection
     */
    fun toggleMinorKey(key: String) {
        val currentKeys = _settings.value.minorKeys
        val updatedKeys = if (currentKeys.contains(key)) {
            currentKeys - key
        } else {
            currentKeys + key
        }
        updateMinorKeys(updatedKeys)
    }

    /**
     * Update major keys and persist
     */
    private fun updateMajorKeys(keys: List<String>) {
        _settings.value = _settings.value.copy(majorKeys = keys)
        viewModelScope.launch {
            settingsRepository.updateMajorKeys(keys)
        }
    }

    /**
     * Update minor keys and persist
     */
    private fun updateMinorKeys(keys: List<String>) {
        _settings.value = _settings.value.copy(minorKeys = keys)
        viewModelScope.launch {
            settingsRepository.updateMinorKeys(keys)
        }
    }

    /**
     * Update the count setting
     */
    fun updateCount(count: Int) {
        _settings.value = _settings.value.copy(count = count)
        viewModelScope.launch {
            settingsRepository.updateCount(count)
        }
    }

    /**
     * Update the delay setting
     */
    fun updateDelay(delay: Float) {
        _settings.value = _settings.value.copy(delay = delay)
        viewModelScope.launch {
            settingsRepository.updateDelay(delay)
        }
    }

    /**
     * Update the limit choices setting
     */
    fun updateLimitChoices(limitChoices: Boolean) {
        _settings.value = _settings.value.copy(limitChoices = limitChoices)
        viewModelScope.launch {
            settingsRepository.updateLimitChoices(limitChoices)
        }
    }

    /**
     * Validate settings before starting the game
     *
     * @return true if at least one key is selected, false otherwise
     */
    fun canStartGame(): Boolean {
        return _settings.value.hasKeysSelected()
    }
}
