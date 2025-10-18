package com.example.keylearner.data.model

/**
 * Represents the score for a single chord position
 *
 * @param correct Number of correct answers
 * @param wrong Number of wrong answers
 */
data class PositionScore(
    val correct: Int = 0,
    val wrong: Int = 0
) {
    /**
     * Total number of attempts
     */
    val total: Int
        get() = correct + wrong

    /**
     * Accuracy percentage (0-100)
     */
    val accuracy: Float
        get() = if (total > 0) (correct.toFloat() / total * 100) else 0f
}

/**
 * Represents scores for all positions in a single key
 *
 * @param keyName The key name (e.g., "C", "Em")
 * @param positionScores Map of position (1-7) to score
 */
data class KeyScore(
    val keyName: String,
    val positionScores: Map<Int, PositionScore> = emptyMap()
) {
    /**
     * Get the score for a specific position, or an empty score if not found
     */
    fun getScoreForPosition(position: Int): PositionScore {
        return positionScores[position] ?: PositionScore()
    }

    /**
     * Calculate overall accuracy for this key
     */
    fun getOverallAccuracy(): Float {
        val totalCorrect = positionScores.values.sumOf { it.correct }
        val totalAttempts = positionScores.values.sumOf { it.total }
        return if (totalAttempts > 0) (totalCorrect.toFloat() / totalAttempts * 100) else 0f
    }

    /**
     * Get total number of correct answers across all positions
     */
    fun getTotalCorrect(): Int = positionScores.values.sumOf { it.correct }

    /**
     * Get total number of wrong answers across all positions
     */
    fun getTotalWrong(): Int = positionScores.values.sumOf { it.wrong }
}

/**
 * Represents all scores across all keys in a game session
 *
 * @param keyScores Map of key name to KeyScore
 */
data class GameScores(
    val keyScores: Map<String, KeyScore> = emptyMap()
) {
    /**
     * Get the score for a specific key
     */
    fun getScoreForKey(keyName: String): KeyScore? {
        return keyScores[keyName]
    }

    /**
     * Get all key names that have been played
     */
    fun getPlayedKeys(): List<String> {
        return keyScores.keys.toList()
    }

    /**
     * Calculate overall accuracy across all keys
     */
    fun getOverallAccuracy(): Float {
        val totalCorrect = keyScores.values.sumOf { it.getTotalCorrect() }
        val totalAttempts = keyScores.values.sumOf {
            it.positionScores.values.sumOf { score -> score.total }
        }
        return if (totalAttempts > 0) (totalCorrect.toFloat() / totalAttempts * 100) else 0f
    }
}
