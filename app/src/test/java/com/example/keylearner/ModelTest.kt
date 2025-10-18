package com.example.keylearner

import com.example.keylearner.data.model.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for data model classes (Score, Settings, GameState, etc.)
 */
class ModelTest {

    // ========================================
    // PositionScore Tests
    // ========================================

    @Test
    fun testPositionScore_DefaultValues() {
        val score = PositionScore()
        assertEquals(0, score.correct)
        assertEquals(0, score.wrong)
        assertEquals(0, score.total)
        assertEquals(0f, score.accuracy, 0.01f)
    }

    @Test
    fun testPositionScore_TotalCalculation() {
        val score = PositionScore(correct = 7, wrong = 3)
        assertEquals(10, score.total)
    }

    @Test
    fun testPositionScore_AccuracyCalculation_AllCorrect() {
        val score = PositionScore(correct = 10, wrong = 0)
        assertEquals(100f, score.accuracy, 0.01f)
    }

    @Test
    fun testPositionScore_AccuracyCalculation_AllWrong() {
        val score = PositionScore(correct = 0, wrong = 10)
        assertEquals(0f, score.accuracy, 0.01f)
    }

    @Test
    fun testPositionScore_AccuracyCalculation_Mixed() {
        val score = PositionScore(correct = 7, wrong = 3)
        assertEquals(70f, score.accuracy, 0.01f)
    }

    @Test
    fun testPositionScore_AccuracyCalculation_Percentage() {
        val score = PositionScore(correct = 3, wrong = 7)
        assertEquals(30f, score.accuracy, 0.01f)
    }

    @Test
    fun testPositionScore_AccuracyCalculation_EmptyScore() {
        val score = PositionScore(correct = 0, wrong = 0)
        assertEquals(0f, score.accuracy, 0.01f)
    }

    // ========================================
    // KeyScore Tests
    // ========================================

    @Test
    fun testKeyScore_DefaultValues() {
        val keyScore = KeyScore("C")
        assertEquals("C", keyScore.keyName)
        assertTrue(keyScore.positionScores.isEmpty())
    }

    @Test
    fun testKeyScore_GetScoreForPosition_Exists() {
        val scores = mapOf(
            1 to PositionScore(5, 2),
            2 to PositionScore(3, 4)
        )
        val keyScore = KeyScore("C", scores)

        val score = keyScore.getScoreForPosition(1)
        assertEquals(5, score.correct)
        assertEquals(2, score.wrong)
    }

    @Test
    fun testKeyScore_GetScoreForPosition_NotExists() {
        val keyScore = KeyScore("C", mapOf())
        val score = keyScore.getScoreForPosition(5)

        // Should return empty score
        assertEquals(0, score.correct)
        assertEquals(0, score.wrong)
    }

    @Test
    fun testKeyScore_OverallAccuracy_AllCorrect() {
        val scores = mapOf(
            1 to PositionScore(10, 0),
            2 to PositionScore(10, 0),
            3 to PositionScore(10, 0)
        )
        val keyScore = KeyScore("C", scores)

        assertEquals(100f, keyScore.getOverallAccuracy(), 0.01f)
    }

    @Test
    fun testKeyScore_OverallAccuracy_Mixed() {
        val scores = mapOf(
            1 to PositionScore(7, 3),   // 70%
            2 to PositionScore(8, 2),   // 80%
            3 to PositionScore(6, 4)    // 60%
        )
        val keyScore = KeyScore("C", scores)

        // Total: 21 correct out of 30 = 70%
        assertEquals(70f, keyScore.getOverallAccuracy(), 0.01f)
    }

    @Test
    fun testKeyScore_TotalCorrect() {
        val scores = mapOf(
            1 to PositionScore(7, 3),
            2 to PositionScore(5, 5),
            3 to PositionScore(8, 2)
        )
        val keyScore = KeyScore("C", scores)

        assertEquals(20, keyScore.getTotalCorrect())
    }

