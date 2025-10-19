package com.example.keylearner.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.keylearner.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for persisting and retrieving historical game scores
 */
class ScoreRepository(private val context: Context) {

    companion object {
        private val Context.scoreDataStore: DataStore<Preferences> by preferencesDataStore(name = "scores")
        private val GAME_SESSIONS = stringPreferencesKey("game_sessions")
        private const val MAX_SESSIONS = 100 // Keep last 100 game sessions
    }

    /**
     * Save a game session
     */
    suspend fun saveGameSession(scores: GameScores) {
        context.scoreDataStore.edit { preferences ->
            val sessionsJson = preferences[GAME_SESSIONS] ?: "[]"
            val sessions = parseGameSessions(sessionsJson).toMutableList()

            // Add new session
            sessions.add(GameSession(System.currentTimeMillis(), scores))

            // Keep only the most recent sessions
            val trimmedSessions = if (sessions.size > MAX_SESSIONS) {
                sessions.sortedByDescending { it.timestamp }.take(MAX_SESSIONS)
            } else {
                sessions
            }

            // Save back as JSON
            preferences[GAME_SESSIONS] = serializeGameSessions(trimmedSessions)
        }
    }

    /**
     * Get all historical game sessions
     */
    val allGameSessions: Flow<List<GameSession>> = context.scoreDataStore.data.map { preferences ->
        val sessionsJson = preferences[GAME_SESSIONS] ?: "[]"
        parseGameSessions(sessionsJson)
    }

    /**
     * Get cumulative scores for a specific key across all sessions
     */
    suspend fun getCumulativeScoresForKey(keyName: String): Map<Int, PositionScore> {
        val sessionsJson = context.scoreDataStore.data.map { it[GAME_SESSIONS] ?: "[]" }
        val sessions = parseGameSessions(sessionsJson.first())

        val cumulativeScores = mutableMapOf<Int, PositionScore>()

        sessions.forEach { session ->
            session.scores.getScoreForKey(keyName)?.positionScores?.forEach { (position, score) ->
                val current = cumulativeScores[position] ?: PositionScore()
                cumulativeScores[position] = PositionScore(
                    correct = current.correct + score.correct,
                    wrong = current.wrong + score.wrong
                )
            }
        }

        return cumulativeScores
    }

    /**
     * Get all keys ever played across all sessions
     */
    suspend fun getAllKeysPlayed(): Set<String> {
        val sessionsJson = context.scoreDataStore.data.map { it[GAME_SESSIONS] ?: "[]" }
        val sessions = parseGameSessions(sessionsJson.first())

        return sessions.flatMap { it.scores.getPlayedKeys() }.toSet()
    }

    /**
     * Get cumulative statistics across all sessions
     */
    suspend fun getCumulativeStatistics(): CumulativeStats {
        val sessionsJson = context.scoreDataStore.data.map { it[GAME_SESSIONS] ?: "[]" }
        val sessions = parseGameSessions(sessionsJson.first())

        var totalCorrect = 0
        var totalWrong = 0
        val keyPracticeCount = mutableMapOf<String, Int>()

        sessions.forEach { session ->
            session.scores.keyScores.values.forEach { keyScore ->
                totalCorrect += keyScore.getTotalCorrect()
                totalWrong += keyScore.getTotalWrong()

                val count = keyPracticeCount[keyScore.keyName] ?: 0
                keyPracticeCount[keyScore.keyName] = count + 1
            }
        }

        val totalAttempts = totalCorrect + totalWrong
        val accuracy = if (totalAttempts > 0) {
            (totalCorrect.toFloat() / totalAttempts * 100)
        } else {
            0f
        }

        val mostPractisedKeys = keyPracticeCount.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        return CumulativeStats(
            totalGamesPlayed = sessions.size,
            totalCorrect = totalCorrect,
            totalWrong = totalWrong,
            overallAccuracy = accuracy,
            mostPractisedKeys = mostPractisedKeys
        )
    }

