package com.example.keylearner.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.keylearner.data.model.GameScores
import com.example.keylearner.ui.screens.GameScreen
import com.example.keylearner.ui.screens.ScoreScreen
import com.example.keylearner.ui.screens.StartScreen
import com.example.keylearner.viewmodel.GameViewModel
import com.example.keylearner.viewmodel.ScoreViewModel
import com.example.keylearner.viewmodel.StartScreenViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    // Shared ViewModel to access settings across navigation
    val context = LocalContext.current
    val startScreenViewModel: StartScreenViewModel = viewModel(
        factory = androidx.lifecycle.viewmodel.compose.viewModel<StartScreenViewModel>().javaClass.let {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return StartScreenViewModel(context.applicationContext as android.app.Application) as T
                }
            }
        }
    )
    val settings by startScreenViewModel.settings.collectAsState()

    // Shared GameViewModel to pass scores from Game to Score screen
    var currentGameScores by remember { mutableStateOf<GameScores?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        // Start Screen - Game configuration
        composable(
            route = Screen.Start.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            val scoreViewModel: ScoreViewModel = viewModel()
            val coroutineScope = rememberCoroutineScope()

            // Export CSV launcher (create new file)
            val exportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("text/csv")
            ) { uri ->
                uri?.let {
                    coroutineScope.launch {
                        val csvContent = scoreViewModel.generateCSVContent()
                        if (csvContent != null) {
                            scoreViewModel.writeCSVToFile(it, csvContent)
                        }
                    }
                }
            }

            // Import CSV launcher (open existing file)
            val importLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let {
                    scoreViewModel.importCSVFromFile(it)
                }
            }

            StartScreen(
                onStartGame = {
                    navController.navigate(Screen.Game.route)
                },
                onViewHistory = {
                    // Navigate to Score screen without current game scores (will show All Time view)
                    currentGameScores = null
                    navController.navigate(Screen.Score.route)
                },
                onExport = {
                    // Generate filename with timestamp
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.UK)
                    val timestamp = dateFormat.format(Date())
                    val filename = "keylearner_scores_$timestamp.csv"
                    exportLauncher.launch(filename)
                },
                onImport = {
                    importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv"))
                }
            )
        }

        // Game Screen - Chord learning gameplay
        composable(
            route = Screen.Game.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            val gameViewModel: GameViewModel = viewModel()

            GameScreen(
                settings = settings,
                viewModel = gameViewModel,
                onQuitToStart = {
                    navController.popBackStack(Screen.Start.route, inclusive = false)
                },
                onGameComplete = {
                    // Save scores before navigating
                    currentGameScores = gameViewModel.getGameScores()
                    navController.navigate(Screen.Score.route) {
                        // Don't allow going back to game from score screen
                        popUpTo(Screen.Start.route)
                    }
                }
            )
        }

        // Score Screen - Results and statistics
        composable(
            route = Screen.Score.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            ScoreScreen(
                currentGameScores = currentGameScores,
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

