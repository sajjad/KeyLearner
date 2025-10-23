package com.example.keylearner.data.model

/**
 * Represents a single response time data point
 *
 * @param questionIndex The question number in the game session (0-based)
 * @param keyName The key being tested (e.g., "Em", "C")
 * @param position The position in the key (1-7)
 * @param chord The chord that was asked
 * @param isCorrect Whether the answer was correct
 * @param responseTimeSeconds Time taken to answer in seconds (1 decimal place)
 */
data class ResponseTimePoint(
    val questionIndex: Int,
    val keyName: String,
    val position: Int,
    val chord: Chord,
    val isCorrect: Boolean,
    val responseTimeSeconds: Float
)

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
 * @param responseTimes List of response time data points for each question
 */
data class GameScores(
    val keyScores: Map<String, KeyScore> = emptyMap(),
    val responseTimes: List<ResponseTimePoint> = emptyList()
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

    /**
     * Get response times filtered by key
     */
    fun getResponseTimesForKey(keyName: String): List<ResponseTimePoint> {
        return responseTimes.filter { it.keyName == keyName }
    }

    /**
     * Get response times filtered by position
     */
    fun getResponseTimesForPosition(position: Int): List<ResponseTimePoint> {
        return responseTimes.filter { it.position == position }
    }

    /**
     * Get response times filtered by key and positions
     */
    fun getResponseTimesFiltered(keyName: String, positions: Set<Int>): List<ResponseTimePoint> {
        return responseTimes.filter { it.keyName == keyName && it.position in positions }
    }

    /**
     * Calculate average response time for filtered data
     */
    fun getAverageResponseTime(filtered: List<ResponseTimePoint>): Float {
        return if (filtered.isNotEmpty()) {
            filtered.map { it.responseTimeSeconds }.average().toFloat()
        } else 0f
    }

    /**
     * Get fastest response time from filtered data
     */
    fun getFastestResponseTime(filtered: List<ResponseTimePoint>): Float {
        return filtered.minOfOrNull { it.responseTimeSeconds } ?: 0f
    }

    /**
     * Get slowest response time from filtered data
     */
    fun getSlowestResponseTime(filtered: List<ResponseTimePoint>): Float {
        return filtered.maxOfOrNull { it.responseTimeSeconds } ?: 0f
    }

    /**
     * Get average response time for correct answers
     */
    fun getAverageCorrectResponseTime(filtered: List<ResponseTimePoint>): Float {
        val correctAnswers = filtered.filter { it.isCorrect }
        return getAverageResponseTime(correctAnswers)
    }

    /**
     * Get average response time for incorrect answers
     */
    fun getAverageIncorrectResponseTime(filtered: List<ResponseTimePoint>): Float {
        val incorrectAnswers = filtered.filter { !it.isCorrect }
        return getAverageResponseTime(incorrectAnswers)
    }
}