    /**
     * Get progress data for a specific key and position across all sessions
     *
     * @param keyName The key to track (e.g., "C", "Em")
     * @param position The position in the scale (1-7)
     * @return List of progress points showing accuracy trend over time
     */
    suspend fun getProgressDataForPosition(keyName: String, position: Int): List<PositionProgressPoint> {
        val sessionsJson = context.scoreDataStore.data.map { it[GAME_SESSIONS] ?: "[]" }
        val allSessions = parseGameSessions(sessionsJson.first())

        // Filter sessions that contain this key, sorted by timestamp
        val sessionsWithKey = allSessions
            .filter { session -> session.scores.getScoreForKey(keyName) != null }
            .sortedBy { it.timestamp }

        // Map to progress points
        return sessionsWithKey.mapIndexed { index, session ->
            val keyScore = session.scores.getScoreForKey(keyName)!!
            val positionScore = keyScore.getScoreForPosition(position)

            val total = positionScore.correct + positionScore.wrong
            val accuracy = if (total > 0) {
                (positionScore.correct.toFloat() / total * 100)
            } else {
                0f
            }

            PositionProgressPoint(
                timestamp = session.timestamp,
                correct = positionScore.correct,
                wrong = positionScore.wrong,
                accuracy = accuracy,
                sessionIndex = index + 1
            )
        }
    }

    /**
     * Export all game sessions to CSV format
     *
     * @return CSV string with format: Timestamp,Key,Position,Correct,Wrong
     */
    suspend fun exportToCSV(): String {
        val sessionsJson = context.scoreDataStore.data.map { it[GAME_SESSIONS] ?: "[]" }
        val sessions = parseGameSessions(sessionsJson.first())

        if (sessions.isEmpty()) {
            return "Timestamp,Key,Position,Correct,Wrong\n"
        }

        val csv = StringBuilder()
        csv.append("Timestamp,Key,Position,Correct,Wrong\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        sessions.forEach { session ->
            val timestampStr = dateFormat.format(Date(session.timestamp))

            session.scores.keyScores.forEach { (keyName, keyScore) ->
                keyScore.positionScores.forEach { (position, positionScore) ->
                    csv.append("$timestampStr,$keyName,$position,${positionScore.correct},${positionScore.wrong}\n")
                }
            }
        }

        return csv.toString()
    }

