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

    private val _selectedPositions = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPositions: StateFlow<Set<Int>> = _selectedPositions.asStateFlow()

    private val _progressData = MutableStateFlow<Map<Int, List<PositionProgressPoint>>>(emptyMap())
    val progressData: StateFlow<Map<Int, List<PositionProgressPoint>>> = _progressData.asStateFlow()

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
     * Set view mode to All Time (used when accessing history from Start Screen)
     */
    fun setViewModeToAllTime() {
        _viewMode.value = ViewMode.ALL_TIME
        // Load cumulative stats and all-time keys
        loadCumulativeStats()
        viewModelScope.launch {
            val keys = scoreRepository.getAllKeysPlayed().toList().sorted()
            keys.firstOrNull()?.let { key ->
                selectKey(key)
            }
        }
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

    /**
     * Toggle a position for viewing progress data
     */
    fun togglePosition(position: Int) {
        val current = _selectedPositions.value
        _selectedPositions.value = if (position in current) {
            current - position  // Remove if already selected
        } else {
            current + position  // Add if not selected
        }
        loadProgressData()
    }

    /**
     * Clear position selection
     */
    fun clearPositionSelection() {
        _selectedPositions.value = emptySet()
        _progressData.value = emptyMap()
    }

    /**
     * Load progress data for selected key and all selected positions
     */
    private fun loadProgressData() {
        val key = _selectedKey.value ?: return
        val positions = _selectedPositions.value

        if (positions.isEmpty()) {
            _progressData.value = emptyMap()
            return
        }

        viewModelScope.launch {
            val dataMap = mutableMapOf<Int, List<PositionProgressPoint>>()
            positions.forEach { position ->
                val data = scoreRepository.getProgressDataForPosition(key, position)
                dataMap[position] = data
            }
            _progressData.value = dataMap
        }
    }

    /**
     * Calculate progress statistics from progress data
     */
    private fun calculateProgressStats(data: List<PositionProgressPoint>): ProgressStats? {
        if (data.isEmpty()) return null

        val firstAccuracy = data.first().accuracy
        val latestAccuracy = data.last().accuracy
        val improvement = latestAccuracy - firstAccuracy

        val bestPoint = data.maxByOrNull { it.accuracy }
        val bestAccuracy = bestPoint?.accuracy ?: 0f
        val bestSessionIndex = bestPoint?.sessionIndex ?: 1

        // Calculate trend
        val trend = when {
            improvement > 10f -> "improving"
            improvement < -10f -> "declining"
            else -> "stable"
        }

        // Generate encouragement message
        val encouragementMessage = when {
            latestAccuracy >= 90f -> "Outstanding! You've mastered this chord!"
            improvement > 20f -> "Excellent progress! Keep up the great work!"
            improvement > 10f -> "Great improvement! You're getting better!"
            improvement > 0f -> "Nice work! Keep practising!"
            improvement == 0f -> "Consistent performance! Try to improve further."
            improvement > -10f -> "Keep practising - you'll improve!"
            else -> "Don't give up! Practice makes perfect."
        }

        return ProgressStats(
            firstAccuracy = firstAccuracy,
            latestAccuracy = latestAccuracy,
            improvementPercentage = improvement,
            bestAccuracy = bestAccuracy,
            bestSessionIndex = bestSessionIndex,
            trend = trend,
            encouragementMessage = encouragementMessage
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
