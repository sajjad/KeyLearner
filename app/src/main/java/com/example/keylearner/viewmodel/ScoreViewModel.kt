package com.example.keylearner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keylearner.data.MusicTheory
import com.example.keylearner.data.model.*
import com.example.keylearner.data.repository.CumulativeStats
import com.example.keylearner.data.repository.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Score Screen
 *
 * Manages current game scores and historical cumulative scores.
 * Provides chart data and statistics for both view modes.
 */
class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val scoreRepository = ScoreRepository(application)

    private val _currentGameScores = MutableStateFlow<GameScores?>(null)
    val currentGameScores: StateFlow<GameScores?> = _currentGameScores.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.CURRENT_GAME)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _selectedKey = MutableStateFlow<String?>(null)
    val selectedKey: StateFlow<String?> = _selectedKey.asStateFlow()

    private val _cumulativeStats = MutableStateFlow<CumulativeStats?>(null)
    val cumulativeStats: StateFlow<CumulativeStats?> = _cumulativeStats.asStateFlow()

    private val _chartData = MutableStateFlow<List<ChartBarData>>(emptyList())
    val chartData: StateFlow<List<ChartBarData>> = _chartData.asStateFlow()

    /**
     * Initialize with current game scores
     */
    fun setCurrentGameScores(scores: GameScores) {
        _currentGameScores.value = scores

        // Save to history
        viewModelScope.launch {
            scoreRepository.saveGameSession(scores)
            loadCumulativeStats()
        }

        // Auto-select first key
        val firstKey = scores.getPlayedKeys().firstOrNull()
        if (firstKey != null) {
            selectKey(firstKey)
        }
    }

    /**
     * Toggle between Current Game and All Time view modes
     */
    fun toggleViewMode() {
        _viewMode.value = when (_viewMode.value) {
            ViewMode.CURRENT_GAME -> {
                ViewMode.ALL_TIME
            }
            ViewMode.ALL_TIME -> ViewMode.CURRENT_GAME
        }

        // Reload chart data for new mode
        _selectedKey.value?.let { selectKey(it) }
    }

    /**
     * Select a key to display in the chart
     */
    fun selectKey(keyName: String) {
        _selectedKey.value = keyName
        updateChartData(keyName)
    }

    /**
     * Get available keys for the current view mode
     */
    fun getAvailableKeys(): List<String> {
        return when (_viewMode.value) {
            ViewMode.CURRENT_GAME -> _currentGameScores.value?.getPlayedKeys() ?: emptyList()
            ViewMode.ALL_TIME -> {
                // This will be loaded asynchronously
                emptyList() // Will be updated via getAllTimeKeys()
            }
        }
    }

    /**
     * Load all-time keys asynchronously
     */
    fun loadAllTimeKeys(onKeysLoaded: (List<String>) -> Unit) {
        viewModelScope.launch {
            val keys = scoreRepository.getAllKeysPlayed().toList().sorted()
            onKeysLoaded(keys)
        }
    }

    /**
     * Update chart data based on selected key and view mode
     */
    private fun updateChartData(keyName: String) {
        viewModelScope.launch {
            val data = when (_viewMode.value) {
                ViewMode.CURRENT_GAME -> {
                    generateCurrentGameChartData(keyName)
                }
                ViewMode.ALL_TIME -> {
                    generateAllTimeChartData(keyName)
                }
            }
            _chartData.value = data
        }
    }

    /**
     * Generate chart data for current game
     */
    private fun generateCurrentGameChartData(keyName: String): List<ChartBarData> {
        val keyScore = _currentGameScores.value?.getScoreForKey(keyName) ?: return emptyList()

        // Determine if this is a minor key
        val isMinor = keyName.endsWith("m")
        val rootKey = if (isMinor) keyName.dropLast(1) else keyName

        return (1..7).map { position ->
            val positionScore = keyScore.getScoreForPosition(position)
            val chord = MusicTheory.getChord(rootKey, isMinor, position)
            val label = "$position-${chord.displayName()}"

            ChartBarData(
                label = label,
                correct = positionScore.correct,
                wrong = positionScore.wrong
            )
        }
    }

    /**
     * Generate chart data for all-time cumulative scores
     */
    private suspend fun generateAllTimeChartData(keyName: String): List<ChartBarData> {
        val cumulativeScores = scoreRepository.getCumulativeScoresForKey(keyName)

        // Determine if this is a minor key
        val isMinor = keyName.endsWith("m")
        val rootKey = if (isMinor) keyName.dropLast(1) else keyName

        return (1..7).map { position ->
            val positionScore = cumulativeScores[position] ?: PositionScore()
            val chord = MusicTheory.getChord(rootKey, isMinor, position)
            val label = "$position-${chord.displayName()}"

            ChartBarData(
                label = label,
                correct = positionScore.correct,
                wrong = positionScore.wrong
            )
        }
    }

    /**
     * Load cumulative statistics
     */
    private fun loadCumulativeStats() {
        viewModelScope.launch {
            val stats = scoreRepository.getCumulativeStatistics()
            _cumulativeStats.value = stats
        }
    }

    /**
     * Get current game statistics
     */
    fun getCurrentGameStats(): GameStats? {
        val scores = _currentGameScores.value ?: return null

        var totalCorrect = 0
        var totalWrong = 0

        scores.keyScores.values.forEach { keyScore ->
            totalCorrect += keyScore.getTotalCorrect()
            totalWrong += keyScore.getTotalWrong()
        }

        val totalAttempts = totalCorrect + totalWrong
        val accuracy = if (totalAttempts > 0) {
            (totalCorrect.toFloat() / totalAttempts * 100)
        } else {
            0f
        }

        return GameStats(
            totalQuestions = totalAttempts,
            correctAnswers = totalCorrect,
            wrongAnswers = totalWrong,
            accuracy = accuracy
        )
    }
}

/**
 * Chart data for a single bar (position)
 */
data class ChartBarData(
    val label: String,           // e.g., "1-C", "2-Dm"
    val correct: Int,
    val wrong: Int
) {
    val total: Int get() = correct + wrong
}

/**
 * Current game statistics
 */
data class GameStats(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val accuracy: Float
)
