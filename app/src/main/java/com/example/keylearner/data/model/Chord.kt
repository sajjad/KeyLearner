package com.example.keylearner.data.model

/**
 * Represents a musical chord with its note and quality
 *
 * @param note The root note (e.g., "C", "F#", "Bb")
 * @param quality The chord quality: "M" (Major), "m" (minor), or "dim" (diminished)
 */
data class Chord(
    val note: String,
    val quality: String
) {
    /**
     * Returns the display name of the chord
     * Examples: "C", "Dm", "B°"
     */
    fun displayName(): String = when (quality) {
        "m" -> "$note$quality"
        "dim" -> "$note°"
        else -> note // Major chords show just the note
    }
}
