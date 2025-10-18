package com.example.keylearner.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.keylearner.ui.screens.StartScreen

/**
 * Main navigation setup for the KeyLearner app
 *
 * This composable sets up the navigation graph with three main destinations:
 * - Start: Game configuration screen
 * - Game: Chord learning gameplay
 * - Score: Results and statistics
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        // Start Screen - Game configuration
        composable(Screen.Start.route) {
            StartScreen(
                onStartGame = {
                    navController.navigate(Screen.Game.route)
                }
            )
        }

        // Game Screen - Chord learning gameplay
        composable(Screen.Game.route) {
            PlaceholderGameScreen(
                onQuitToStart = {
                    navController.popBackStack(Screen.Start.route, inclusive = false)
                },
                onGameComplete = {
                    navController.navigate(Screen.Score.route) {
                        // Don't allow going back to game from score screen
                        popUpTo(Screen.Start.route)
                    }
                }
            )
        }

        // Score Screen - Results and statistics
        composable(Screen.Score.route) {
            PlaceholderScoreScreen(
                onReplay = {
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Start.route)
                    }
                },
                onBackToStart = {
                    navController.popBackStack(Screen.Start.route, inclusive = false)
                }
            )
        }
    }
}

// Placeholder screens - will be replaced with actual implementations in later phases

@Composable
private fun PlaceholderGameScreen(
    onQuitToStart: () -> Unit,
    onGameComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Screen",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Chord learning game will go here",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(
            onClick = onGameComplete,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Complete Game (Placeholder)")
        }
        Button(
            onClick = onQuitToStart,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Quit to Start")
        }
    }
}

@Composable
private fun PlaceholderScoreScreen(
    onReplay: () -> Unit,
    onBackToStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Score Screen",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Results and statistics will go here",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(
            onClick = onReplay,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Replay with Same Settings")
        }
        Button(
            onClick = onBackToStart,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Back to Start Screen")
        }
    }
}
