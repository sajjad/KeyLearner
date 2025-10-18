package com.example.keylearner

import com.example.keylearner.data.MusicTheory
import com.example.keylearner.data.model.Chord
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MusicTheory chord generation logic
 *
 * Tests chord generation for all major and minor keys,
 * enharmonic handling, and edge cases.
 */
class MusicTheoryTest {

    // ========================================
    // Major Key Tests
    // ========================================

    @Test
    fun testCMajorChords() {
        val expected = listOf(
            Chord("C", "M"),
            Chord("D", "m"),
            Chord("E", "m"),
            Chord("F", "M"),
            Chord("G", "M"),
            Chord("A", "m"),
            Chord("B", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("C", false)
        assertEquals(expected, actual)
    }

    @Test
    fun testDMajorChords() {
        val expected = listOf(
            Chord("D", "M"),
            Chord("E", "m"),
            Chord("F#", "m"),
            Chord("G", "M"),
            Chord("A", "M"),
            Chord("B", "m"),
            Chord("C#", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("D", false)
        assertEquals(expected, actual)
    }

    @Test
    fun testEMajorChords() {
        val expected = listOf(
            Chord("E", "M"),
            Chord("F#", "m"),
            Chord("G#", "m"),
            Chord("A", "M"),
            Chord("B", "M"),
            Chord("C#", "m"),
            Chord("D#", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("E", false)
        assertEquals(expected, actual)
    }

    @Test
    fun testFMajorChords() {
        val expected = listOf(
            Chord("F", "M"),
            Chord("G", "m"),
            Chord("A", "m"),
            Chord("Bb", "M"),
            Chord("C", "M"),
            Chord("D", "m"),
            Chord("E", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("F", false)
        assertEquals(expected, actual)
    }

    @Test
    fun testGMajorChords() {
        val expected = listOf(
            Chord("G", "M"),
            Chord("A", "m"),
            Chord("B", "m"),
            Chord("C", "M"),
            Chord("D", "M"),
            Chord("E", "m"),
            Chord("F#", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("G", false)
        assertEquals(expected, actual)
    }

    @Test
    fun testAMajorChords() {
        val expected = listOf(
            Chord("A", "M"),
            Chord("B", "m"),
            Chord("C#", "m"),
            Chord("D", "M"),
            Chord("E", "M"),
            Chord("F#", "m"),
            Chord("G#", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("A", false)
        assertEquals(expected, actual)
    }

    @Test
    fun testBMajorChords() {
        val expected = listOf(
            Chord("B", "M"),
            Chord("C#", "m"),
            Chord("D#", "m"),
            Chord("E", "M"),
            Chord("F#", "M"),
            Chord("G#", "m"),
            Chord("A#", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("B", false)
        assertEquals(expected, actual)
    }

    // ========================================
    // Minor Key Tests
    // ========================================

    @Test
    fun testAMinorChords() {
        val expected = listOf(
            Chord("A", "m"),
            Chord("B", "dim"),
            Chord("C", "M"),
            Chord("D", "m"),
            Chord("E", "m"),
            Chord("F", "M"),
            Chord("G", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("A", true)
        assertEquals(expected, actual)
    }

    @Test
    fun testBMinorChords() {
        val expected = listOf(
            Chord("B", "m"),
            Chord("C#", "dim"),
            Chord("D", "M"),
            Chord("E", "m"),
            Chord("F#", "m"),
            Chord("G", "M"),
            Chord("A", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("B", true)
        assertEquals(expected, actual)
    }

    @Test
    fun testCMinorChords() {
        val expected = listOf(
            Chord("C", "m"),
            Chord("D", "dim"),
            Chord("Eb", "M"),
            Chord("F", "m"),
            Chord("G", "m"),
            Chord("Ab", "M"),
            Chord("Bb", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("C", true)
        assertEquals(expected, actual)
    }

    @Test
    fun testDMinorChords() {
        val expected = listOf(
            Chord("D", "m"),
            Chord("E", "dim"),
            Chord("F", "M"),
            Chord("G", "m"),
            Chord("A", "m"),
            Chord("Bb", "M"),
            Chord("C", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("D", true)
        assertEquals(expected, actual)
    }

    @Test
    fun testEMinorChords() {
        // Critical test case from README: position 2 should be F#dim, NOT G♮dim
        val expected = listOf(
            Chord("E", "m"),
            Chord("F#", "dim"),  // NOT Gb - because G is already in the scale
            Chord("G", "M"),
            Chord("A", "m"),
            Chord("B", "m"),
            Chord("C", "M"),
            Chord("D", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("E", true)
        assertEquals(expected, actual)
    }

    @Test
    fun testFMinorChords() {
        val expected = listOf(
            Chord("F", "m"),
            Chord("G", "dim"),
            Chord("Ab", "M"),
            Chord("Bb", "m"),
            Chord("C", "m"),
            Chord("Db", "M"),
            Chord("Eb", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("F", true)
        assertEquals(expected, actual)
    }

    @Test
    fun testGMinorChords() {
        val expected = listOf(
            Chord("G", "m"),
            Chord("A", "dim"),
            Chord("Bb", "M"),
            Chord("C", "m"),
            Chord("D", "m"),
            Chord("Eb", "M"),
            Chord("F", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("G", true)
        assertEquals(expected, actual)
    }

    // ========================================
    // Sharp and Flat Key Tests
    // ========================================

    @Test
    fun testFSharpMajorChords() {
        val expected = listOf(
            Chord("F#", "M"),
            Chord("G#", "m"),
            Chord("A#", "m"),
            Chord("B", "M"),
            Chord("C#", "M"),
            Chord("D#", "m"),
            Chord("E#", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("F#", false)
        assertEquals(expected, actual)
    }

    @Test
    fun testBbMajorChords() {
        val expected = listOf(
            Chord("Bb", "M"),
            Chord("C", "m"),
            Chord("D", "m"),
            Chord("Eb", "M"),
            Chord("F", "M"),
            Chord("G", "m"),
            Chord("A", "dim")
        )
        val actual = MusicTheory.getAllChordsInKey("Bb", false)
        assertEquals(expected, actual)
    }

    @Test
    fun testFSharpMinorChords() {
        val expected = listOf(
            Chord("F#", "m"),
            Chord("G#", "dim"),
            Chord("A", "M"),
            Chord("B", "m"),
            Chord("C#", "m"),
            Chord("D", "M"),
            Chord("E", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("F#", true)
        assertEquals(expected, actual)
    }

    @Test
    fun testBbMinorChords() {
        val expected = listOf(
            Chord("Bb", "m"),
            Chord("C", "dim"),
            Chord("Db", "M"),
            Chord("Eb", "m"),
            Chord("F", "m"),
            Chord("Gb", "M"),
            Chord("Ab", "M")
        )
        val actual = MusicTheory.getAllChordsInKey("Bb", true)
        assertEquals(expected, actual)
    }

    // ========================================
    // Individual Position Tests
    // ========================================

    @Test
    fun testGetChordAtSpecificPosition() {
        // C major, position 5 should be G major
        val chord = MusicTheory.getChord("C", false, 5)
        assertEquals(Chord("G", "M"), chord)
    }

    @Test
    fun testGetChordPosition1() {
        // Position 1 in any key should be the tonic
        assertEquals(Chord("D", "M"), MusicTheory.getChord("D", false, 1))
        assertEquals(Chord("E", "m"), MusicTheory.getChord("E", true, 1))
    }

    @Test
    fun testGetChordPosition7() {
        // Position 7 should always be diminished
        assertEquals("dim", MusicTheory.getChord("C", false, 7).quality)
        assertEquals("dim", MusicTheory.getChord("A", true, 2).quality)
    }

    // ========================================
    // Enharmonic Equivalence Tests
    // ========================================

    @Test
    fun testEnharmonicEquivalence_CSharpAndDb() {
        val cSharpMajor = Chord("C#", "M")
        val dbMajor = Chord("Db", "M")
        assertTrue(MusicTheory.areEnharmonicallyEquivalent(cSharpMajor, dbMajor))
    }

    @Test
    fun testEnharmonicEquivalence_FSharpAndGb() {
        val fSharpMinor = Chord("F#", "m")
        val gbMinor = Chord("Gb", "m")
        assertTrue(MusicTheory.areEnharmonicallyEquivalent(fSharpMinor, gbMinor))
    }

    @Test
    fun testEnharmonicEquivalence_SameNote() {
        val chord1 = Chord("C", "M")
        val chord2 = Chord("C", "M")
        assertTrue(MusicTheory.areEnharmonicallyEquivalent(chord1, chord2))
    }

    @Test
    fun testEnharmonicEquivalence_DifferentQuality() {
        val cMajor = Chord("C", "M")
        val cMinor = Chord("C", "m")
        assertFalse(MusicTheory.areEnharmonicallyEquivalent(cMajor, cMinor))
    }

    @Test
    fun testEnharmonicEquivalence_AllEnharmonicPairs() {
        // Test all common enharmonic pairs
        val pairs = listOf(
            Chord("C#", "M") to Chord("Db", "M"),
            Chord("D#", "m") to Chord("Eb", "m"),
            Chord("F#", "dim") to Chord("Gb", "dim"),
            Chord("G#", "M") to Chord("Ab", "M"),
            Chord("A#", "m") to Chord("Bb", "m")
        )

        pairs.forEach { (chord1, chord2) ->
            assertTrue(
                "Expected ${chord1.note} and ${chord2.note} to be enharmonically equivalent",
                MusicTheory.areEnharmonicallyEquivalent(chord1, chord2)
            )
        }
    }

    // ========================================
    // Edge Cases and Validation
    // ========================================

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidPosition_Zero() {
        MusicTheory.getChord("C", false, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidPosition_Eight() {
        MusicTheory.getChord("C", false, 8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidPosition_Negative() {
        MusicTheory.getChord("C", false, -1)
    }

    @Test
    fun testValidPositionsRange() {
        // Test that all positions 1-7 work without throwing
        for (position in 1..7) {
            val chord = MusicTheory.getChord("C", false, position)
            assertNotNull(chord)
        }
    }

    // ========================================
    // Display Name Tests
    // ========================================

    @Test
    fun testGetKeyDisplayName_Major() {
        assertEquals("C", MusicTheory.getKeyDisplayName("C", false))
        assertEquals("F#", MusicTheory.getKeyDisplayName("F#", false))
    }

    @Test
    fun testGetKeyDisplayName_Minor() {
        assertEquals("Am", MusicTheory.getKeyDisplayName("A", true))
        assertEquals("Em", MusicTheory.getKeyDisplayName("E", true))
        assertEquals("F#m", MusicTheory.getKeyDisplayName("F#", true))
    }

    @Test
    fun testChordDisplayName() {
        assertEquals("C", Chord("C", "M").displayName())
        assertEquals("Dm", Chord("D", "m").displayName())
        assertEquals("B°", Chord("B", "dim").displayName())
        assertEquals("F#m", Chord("F#", "m").displayName())
    }

    // ========================================
    // Scale Consistency Tests
    // ========================================

    @Test
    fun testEachLetterAppearsOnceInScale() {
        // Test that in any key, each letter A-G appears exactly once
        val keys = listOf(
            "C" to false, "D" to false, "E" to false, "F" to false, "G" to false, "A" to false, "B" to false,
            "A" to true, "B" to true, "C" to true, "D" to true, "E" to true, "F" to true, "G" to true
        )

        keys.forEach { (key, isMinor) ->
            val chords = MusicTheory.getAllChordsInKey(key, isMinor)
            val letters = chords.map { it.note[0] }

            // Check that we have exactly 7 letters
            assertEquals(7, letters.size)

            // Check that all letters are unique
            assertEquals(7, letters.toSet().size)

            // Check that all letters A-G are present
            val letterSet = letters.toSet()
            assertTrue(
                "Key ${MusicTheory.getKeyDisplayName(key, isMinor)} should contain all letters A-G. Got: $letters",
                letterSet.containsAll(listOf('A', 'B', 'C', 'D', 'E', 'F', 'G'))
            )
        }
    }

    @Test
    fun testMajorScaleQualityPattern() {
        // All major keys should follow the pattern: M m m M M m dim
        val majorKeys = listOf("C", "D", "E", "F", "G", "A", "B")
        val expectedPattern = listOf("M", "m", "m", "M", "M", "m", "dim")

        majorKeys.forEach { key ->
            val chords = MusicTheory.getAllChordsInKey(key, false)
            val qualities = chords.map { it.quality }
            assertEquals(
                "Key $key should follow major scale quality pattern",
                expectedPattern,
                qualities
            )
        }
    }

    @Test
    fun testMinorScaleQualityPattern() {
        // All minor keys should follow the pattern: m dim M m m M M
        val minorKeys = listOf("A", "B", "C", "D", "E", "F", "G")
        val expectedPattern = listOf("m", "dim", "M", "m", "m", "M", "M")

        minorKeys.forEach { key ->
            val chords = MusicTheory.getAllChordsInKey(key, true)
            val qualities = chords.map { it.quality }
            assertEquals(
                "Key ${key}m should follow minor scale quality pattern",
                expectedPattern,
                qualities
            )
        }
    }
}
