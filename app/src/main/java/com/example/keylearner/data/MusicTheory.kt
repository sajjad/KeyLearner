package com.example.keylearner.data

import com.example.keylearner.data.model.Chord

/**
 * Music theory utilities for chord generation
 *
 * This object handles the generation of chords for any major or minor key,
 * with proper enharmonic handling to ensure each note name appears only once in a scale.
 */
object MusicTheory {

    // Chromatic scale using sharps
    private val notes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    // Enharmonic equivalents (sharps to flats)
    private val noteEnharmonics = mapOf(
        "C#" to "Db",
        "D#" to "Eb",
        "F#" to "Gb",
        "G#" to "Ab",
        "A#" to "Bb"
    )

    // Major scale intervals (whole and half steps from root)
    // W-W-H-W-W-W-H pattern: 0, 2, 4, 5, 7, 9, 11
    private val majorIntervals = listOf(0, 2, 4, 5, 7, 9, 11)

    // Major scale chord qualities for positions 1-7
    // I, ii, iii, IV, V, vi, vii°
    private val majorQualities = listOf("M", "m", "m", "M", "M", "m", "dim")

    // Natural minor scale intervals (whole and half steps from root)
    // W-H-W-W-H-W-W pattern: 0, 2, 3, 5, 7, 8, 10
    private val minorIntervals = listOf(0, 2, 3, 5, 7, 8, 10)

    // Natural minor scale chord qualities for positions 1-7
    // i, ii°, III, iv, v, VI, VII
    private val minorQualities = listOf("m", "dim", "M", "m", "m", "M", "M")

    /**
     * Generate a chord for a given key and position
     *
     * @param key The root note of the key (e.g., "C", "Em", "F#")
     * @param isMinor Whether the key is minor (true) or major (false)
     * @param position The position in the scale (1-7)
     * @return The chord at the specified position
     *
     * Example: getChord("C", false, 1) returns Chord("C", "M")
     * Example: getChord("Em", true, 2) returns Chord("F#", "dim")
     */
    fun getChord(key: String, isMinor: Boolean, position: Int): Chord {
        require(position in 1..7) { "Position must be between 1 and 7" }

        val keyIndex = notes.indexOf(key)
        require(keyIndex != -1) { "Invalid key: $key" }

        return if (isMinor) {
            getMinorChord(key, keyIndex, position)
        } else {
            getMajorChord(key, keyIndex, position)
        }
    }

    /**
     * Generate a chord in a major key
     */
    private fun getMajorChord(key: String, keyIndex: Int, position: Int): Chord {
        val intervals = majorIntervals
        val qualities = majorQualities

        val noteIndex = (keyIndex + intervals[position - 1]) % 12
        var noteName = notes[noteIndex]

        // Enharmonic handling for major keys
        // Sharp keys (G, D, A, E, B) keep sharps
        // Flat keys (F, C) convert sharps to flats
        if (noteName.contains("#")) {
            when (key) {
                in listOf("G", "D", "A", "E", "B") -> {
                    // Keep sharp notation
                }
                in listOf("F", "C") -> {
                    // Convert to flat notation
                    noteName = noteEnharmonics[noteName] ?: noteName
                }
            }
        }

        return Chord(noteName, qualities[position - 1])
    }

    /**
     * Generate a chord in a minor key
     */
    private fun getMinorChord(key: String, keyIndex: Int, position: Int): Chord {
        val intervals = minorIntervals
        val qualities = minorQualities

        val noteIndex = (keyIndex + intervals[position - 1]) % 12
        var noteName = notes[noteIndex]

        // Enharmonic handling for minor keys
        // Minor keys (D, E, G, A, B) prefer flats
        if (noteName.contains("#") && key in listOf("D", "E", "G", "A", "B")) {
            noteName = noteEnharmonics[noteName] ?: noteName
        }

        return Chord(noteName, qualities[position - 1])
    }

    /**
     * Get all 7 chords for a given key
     *
     * @param key The root note of the key
     * @param isMinor Whether the key is minor
     * @return List of all 7 chords in the key
     */
    fun getAllChordsInKey(key: String, isMinor: Boolean): List<Chord> {
        return (1..7).map { position -> getChord(key, isMinor, position) }
    }

    /**
     * Check if two chords are enharmonically equivalent
     * (e.g., C# and Db are the same note)
     *
     * @param chord1 First chord
     * @param chord2 Second chord
     * @return True if the chords are enharmonically equivalent
     */
    fun areEnharmonicallyEquivalent(chord1: Chord, chord2: Chord): Boolean {
        if (chord1.quality != chord2.quality) return false

        // Check if notes are the same
        if (chord1.note == chord2.note) return true

        // Check if one is the enharmonic equivalent of the other
        return noteEnharmonics[chord1.note] == chord2.note ||
               noteEnharmonics[chord2.note] == chord1.note
    }

    /**
     * Get the display name for a key
     *
     * @param key The root note
     * @param isMinor Whether it's a minor key
     * @return Display name (e.g., "C" or "Em")
     */
    fun getKeyDisplayName(key: String, isMinor: Boolean): String {
        return if (isMinor) "${key}m" else key
    }
}
