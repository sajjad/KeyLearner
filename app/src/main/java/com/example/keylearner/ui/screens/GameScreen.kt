package com.example.keylearner.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.keylearner.data.model.Chord
import com.example.keylearner.data.model.Settings
import com.example.keylearner.ui.theme.*
import com.example.keylearner.viewmodel.GameResult
import com.example.keylearner.viewmodel.GameViewModel

/**
 * Game Screen - Chord learning gameplay
 *
 * Displays the current key, a random chord position (1-7), and answer choices.
 * Two modes:
 * - Limited: 7 shuffled chords from the current key
 * - Full: Note/Quality/Accidental selection
 */
@Composable
fun GameScreen(
    settings: Settings,
    onQuitToStart: () -> Unit,
    onGameComplete: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()

    // Initialize game on first composition
    LaunchedEffect(Unit) {
        viewModel.startGame(settings)
    }

    // Safety check
    if (gameState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val state = gameState!!
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isDarkTheme = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(DarkGrey800, DarkGrey900)
                    } else {
                        listOf(SkyBlueLight, BlueLight)
                    }
                )
            )
    ) {
        if (isLandscape) {
            // Landscape Layout: Left = Key/Position, Right = Choices
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left side: Key and Position
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quit Button
                    Button(
                        onClick = onQuitToStart,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Quit", style = MaterialTheme.typography.bodyMedium)
                    }

                    // Current Key Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Current Key",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = state.getCurrentKeyDisplay(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = SkyBlue600
                            )
                            Text(
                                text = "Question ${state.questionsAsked + 1} of ${settings.count}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    // Current Position Display
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Chord Position",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = state.currentPosition.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (settings.delay > 0) {
                                Text(
                                    text = "Next: ${String.format("%.1f", state.countdown)}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Right side: Answer Choices
                Card(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (settings.limitChoices) {
                        LimitedChoicesMode(
                            shuffledChords = state.shuffledChords,
                            onChordSelected = { chord ->
                                val result = viewModel.submitAnswer(chord)
                                if (result is GameResult.GameComplete) {
                                    onGameComplete()
                                }
                            },
                            isLandscape = true
                        )
                    } else {
                        FullChoicesMode(
                            selectedAnswer = selectedAnswer,
                            onNoteSelected = viewModel::selectNote,
                            onQualitySelected = viewModel::selectQuality,
                            onAccidentalSelected = viewModel::selectAccidental,
                            onSubmit = {
                                val result = viewModel.submitComposedAnswer()
                                if (result is GameResult.GameComplete) {
                                    onGameComplete()
                                }
                            },
                            isLandscape = true
                        )
                    }
                }
            }
        } else {
            // Portrait Layout: Original vertical layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                // Quit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onQuitToStart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Quit to Start")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Current Key Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Current Key",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = state.getCurrentKeyDisplay(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SkyBlue600
                        )
                        Text(
                            text = "Question ${state.questionsAsked + 1} of ${settings.count}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Position Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chord Position",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = state.currentPosition.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (settings.delay > 0) {
                            Text(
                                text = "Next in: ${String.format("%.1f", state.countdown)}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Answer Choices
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (settings.limitChoices) {
                        LimitedChoicesMode(
                            shuffledChords = state.shuffledChords,
                            onChordSelected = { chord ->
                                val result = viewModel.submitAnswer(chord)
                                if (result is GameResult.GameComplete) {
                                    onGameComplete()
                                }
                            },
                            isLandscape = false
                        )
                    } else {
                        FullChoicesMode(
                            selectedAnswer = selectedAnswer,
                            onNoteSelected = viewModel::selectNote,
                            onQualitySelected = viewModel::selectQuality,
                            onAccidentalSelected = viewModel::selectAccidental,
                            onSubmit = {
                                val result = viewModel.submitComposedAnswer()
                                if (result is GameResult.GameComplete) {
                                    onGameComplete()
                                }
                            },
                            isLandscape = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LimitedChoicesMode(
    shuffledChords: List<Chord>,
    onChordSelected: (Chord) -> Unit,
    isLandscape: Boolean = false
) {
    val modifier = if (isLandscape) {
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    }

    Column(
        modifier = modifier,
        verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.spacedBy(12.dp)
    ) {
        shuffledChords.chunked(2).forEach { rowChords ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowChords.forEach { chord ->
                    Button(
                        onClick = { onChordSelected(chord) },
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLandscape) 56.dp else 64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyBlue600
                        )
                    ) {
                        Text(
                            text = chord.displayName(),
                            style = if (isLandscape)
                                MaterialTheme.typography.titleMedium
                            else
                                MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BlueLight
                        )
                    }
                }
                // Add spacer if odd number
                if (rowChords.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (isLandscape) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FullChoicesMode(
    selectedAnswer: com.example.keylearner.data.model.SelectedAnswer,
    onNoteSelected: (String) -> Unit,
    onQualitySelected: (String) -> Unit,
    onAccidentalSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    isLandscape: Boolean = false
) {
    // Shuffle notes once to prevent users from counting positions
    val shuffledNotes = remember { listOf("A", "B", "C", "D", "E", "F", "G").shuffled() }

    val modifier = if (isLandscape) {
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    }

    val spacing = if (isLandscape) 8.dp else 8.dp
    val buttonHeight = if (isLandscape) 44.dp else 44.dp

    Column(
        modifier = modifier,
        verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.spacedBy(spacing)
    ) {
        // Note Selection
        Text(
            text = "Note",
            style = if (isLandscape)
                MaterialTheme.typography.titleSmall
            else
                MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            shuffledNotes.forEach { note ->
                SelectionButton(
                    label = note,
                    isSelected = selectedAnswer.note == note,
                    onClick = { onNoteSelected(note) },
                    selectedColor = SkyBlue600,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing))

        // Quality Selection
        Text(
            text = "Chord Quality",
            style = if (isLandscape)
                MaterialTheme.typography.titleSmall
            else
                MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "M" to "M",
                "m" to "m",
                "dim" to "dim"
            ).forEach { (label, value) ->
                SelectionButton(
                    label = label,
                    isSelected = selectedAnswer.quality == value,
                    onClick = { onQualitySelected(value) },
                    selectedColor = Blue600,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing))

        // Accidental Selection
        Text(
            text = "Accidental",
            style = if (isLandscape)
                MaterialTheme.typography.titleSmall
            else
                MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Natural" to "",
                "#" to "#",
                "♭" to "b"
            ).forEach { (label, value) ->
                SelectionButton(
                    label = label,
                    isSelected = selectedAnswer.accidental == value,
                    onClick = { onAccidentalSelected(value) },
                    selectedColor = Green600,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 12.dp))

        // Submit Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isLandscape) 48.dp else 52.dp),
            enabled = selectedAnswer.isComplete(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green600
            )
        ) {
            Text(
                text = "Submit Answer",
                style = if (isLandscape)
                    MaterialTheme.typography.titleMedium
                else
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SelectionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 48.dp
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) selectedColor else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.DarkGray
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
