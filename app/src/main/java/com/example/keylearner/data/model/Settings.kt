package com.example.keylearner.data.model

/**
 * User settings for the chord learning game
 *
 * @param majorKeys List of selected major keys (e.g., ["C", "G", "D"])
 * @param minorKeys List of selected minor keys (e.g., ["A", "E", "D"])
 * @param count Number of questions per key (5, 10, 15, 20, 25, 30, or 40)
 * @param delay Time in seconds before auto-advancing to next question (0.5 to 20)
 * @param limitChoices If true, show only chords in the current key; if false, show all options
 */
data class Settings(
    val majorKeys: List<String> = emptyList(),
    val minorKeys: List<String> = emptyList(),
    val count: Int = 10,
    val delay: Float = 10f,
    val limitChoices: Boolean = true
) {
    /**
     * Get all selected keys as a combined list with their minor status
     */
    fun getAllSelectedKeys(): List<Pair<String, Boolean>> {
        val major = majorKeys.map { it to false }
        val minor = minorKeys.map { it to true }
        return major + minor
    }

    /**
     * Check if at least one key is selected
     */
    fun hasKeysSelected(): Boolean {
        return majorKeys.isNotEmpty() || minorKeys.isNotEmpty()
    }

    companion object {
        // Available options for each setting
        val AVAILABLE_KEYS = listOf("A", "B", "C", "D", "E", "F", "G")
        val AVAILABLE_COUNTS = listOf(5, 10, 15, 20, 25, 30, 40)
        val AVAILABLE_DELAYS = listOf(0.5f, 1f, 1.5f, 2f, 2.5f, 3f, 4f, 5f, 6f, 8f, 10f, 15f, 20f)
    }
}
