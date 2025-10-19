package com.example.keylearner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keylearner.data.MusicTheory
import com.example.keylearner.data.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Game Screen
 *
 * Manages the game state, timer, scoring, and progression through keys.
 */
class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _selectedAnswer = MutableStateFlow(SelectedAnswer())
    val selectedAnswer: StateFlow<SelectedAnswer> = _selectedAnswer.asStateFlow()

    private val _scores = MutableStateFlow<MutableMap<String, MutableMap<Int, PositionScore>>>(mutableMapOf())

    private var timerJob: Job? = null
    private var currentSettings: Settings? = null

    /**
     * Initialize the game with settings
     */
    fun startGame(settings: Settings) {
        currentSettings = settings
        val allKeys = settings.getAllSelectedKeys()

        if (allKeys.isEmpty()) return

        val firstKey = allKeys[0]
        val shuffledChords = if (settings.limitChoices) {
            MusicTheory.getAllChordsInKey(firstKey.first, firstKey.second).shuffled()
        } else {
            emptyList()
        }

        _gameState.value = GameState(
            allKeys = allKeys,
            currentKeyIndex = 0,
            currentKey = firstKey.first,
            isMinor = firstKey.second,
            currentPosition = Random.nextInt(1, 8),
            questionsAsked = 0,
            countdown = settings.delay,
            shuffledChords = shuffledChords
        )

        startTimer()
    }

    /**
     * Start the countdown timer
     */
    private fun startTimer() {
        timerJob?.cancel()

        val settings = currentSettings ?: return
        if (settings.delay <= 0) return

        timerJob = viewModelScope.launch {
            while (true) {
                delay(50) // Update every 50ms for smoother animation

                val current = _gameState.value ?: break
                val newCountdown = current.countdown - 0.05f

                if (newCountdown <= 0) {
                    // Timer expired - change to new random position
                    _gameState.value = current.copy(
                        currentPosition = Random.nextInt(1, 8),
                        countdown = settings.delay
                    )
                } else {
                    _gameState.value = current.copy(countdown = newCountdown)
                }
            }
        }
    }

    /**
     * Handle answer submission
     */
    fun submitAnswer(answer: Chord): GameResult {
        val current = _gameState.value ?: return GameResult.Continue
        val settings = currentSettings ?: return GameResult.Continue

        val correctChord = MusicTheory.getChord(
            current.currentKey,
            current.isMinor,
            current.currentPosition
        )

        val isCorrect = MusicTheory.areEnharmonicallyEquivalent(answer, correctChord)

        // Update scores
        updateScore(current.getCurrentKeyDisplay(), current.currentPosition, isCorrect)

        // Move to next question
        val newQuestionsAsked = current.questionsAsked + 1

        if (newQuestionsAsked >= settings.count) {
            // Move to next key or end game
            return advanceToNextKey(current)
        } else {
            // Next question in same key
            advanceQuestion(current, newQuestionsAsked, settings)
            return GameResult.Continue
        }
    }

    /**
     * Advance to the next question in the current key
     */
    private fun advanceQuestion(current: GameState, newQuestionsAsked: Int, settings: Settings) {
        val newChords = if (settings.limitChoices) {
            MusicTheory.getAllChordsInKey(current.currentKey, current.isMinor).shuffled()
        } else {
            emptyList()
        }

        _gameState.value = current.copy(
            currentPosition = Random.nextInt(1, 8),
            questionsAsked = newQuestionsAsked,
            countdown = settings.delay,
            shuffledChords = newChords
        )

        _selectedAnswer.value = SelectedAnswer()
    }

    /**
     * Advance to the next key or complete the game
     */
    private fun advanceToNextKey(current: GameState): GameResult {
        val nextKeyIndex = current.currentKeyIndex + 1

        if (nextKeyIndex >= current.allKeys.size) {
            // Game complete
            timerJob?.cancel()
            return GameResult.GameComplete
        }

        val settings = currentSettings ?: return GameResult.Continue
        val nextKey = current.allKeys[nextKeyIndex]

        val shuffledChords = if (settings.limitChoices) {
            MusicTheory.getAllChordsInKey(nextKey.first, nextKey.second).shuffled()
        } else {
            emptyList()
        }

        _gameState.value = current.copy(
            currentKeyIndex = nextKeyIndex,
            currentKey = nextKey.first,
            isMinor = nextKey.second,
            currentPosition = Random.nextInt(1, 8),
            questionsAsked = 0,
            countdown = settings.delay,
            shuffledChords = shuffledChords
        )

        _selectedAnswer.value = SelectedAnswer()
        return GameResult.Continue
    }

    /**
     * Update the score for a specific key and position
     */
    private fun updateScore(keyName: String, position: Int, isCorrect: Boolean) {
        val currentScores = _scores.value
        val keyScores = currentScores.getOrPut(keyName) { mutableMapOf() }
        val positionScore = keyScores.getOrPut(position) { PositionScore() }

        keyScores[position] = if (isCorrect) {
            positionScore.copy(correct = positionScore.correct + 1)
        } else {
            positionScore.copy(wrong = positionScore.wrong + 1)
        }
    }

    /**
     * Get the final game scores
     */
    fun getGameScores(): GameScores {
        val keyScores = _scores.value.map { (keyName, positionScores) ->
            keyName to KeyScore(keyName, positionScores.toMap())
        }.toMap()

        return GameScores(keyScores)
    }

    // Methods for full-choice mode

    /**
     * Update the selected note
     */
    fun selectNote(note: String) {
        _selectedAnswer.value = _selectedAnswer.value.copy(note = note)
    }

    /**
     * Update the selected quality
     */
    fun selectQuality(quality: String) {
        _selectedAnswer.value = _selectedAnswer.value.copy(quality = quality)
    }

    /**
     * Update the selected accidental
     */
    fun selectAccidental(accidental: String) {
        _selectedAnswer.value = _selectedAnswer.value.copy(accidental = accidental)
    }

    /**
     * Submit the composed answer (full-choice mode)
     */
    fun submitComposedAnswer(): GameResult {
        val answer = _selectedAnswer.value.toChord() ?: return GameResult.Continue
        return submitAnswer(answer)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

/**
 * Result of answering a question
 */
sealed class GameResult {
    object Continue : GameResult()
    object GameComplete : GameResult()
}
