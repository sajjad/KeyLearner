package com.example.keylearner.data.model

/**
 * Represents a single game session with timestamp
 *
 * @param timestamp Unix timestamp (milliseconds) when the game was completed
 * @param scores The GameScores from this session
 */
data class GameSession(
    val timestamp: Long = System.currentTimeMillis(),
    val scores: GameScores = GameScores()
)

/**
 * View mode for the Score Screen
 */
enum class ViewMode {
    /**
     * Show scores from the current/most recent game session only
     */
    CURRENT_GAME,

    /**
     * Show cumulative scores across all historical game sessions
     */
    ALL_TIME
}

/**
 * Progress data point for a specific position across game sessions
 *
 * @param timestamp Unix timestamp when the session occurred
 * @param correct Number of correct answers for this position in this session
 * @param wrong Number of wrong answers for this position in this session
 * @param accuracy Calculated accuracy percentage (0-100)
 * @param sessionIndex Ordinal session number (1, 2, 3, ...) for X-axis display
 */
data class PositionProgressPoint(
    val timestamp: Long,
    val correct: Int,
    val wrong: Int,
    val accuracy: Float,
    val sessionIndex: Int
)

/**
 * Statistics about progress for a specific position
 *
 * @param firstAccuracy Accuracy from the first session
 * @param latestAccuracy Accuracy from the most recent session
 * @param improvementPercentage Percentage point improvement (can be negative)
 * @param bestAccuracy Highest accuracy achieved
 * @param bestSessionIndex Session number where best accuracy was achieved
 * @param trend Overall trend: "improving", "declining", or "stable"
 * @param encouragementMessage Motivational message based on performance
 */
data class ProgressStats(
    val firstAccuracy: Float,
    val latestAccuracy: Float,
    val improvementPercentage: Float,
    val bestAccuracy: Float,
    val bestSessionIndex: Int,
    val trend: String,
    val encouragementMessage: String
)
