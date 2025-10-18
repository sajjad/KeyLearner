package com.example.keylearner

import com.example.keylearner.data.MusicTheory
import com.example.keylearner.data.model.Chord
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for game logic, specifically answer checking with enharmonic equivalence
 */
class GameLogicTest {

    // ========================================
    // Answer Checking Tests
    // ========================================

    @Test
    fun testCorrectAnswer_ExactMatch() {
        val correctChord = MusicTheory.getChord("C", false, 5) // G Major
        val userAnswer = Chord("G", "M")

        assertTrue(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    @Test
    fun testIncorrectAnswer_WrongNote() {
        val correctChord = MusicTheory.getChord("C", false, 5) // G Major
        val userAnswer = Chord("A", "M")

        assertFalse(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    @Test
    fun testIncorrectAnswer_WrongQuality() {
        val correctChord = MusicTheory.getChord("C", false, 5) // G Major
        val userAnswer = Chord("G", "m") // Wrong quality

        assertFalse(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    @Test
    fun testEnharmonicAnswer_CSharpVsDb() {
        // In D major, position 7 is C# diminished
        val correctChord = MusicTheory.getChord("D", false, 7) // C# dim
        val userAnswer = Chord("Db", "dim") // Enharmonic equivalent

        assertTrue(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    @Test
    fun testEnharmonicAnswer_FSharpVsGb() {
        // In E minor, position 2 is F# diminished
        val correctChord = MusicTheory.getChord("E", true, 2) // F# dim
        val userAnswer = Chord("Gb", "dim") // Enharmonic equivalent

        assertTrue(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    @Test
    fun testEnharmonicAnswer_AllCommonPairs() {
        // Test all common enharmonic pairs in real key contexts
        val testCases = listOf(
            // D major position 3 (F#m) can be answered as Gbm
            Triple("D", false, 3) to Chord("Gb", "m"),
            // A major position 3 (C#m) can be answered as Dbm
            Triple("A", false, 3) to Chord("Db", "m"),
            // B major position 3 (D#m) can be answered as Ebm
            Triple("B", false, 3) to Chord("Eb", "m"),
            // Bb major position 4 (Eb) can be answered as D#
            Triple("Bb", false, 4) to Chord("D#", "M")
        )

        testCases.forEach { (keyInfo, userAnswer) ->
            val (key, isMinor, position) = keyInfo
            val correctChord = MusicTheory.getChord(key, isMinor, position)
            assertTrue(
                "Expected ${correctChord.note}${correctChord.quality} to match enharmonic ${userAnswer.note}${userAnswer.quality}",
                MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer)
            )
        }
    }

    // ========================================
    // Position Validation Tests
    // ========================================

    @Test
    fun testAllPositionsInKey_CMajor() {
        // Verify all positions 1-7 return valid chords
        for (position in 1..7) {
            val chord = MusicTheory.getChord("C", false, position)
            assertNotNull(chord)
            assertNotNull(chord.note)
            assertNotNull(chord.quality)
            assertTrue(chord.quality in listOf("M", "m", "dim"))
        }
    }

    @Test
    fun testAllPositionsInKey_EMinor() {
        // Verify all positions 1-7 return valid chords
        for (position in 1..7) {
            val chord = MusicTheory.getChord("E", true, position)
            assertNotNull(chord)
            assertNotNull(chord.note)
            assertNotNull(chord.quality)
            assertTrue(chord.quality in listOf("M", "m", "dim"))
        }
    }

    // ========================================
    // Quality Pattern Tests
    // ========================================

    @Test
    fun testMajorKeyQualityPattern() {
        // Major keys: I ii iii IV V vi vii°
        // Pattern: M m m M M m dim
        val expected = listOf("M", "m", "m", "M", "M", "m", "dim")

        val keys = listOf("C", "D", "E", "F", "G", "A", "B")
        keys.forEach { key ->
            val qualities = (1..7).map { position ->
                MusicTheory.getChord(key, false, position).quality
            }
            assertEquals("Key $key should follow major quality pattern", expected, qualities)
        }
    }

    @Test
    fun testMinorKeyQualityPattern() {
        // Natural minor keys: i ii° III iv v VI VII
        // Pattern: m dim M m m M M
        val expected = listOf("m", "dim", "M", "m", "m", "M", "M")

        val keys = listOf("A", "B", "C", "D", "E", "F", "G")
        keys.forEach { key ->
            val qualities = (1..7).map { position ->
                MusicTheory.getChord(key, true, position).quality
            }
            assertEquals("Key ${key}m should follow minor quality pattern", expected, qualities)
        }
    }

    // ========================================
    // Answer Acceptance Tests
    // ========================================

    @Test
    fun testAcceptNaturalNote() {
        // In C major, position 1 is C major
        val correctChord = MusicTheory.getChord("C", false, 1)
        val userAnswer = Chord("C", "M")

        assertTrue(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    @Test
    fun testAcceptSharpNote() {
        // In D major, position 3 is F# minor
        val correctChord = MusicTheory.getChord("D", false, 3)
        val userAnswer = Chord("F#", "m")

        assertTrue(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    @Test
    fun testAcceptFlatNote() {
        // In F major, position 4 is Bb major
        val correctChord = MusicTheory.getChord("F", false, 4)
        val userAnswer = Chord("Bb", "M")

        assertTrue(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    @Test
    fun testRejectWrongAccidental() {
        // In C major, position 1 is C major, NOT C# major
        val correctChord = MusicTheory.getChord("C", false, 1)
        val userAnswer = Chord("C#", "M")

        assertFalse(MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer))
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    fun testDiminishedChordAnswerChecking() {
        // Position 7 in major keys is always diminished
        val correctChord = MusicTheory.getChord("C", false, 7) // B dim
        val correctAnswer = Chord("B", "dim")
        val wrongQualityAnswer = Chord("B", "m")

        assertTrue(MusicTheory.areEnharmonicallyEquivalent(correctChord, correctAnswer))
        assertFalse(MusicTheory.areEnharmonicallyEquivalent(correctChord, wrongQualityAnswer))
    }

    @Test
    fun testMultipleKeysWithSameChord() {
        // The chord C major appears in multiple keys
        // In C major (position 1), F major (position 5), G major (position 4)
        val cMajorPos1 = MusicTheory.getChord("C", false, 1)
        val fMajorPos5 = MusicTheory.getChord("F", false, 5)
        val gMajorPos4 = MusicTheory.getChord("G", false, 4)

        // All should be C major
        assertEquals(Chord("C", "M"), cMajorPos1)
        assertEquals(Chord("C", "M"), fMajorPos5)
        assertEquals(Chord("C", "M"), gMajorPos4)

        // All should be equivalent to each other
        assertTrue(MusicTheory.areEnharmonicallyEquivalent(cMajorPos1, fMajorPos5))
        assertTrue(MusicTheory.areEnharmonicallyEquivalent(fMajorPos5, gMajorPos4))
    }

    // ========================================
    // Real Game Scenario Tests
    // ========================================

    @Test
    fun testGameScenario_CorrectProgressionCMajor() {
        // Simulate answering all 7 positions correctly in C major
        val expectedAnswers = listOf(
            Chord("C", "M"),
            Chord("D", "m"),
            Chord("E", "m"),
            Chord("F", "M"),
            Chord("G", "M"),
            Chord("A", "m"),
            Chord("B", "dim")
        )

        for (position in 1..7) {
            val correctChord = MusicTheory.getChord("C", false, position)
            val userAnswer = expectedAnswers[position - 1]
            assertTrue(
                "Position $position in C major should accept ${userAnswer.displayName()}",
                MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer)
            )
        }
    }

    @Test
    fun testGameScenario_MixedCorrectIncorrectEMinor() {
        // Simulate a mix of correct and incorrect answers in E minor
        val testCases = listOf(
            // position, user answer, expected result
            Triple(1, Chord("E", "m"), true),        // Correct
            Triple(2, Chord("F#", "dim"), true),     // Correct
            Triple(3, Chord("A", "M"), false),       // Wrong - should be G major
            Triple(4, Chord("A", "m"), true),        // Correct
            Triple(5, Chord("B", "M"), false),       // Wrong - should be B minor
            Triple(6, Chord("C", "M"), true),        // Correct
            Triple(7, Chord("D", "M"), true)         // Correct
        )

        testCases.forEach { (position, userAnswer, expectedResult) ->
            val correctChord = MusicTheory.getChord("E", true, position)
            val isCorrect = MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer)
            assertEquals(
                "Position $position in E minor: expected $expectedResult for ${userAnswer.displayName()}",
                expectedResult,
                isCorrect
            )
        }
    }

    @Test
    fun testGameScenario_EnharmonicAnswersAccepted() {
        // User answers with enharmonic equivalents throughout a game
        val testCases = listOf(
            // D major with enharmonic answers
            Triple("D", false, 3) to Chord("Gb", "m"),  // F#m answered as Gbm
            Triple("D", false, 7) to Chord("Db", "dim"), // C#dim answered as Dbdim

            // B major with enharmonic answers
            Triple("B", false, 2) to Chord("Db", "m"),  // C#m answered as Dbm
            Triple("B", false, 6) to Chord("Ab", "m")   // G#m answered as Abm
        )

        testCases.forEach { (keyInfo, userAnswer) ->
            val (key, isMinor, position) = keyInfo
            val correctChord = MusicTheory.getChord(key, isMinor, position)
            assertTrue(
                "Enharmonic answer ${userAnswer.displayName()} should be accepted for ${correctChord.displayName()}",
                MusicTheory.areEnharmonicallyEquivalent(correctChord, userAnswer)
            )
        }
    }
}