    /**
     * Import game sessions from CSV format and merge with existing data
     *
     * @param csvContent The CSV string to import
     * @return Result indicating success or error message
     */
    suspend fun importFromCSV(csvContent: String): Result<Unit> {
        return try {
            // Parse CSV
            val lines = csvContent.trim().split("\n")

            if (lines.isEmpty()) {
                return Result.failure(Exception("CSV file is empty"))
            }

            // Validate header
            val header = lines[0].trim()
            if (header != "Timestamp,Key,Position,Correct,Wrong") {
                return Result.failure(Exception("Invalid CSV format - incorrect header"))
            }

            if (lines.size < 2) {
                return Result.failure(Exception("No score data found in file"))
            }

            // Parse data rows
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            // Group by timestamp to reconstruct GameSessions
            val sessionMap = mutableMapOf<Long, MutableMap<String, MutableMap<Int, PositionScore>>>()

            lines.drop(1).forEachIndexed { index, line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) return@forEachIndexed

                val parts = trimmedLine.split(",")
                if (parts.size != 5) {
                    return Result.failure(Exception("Invalid CSV format at line ${index + 2} - expected 5 columns, found ${parts.size}"))
                }

                try {
                    val timestamp = dateFormat.parse(parts[0])?.time
                        ?: return Result.failure(Exception("Invalid timestamp at line ${index + 2}"))
                    val keyName = parts[1].trim()
                    val position = parts[2].trim().toIntOrNull()
                        ?: return Result.failure(Exception("Invalid position at line ${index + 2}"))
                    val correct = parts[3].trim().toIntOrNull()
                        ?: return Result.failure(Exception("Invalid correct count at line ${index + 2}"))
                    val wrong = parts[4].trim().toIntOrNull()
                        ?: return Result.failure(Exception("Invalid wrong count at line ${index + 2}"))

                    // Validate ranges
                    if (position !in 1..7) {
                        return Result.failure(Exception("Position must be 1-7 at line ${index + 2}"))
                    }
                    if (correct < 0 || wrong < 0) {
                        return Result.failure(Exception("Scores cannot be negative at line ${index + 2}"))
                    }

                    // Build session structure
                    val keyMap = sessionMap.getOrPut(timestamp) { mutableMapOf() }
                    val posMap = keyMap.getOrPut(keyName) { mutableMapOf() }
                    posMap[position] = PositionScore(correct, wrong)

                } catch (e: Exception) {
                    return Result.failure(Exception("Error parsing line ${index + 2}: ${e.message}"))
                }
            }

            // Convert to GameSessions
            val importedSessions = sessionMap.map { (timestamp, keyMap) ->
                val keyScores = keyMap.map { (keyName, posMap) ->
                    keyName to KeyScore(keyName, posMap)
                }.toMap()
                GameSession(timestamp, GameScores(keyScores))
            }

            // Merge with existing sessions
            context.scoreDataStore.edit { preferences ->
                val existingJson = preferences[GAME_SESSIONS] ?: "[]"
                val existingSessions = parseGameSessions(existingJson).toMutableList()

                // Create a set of existing timestamps to avoid duplicates
                val existingTimestamps = existingSessions.map { it.timestamp }.toSet()

                // Add only new sessions (not already present)
                val newSessions = importedSessions.filter { it.timestamp !in existingTimestamps }
                existingSessions.addAll(newSessions)

                // Sort by timestamp and keep only most recent MAX_SESSIONS
                val trimmedSessions = existingSessions
                    .sortedByDescending { it.timestamp }
                    .take(MAX_SESSIONS)

                preferences[GAME_SESSIONS] = serializeGameSessions(trimmedSessions)
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception("Import failed: ${e.message}"))
        }
    }

    /**
     * Clear all historical scores
     */
    suspend fun clearHistory() {
        context.scoreDataStore.edit { preferences ->
            preferences.remove(GAME_SESSIONS)
        }
    }

    // JSON serialization helpers

    private fun serializeGameSessions(sessions: List<GameSession>): String {
        val jsonArray = JSONArray()
        sessions.forEach { session ->
            val sessionObj = JSONObject()
            sessionObj.put("timestamp", session.timestamp)
            sessionObj.put("scores", serializeGameScores(session.scores))
            jsonArray.put(sessionObj)
        }
        return jsonArray.toString()
    }

    private fun serializeGameScores(scores: GameScores): JSONObject {
        val scoresObj = JSONObject()
        scores.keyScores.forEach { (keyName, keyScore) ->
            val keyScoreObj = JSONObject()
            keyScore.positionScores.forEach { (position, positionScore) ->
                val posObj = JSONObject()
                posObj.put("correct", positionScore.correct)
                posObj.put("wrong", positionScore.wrong)
                keyScoreObj.put(position.toString(), posObj)
            }
            scoresObj.put(keyName, keyScoreObj)
        }
        return scoresObj
    }

    private fun parseGameSessions(json: String): List<GameSession> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val sessionObj = jsonArray.getJSONObject(i)
                GameSession(
                    timestamp = sessionObj.getLong("timestamp"),
                    scores = parseGameScores(sessionObj.getJSONObject("scores"))
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseGameScores(scoresObj: JSONObject): GameScores {
        val keyScores = mutableMapOf<String, KeyScore>()

        scoresObj.keys().forEach { keyName ->
            val keyScoreObj = scoresObj.getJSONObject(keyName)
            val positionScores = mutableMapOf<Int, PositionScore>()

            keyScoreObj.keys().forEach { positionStr ->
                val position = positionStr.toInt()
                val posObj = keyScoreObj.getJSONObject(positionStr)
                positionScores[position] = PositionScore(
                    correct = posObj.getInt("correct"),
                    wrong = posObj.getInt("wrong")
                )
            }

            keyScores[keyName] = KeyScore(keyName, positionScores)
        }

        return GameScores(keyScores)
    }
}

/**
 * Cumulative statistics across all game sessions
 */
data class CumulativeStats(
    val totalGamesPlayed: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val overallAccuracy: Float = 0f,
    val mostPractisedKeys: List<String> = emptyList()
)
