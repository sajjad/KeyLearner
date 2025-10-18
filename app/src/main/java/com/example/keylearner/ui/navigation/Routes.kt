package com.example.keylearner.ui.navigation

/**
 * Navigation routes for the KeyLearner app
 */
sealed class Screen(val route: String) {
    /**
     * Start screen - where users configure game settings
     */
    data object Start : Screen("start")

    /**
     * Game screen - where users play the chord learning game
     */
    data object Game : Screen("game")

    /**
     * Score screen - where users view their results
     */
    data object Score : Screen("score")
}