    @Test
    fun testKeyScore_TotalWrong() {
        val scores = mapOf(
            1 to PositionScore(7, 3),
            2 to PositionScore(5, 5),
            3 to PositionScore(8, 2)
        )
        val keyScore = KeyScore("C", scores)

        assertEquals(10, keyScore.getTotalWrong())
    }

    // ========================================
    // GameScores Tests
    // ========================================

    @Test
    fun testGameScores_DefaultValues() {
        val gameScores = GameScores()
        assertTrue(gameScores.keyScores.isEmpty())
        assertTrue(gameScores.getPlayedKeys().isEmpty())
    }

    @Test
    fun testGameScores_GetScoreForKey_Exists() {
        val keyScore = KeyScore("C", mapOf(1 to PositionScore(5, 2)))
        val gameScores = GameScores(mapOf("C" to keyScore))

        val retrieved = gameScores.getScoreForKey("C")
        assertNotNull(retrieved)
        assertEquals("C", retrieved?.keyName)
    }

    @Test
    fun testGameScores_GetScoreForKey_NotExists() {
        val gameScores = GameScores()
        val retrieved = gameScores.getScoreForKey("C")
        assertNull(retrieved)
    }

    @Test
    fun testGameScores_GetPlayedKeys() {
        val gameScores = GameScores(
            mapOf(
                "C" to KeyScore("C"),
                "Em" to KeyScore("Em"),
                "G" to KeyScore("G")
            )
        )

        val playedKeys = gameScores.getPlayedKeys()
        assertEquals(3, playedKeys.size)
        assertTrue(playedKeys.contains("C"))
        assertTrue(playedKeys.contains("Em"))
        assertTrue(playedKeys.contains("G"))
    }

    @Test
    fun testGameScores_OverallAccuracy_SingleKey() {
        val keyScore = KeyScore(
            "C",
            mapOf(
                1 to PositionScore(7, 3),
                2 to PositionScore(8, 2)
            )
        )
        val gameScores = GameScores(mapOf("C" to keyScore))

        // Total: 15 correct out of 20 = 75%
        assertEquals(75f, gameScores.getOverallAccuracy(), 0.01f)
    }

    @Test
    fun testGameScores_OverallAccuracy_MultipleKeys() {
        val cScore = KeyScore("C", mapOf(1 to PositionScore(8, 2)))  // 8/10 = 80%
        val emScore = KeyScore("Em", mapOf(1 to PositionScore(6, 4)))  // 6/10 = 60%
        val gameScores = GameScores(mapOf("C" to cScore, "Em" to emScore))

        // Total: 14 correct out of 20 = 70%
        assertEquals(70f, gameScores.getOverallAccuracy(), 0.01f)
    }

    @Test
    fun testGameScores_OverallAccuracy_EmptyScores() {
        val gameScores = GameScores()
        assertEquals(0f, gameScores.getOverallAccuracy(), 0.01f)
    }

    // ========================================
    // Settings Tests
    // ========================================

    @Test
    fun testSettings_DefaultValues() {
        val settings = Settings()
        assertTrue(settings.majorKeys.isEmpty())
        assertTrue(settings.minorKeys.isEmpty())
        assertEquals(10, settings.count)
        assertEquals(10f, settings.delay, 0.01f)
        assertTrue(settings.limitChoices)
    }

    @Test
    fun testSettings_GetAllSelectedKeys_Empty() {
        val settings = Settings()
        assertTrue(settings.getAllSelectedKeys().isEmpty())
    }

    @Test
    fun testSettings_GetAllSelectedKeys_OnlyMajor() {
        val settings = Settings(majorKeys = listOf("C", "G", "D"))
        val keys = settings.getAllSelectedKeys()

        assertEquals(3, keys.size)
        assertEquals(Pair("C", false), keys[0])
        assertEquals(Pair("G", false), keys[1])
        assertEquals(Pair("D", false), keys[2])
    }

