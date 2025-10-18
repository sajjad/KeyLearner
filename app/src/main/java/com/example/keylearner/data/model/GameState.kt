package com.example.keylearner.data.model

/**
 * Represents the current state of an active game session
 *
 * @param allKeys List of all keys to practice (key name + isMinor flag)
 * @param currentKeyIndex Index of the current key in allKeys
 * @param currentKey Current key being practiced (e.g., "C", "E")
 * @param isMinor Whether the current key is minor
 * @param currentPosition Current chord position (1-7)
 * @param questionsAsked Number of questions asked for the current key
 * @param countdown Current countdown timer value in seconds
 * @param shuffledChords Shuffled list of chords for limited choice mode
 */
data class GameState(
    val allKeys: List<Pair<String, Boolean>> = emptyList(),
    val currentKeyIndex: Int = 0,
    val currentKey: String = "",
    val isMinor: Boolean = false,
    val currentPosition: Int = 1,
    val questionsAsked: Int = 0,
    val countdown: Float = 0f,
    val shuffledChords: List<Chord> = emptyList()
) {
    /**
     * Get the display name for the current key
     */
    fun getCurrentKeyDisplay(): String {
        return if (isMinor) "${currentKey}m" else currentKey
    }

    /**
     * Check if this is the last key in the game
     */
    fun isLastKey(): Boolean {
        return currentKeyIndex >= allKeys.size - 1
    }

    /**
     * Get the next key in the sequence
     */
    fun getNextKey(): Pair<String, Boolean>? {
        val nextIndex = currentKeyIndex + 1
        return if (nextIndex < allKeys.size) allKeys[nextIndex] else null
    }
}

/**
 * Represents a selected answer in the game
 * Used when limitChoices is false
 *
 * @param note Selected note (A-G)
 * @param quality Selected quality (M, m, or dim)
 * @param accidental Selected accidental (empty string for natural, "#" for sharp, "b" for flat)
 */
data class SelectedAnswer(
    val note: String? = null,
    val quality: String? = null,
    val accidental: String? = null
) {
    /**
     * Check if all parts of the answer are selected
     */
    fun isComplete(): Boolean {
        return note != null && quality != null && accidental != null
    }

    /**
     * Convert to a Chord object
     */
    fun toChord(): Chord? {
        return if (isComplete()) {
            val fullNote = "$note${accidental ?: ""}"
            Chord(fullNote, quality!!)
        } else {
            null
        }
    }
}
