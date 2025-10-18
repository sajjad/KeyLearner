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