    @Test
    fun testSettings_GetAllSelectedKeys_OnlyMinor() {
        val settings = Settings(minorKeys = listOf("A", "E", "D"))
        val keys = settings.getAllSelectedKeys()

        assertEquals(3, keys.size)
        assertEquals(Pair("A", true), keys[0])
        assertEquals(Pair("E", true), keys[1])
        assertEquals(Pair("D", true), keys[2])
    }

    @Test
    fun testSettings_GetAllSelectedKeys_Mixed() {
        val settings = Settings(
            majorKeys = listOf("C", "G"),
            minorKeys = listOf("A", "E")
        )
        val keys = settings.getAllSelectedKeys()

        assertEquals(4, keys.size)
        // Major keys first
        assertEquals(Pair("C", false), keys[0])
        assertEquals(Pair("G", false), keys[1])
        // Then minor keys
        assertEquals(Pair("A", true), keys[2])
        assertEquals(Pair("E", true), keys[3])
    }

    @Test
    fun testSettings_HasKeysSelected_Empty() {
        val settings = Settings()
        assertFalse(settings.hasKeysSelected())
    }

    @Test
    fun testSettings_HasKeysSelected_OnlyMajor() {
        val settings = Settings(majorKeys = listOf("C"))
        assertTrue(settings.hasKeysSelected())
    }

    @Test
    fun testSettings_HasKeysSelected_OnlyMinor() {
        val settings = Settings(minorKeys = listOf("A"))
        assertTrue(settings.hasKeysSelected())
    }

    @Test
    fun testSettings_HasKeysSelected_Both() {
        val settings = Settings(majorKeys = listOf("C"), minorKeys = listOf("A"))
        assertTrue(settings.hasKeysSelected())
    }

    @Test
    fun testSettings_AvailableOptions() {
        assertEquals(7, Settings.AVAILABLE_KEYS.size)
        assertEquals(7, Settings.AVAILABLE_COUNTS.size)
        assertEquals(13, Settings.AVAILABLE_DELAYS.size)

        // Verify key options
        assertTrue(Settings.AVAILABLE_KEYS.containsAll(listOf("A", "B", "C", "D", "E", "F", "G")))

        // Verify count options
        assertTrue(Settings.AVAILABLE_COUNTS.contains(5))
        assertTrue(Settings.AVAILABLE_COUNTS.contains(40))

        // Verify delay options
        assertTrue(Settings.AVAILABLE_DELAYS.contains(0.5f))
        assertTrue(Settings.AVAILABLE_DELAYS.contains(20f))
    }

    // ========================================
    // GameState Tests
    // ========================================

    @Test
    fun testGameState_DefaultValues() {
        val gameState = GameState()
        assertTrue(gameState.allKeys.isEmpty())
        assertEquals(0, gameState.currentKeyIndex)
        assertEquals("", gameState.currentKey)
        assertFalse(gameState.isMinor)
        assertEquals(1, gameState.currentPosition)
        assertEquals(0, gameState.questionsAsked)
        assertEquals(0f, gameState.countdown, 0.01f)
        assertTrue(gameState.shuffledChords.isEmpty())
    }

    @Test
    fun testGameState_GetCurrentKeyDisplay_Major() {
        val gameState = GameState(currentKey = "C", isMinor = false)
        assertEquals("C", gameState.getCurrentKeyDisplay())
    }

    @Test
    fun testGameState_GetCurrentKeyDisplay_Minor() {
        val gameState = GameState(currentKey = "A", isMinor = true)
        assertEquals("Am", gameState.getCurrentKeyDisplay())
    }

    @Test
    fun testGameState_IsLastKey_True() {
        val keys = listOf(Pair("C", false), Pair("G", false))
        val gameState = GameState(allKeys = keys, currentKeyIndex = 1)
        assertTrue(gameState.isLastKey())
    }

    @Test
    fun testGameState_IsLastKey_False() {
        val keys = listOf(Pair("C", false), Pair("G", false), Pair("D", false))
        val gameState = GameState(allKeys = keys, currentKeyIndex = 1)
        assertFalse(gameState.isLastKey())
    }

