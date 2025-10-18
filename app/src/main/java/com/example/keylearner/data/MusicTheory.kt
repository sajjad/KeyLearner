package com.example.keylearner.data

import com.example.keylearner.data.model.Chord

/**
 * Music theory utilities for chord generation
 *
 * This object handles the generation of chords for any major or minor key,
 * with proper enharmonic handling to ensure each letter name (A-G) appears exactly once in a scale.
 */
object MusicTheory {

    // Letter names in order
    private val letters = listOf("A", "B", "C", "D", "E", "F", "G")

    // Semitones from C for each natural note
    private val naturalNoteSemitones = mapOf(
        "C" to 0, "D" to 2, "E" to 4, "F" to 5, "G" to 7, "A" to 9, "B" to 11
    )

    // Major scale intervals (semitones from root)
    // W-W-H-W-W-W-H pattern: 0, 2, 4, 5, 7, 9, 11
    private val majorIntervals = listOf(0, 2, 4, 5, 7, 9, 11)

    // Major scale chord qualities for positions 1-7
    // I, ii, iii, IV, V, vi, vii°
    private val majorQualities = listOf("M", "m", "m", "M", "M", "m", "dim")

    // Natural minor scale intervals (semitones from root)
    // W-H-W-W-H-W-W pattern: 0, 2, 3, 5, 7, 8, 10
    private val minorIntervals = listOf(0, 2, 3, 5, 7, 8, 10)

    // Natural minor scale chord qualities for positions 1-7
    // i, ii°, III, iv, v, VI, VII
    private val minorQualities = listOf("m", "dim", "M", "m", "m", "M", "M")

    /**
     * Generate a chord for a given key and position
     *
     * @param key The root note of the key (e.g., "C", "E", "F#")
     * @param isMinor Whether the key is minor (true) or major (false)
     * @param position The position in the scale (1-7)
     * @return The chord at the specified position
     *
     * Example: getChord("C", false, 1) returns Chord("C", "M")
     * Example: getChord("E", true, 2) returns Chord("F#", "dim")
     */
    fun getChord(key: String, isMinor: Boolean, position: Int): Chord {
        require(position in 1..7) { "Position must be between 1 and 7" }

        // Build the scale with proper letter-name spelling
        val scale = buildScale(key, if (isMinor) minorIntervals else majorIntervals)
        val qualities = if (isMinor) minorQualities else majorQualities

        return Chord(scale[position - 1], qualities[position - 1])
    }

    /**
     * Build a scale with proper enharmonic spelling
     * Ensures each letter (A-G) appears exactly once
     *
     * @param rootNote The root note (e.g., "C", "F#", "Bb")
     * @param intervals List of semitone intervals from root
     * @return List of note names with correct enharmonic spelling
     */
    private fun buildScale(rootNote: String, intervals: List<Int>): List<String> {
        // Parse the root note to get letter and accidental
        val rootLetter = rootNote[0].toString()
        val rootAccidental = if (rootNote.length > 1) rootNote.substring(1) else ""

        // Get the semitone value of the root
        val rootSemitones = getNoteInSemitones(rootNote)

        // Find starting position in letter sequence
        val startLetterIndex = letters.indexOf(rootLetter)

        // Build scale notes
        return intervals.mapIndexed { index, interval ->
            // Calculate which letter this scale degree should use
            val letterIndex = (startLetterIndex + index) % 7
            val letter = letters[letterIndex]

            // Calculate target semitone value
            val targetSemitone = (rootSemitones + interval) % 12

            // Calculate natural semitone value for this letter
            val naturalSemitone = naturalNoteSemitones[letter]!!

            // Calculate how many semitones we need to adjust
            val adjustment = (targetSemitone - naturalSemitone + 12) % 12

            // Apply the appropriate accidental
            when (adjustment) {
                0 -> letter                    // Natural
                1 -> "${letter}#"             // Sharp
                2 -> "${letter}##"            // Double sharp (rare)
                11 -> "${letter}b"            // Flat
                10 -> "${letter}bb"           // Double flat (rare)
                else -> {
                    // This shouldn't happen in normal keys
                    letter
                }
            }
        }
    }

    /**
     * Convert a note name to its semitone value (0-11)
     */
    private fun getNoteInSemitones(note: String): Int {
        val letter = note[0].toString()
        val accidental = if (note.length > 1) note.substring(1) else ""

        var semitones = naturalNoteSemitones[letter]!!

        // Apply accidentals
        when (accidental) {
            "#" -> semitones += 1
            "##" -> semitones += 2
            "b" -> semitones -= 1
            "bb" -> semitones -= 2
        }

        return (semitones + 12) % 12
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

        // Check if they have the same semitone value
        return getNoteInSemitones(chord1.note) == getNoteInSemitones(chord2.note)
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