    @Test
    fun testGameState_GetNextKey_Exists() {
        val keys = listOf(Pair("C", false), Pair("G", false), Pair("D", false))
        val gameState = GameState(allKeys = keys, currentKeyIndex = 0)

        val nextKey = gameState.getNextKey()
        assertNotNull(nextKey)
        assertEquals(Pair("G", false), nextKey)
    }

    @Test
    fun testGameState_GetNextKey_NotExists() {
        val keys = listOf(Pair("C", false), Pair("G", false))
        val gameState = GameState(allKeys = keys, currentKeyIndex = 1)

        val nextKey = gameState.getNextKey()
        assertNull(nextKey)
    }

    // ========================================
    // SelectedAnswer Tests
    // ========================================

    @Test
    fun testSelectedAnswer_DefaultValues() {
        val answer = SelectedAnswer()
        assertNull(answer.note)
        assertNull(answer.quality)
        assertNull(answer.accidental)
        assertFalse(answer.isComplete())
        assertNull(answer.toChord())
    }

    @Test
    fun testSelectedAnswer_IsComplete_AllSelected() {
        val answer = SelectedAnswer(note = "C", quality = "M", accidental = "")
        assertTrue(answer.isComplete())
    }

    @Test
    fun testSelectedAnswer_IsComplete_MissingNote() {
        val answer = SelectedAnswer(note = null, quality = "M", accidental = "")
        assertFalse(answer.isComplete())
    }

    @Test
    fun testSelectedAnswer_IsComplete_MissingQuality() {
        val answer = SelectedAnswer(note = "C", quality = null, accidental = "")
        assertFalse(answer.isComplete())
    }

    @Test
    fun testSelectedAnswer_IsComplete_MissingAccidental() {
        val answer = SelectedAnswer(note = "C", quality = "M", accidental = null)
        assertFalse(answer.isComplete())
    }

    @Test
    fun testSelectedAnswer_ToChord_Natural() {
        val answer = SelectedAnswer(note = "C", quality = "M", accidental = "")
        val chord = answer.toChord()

        assertNotNull(chord)
        assertEquals("C", chord?.note)
        assertEquals("M", chord?.quality)
    }

    @Test
    fun testSelectedAnswer_ToChord_Sharp() {
        val answer = SelectedAnswer(note = "F", quality = "m", accidental = "#")
        val chord = answer.toChord()

        assertNotNull(chord)
        assertEquals("F#", chord?.note)
        assertEquals("m", chord?.quality)
    }

    @Test
    fun testSelectedAnswer_ToChord_Flat() {
        val answer = SelectedAnswer(note = "B", quality = "dim", accidental = "b")
        val chord = answer.toChord()

        assertNotNull(chord)
        assertEquals("Bb", chord?.note)
        assertEquals("dim", chord?.quality)
    }

    @Test
    fun testSelectedAnswer_ToChord_Incomplete() {
        val answer = SelectedAnswer(note = "C", quality = "M", accidental = null)
        val chord = answer.toChord()

        assertNull(chord)
    }

    // ========================================
    // Chord Display Name Tests
    // ========================================

    @Test
    fun testChord_DisplayName_Major() {
        val chord = Chord("C", "M")
        assertEquals("C", chord.displayName())
    }

    @Test
    fun testChord_DisplayName_Minor() {
        val chord = Chord("D", "m")
        assertEquals("Dm", chord.displayName())
    }

    @Test
    fun testChord_DisplayName_Diminished() {
        val chord = Chord("B", "dim")
        assertEquals("B°", chord.displayName())
    }

    @Test
    fun testChord_DisplayName_WithAccidentals() {
        assertEquals("F#", Chord("F#", "M").displayName())
        assertEquals("Bbm", Chord("Bb", "m").displayName())
        assertEquals("G#°", Chord("G#", "dim").displayName())
    }
}
