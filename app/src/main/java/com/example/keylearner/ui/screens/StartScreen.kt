package com.example.keylearner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.keylearner.data.model.Settings
import com.example.keylearner.ui.theme.BlueLight
import com.example.keylearner.ui.theme.SkyBlueLight
import com.example.keylearner.ui.theme.SkyBlue600
import com.example.keylearner.ui.theme.Blue600
import com.example.keylearner.ui.theme.Green600
import com.example.keylearner.ui.theme.Teal600
import com.example.keylearner.ui.theme.DarkGrey800
import com.example.keylearner.ui.theme.DarkGrey900
import com.example.keylearner.viewmodel.StartScreenViewModel

/**
 * Start Screen - Game configuration
 *
 * Allows users to:
 * - Select major and minor keys to practice
 * - Set number of questions per key (count)
 * - Set delay time between questions
 * - Toggle whether to limit answer choices to chords in the key
 */
@Composable
fun StartScreen(
    onStartGame: () -> Unit,
    onViewHistory: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    viewModel: StartScreenViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    var showInfoDialog by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title with info button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Learn Keys",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = SkyBlue600,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "About",
                        tint = SkyBlue600,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Card containing all settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Keys Section
                    KeysSection(
                        majorKeys = settings.majorKeys,
                        minorKeys = settings.minorKeys,
                        onMajorKeyToggle = viewModel::toggleMajorKey,
                        onMinorKeyToggle = viewModel::toggleMinorKey
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Count Section
                    CountSection(
                        selectedCount = settings.count,
                        onCountSelected = viewModel::updateCount
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Delay Section
                    DelaySection(
                        selectedDelay = settings.delay,
                        onDelaySelected = viewModel::updateDelay
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Limit Choices Section
                    LimitChoicesSection(
                        limitChoices = settings.limitChoices,
                        onLimitChoicesChanged = viewModel::updateLimitChoices
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start Game Button
            val canStart = settings.hasKeysSelected()
            Button(
                onClick = {
                    if (canStart) {
                        onStartGame()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue600,
                    disabledContainerColor = SkyBlue600.copy(alpha = 0.5f),
                    contentColor = BlueLight,
                    disabledContentColor = BlueLight
                ),
                enabled = canStart
            ) {
                Text(
                    text = if (canStart) "Start Game" else "Select at least one key",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View History Button
            OutlinedButton(
                onClick = onViewHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Teal600
                )
            ) {
                Text(
                    text = "View History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import/Export Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Export Data Button
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Teal600
                    )
                ) {
                    Text(
                        text = "Export Data",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Import Data Button
                OutlinedButton(
                    onClick = onImport,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Teal600
                    )
                ) {
                    Text(
                        text = "Import Data",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // About Dialog
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = {
                    Text(
                        text = "About Learn Keys",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This app was written for the express purpose of me trying to learn different chords in the major and minor keys. There is a chance that as my musical journey continues, I'll end up adding more learning exercises as well.\n\n" +
                                "This app doesn't farm your data 😁\n\n" +
                                "Sajjad",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun KeysSection(
    majorKeys: List<String>,
    minorKeys: List<String>,
    onMajorKeyToggle: (String) -> Unit,
    onMinorKeyToggle: (String) -> Unit
) {
    Column {
        Text(
            text = "Keys",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Major Keys
        Text(
            text = "Major",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Settings.AVAILABLE_KEYS.forEach { key ->
                KeyButton(
                    key = key,
                    isSelected = majorKeys.contains(key),
                    onClick = { onMajorKeyToggle(key) },
                    selectedColor = SkyBlue600,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Minor Keys
        Text(
            text = "Minor",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Settings.AVAILABLE_KEYS.forEach { key ->
                KeyButton(
                    key = "${key}m",
                    isSelected = minorKeys.contains(key),
                    onClick = { onMinorKeyToggle(key) },
                    selectedColor = Blue600,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    key: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) selectedColor else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.DarkGray
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CountSection(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "Count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Settings.AVAILABLE_COUNTS.forEach { count ->
                SelectionButton(
                    label = count.toString(),
                    isSelected = selectedCount == count,
                    onClick = { onCountSelected(count) },
                    selectedColor = Green600,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DelaySection(
    selectedDelay: Float,
    onDelaySelected: (Float) -> Unit
) {
    Column {
        Text(
            text = "Delay (seconds)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Row 1: 0.5 to 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Settings.AVAILABLE_DELAYS.take(9).forEach { delay ->
                SelectionButton(
                    label = delay.toString(),
                    isSelected = selectedDelay == delay,
                    onClick = { onDelaySelected(delay) },
                    selectedColor = Green600,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: 8 to 20
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Settings.AVAILABLE_DELAYS.drop(9).forEach { delay ->
                SelectionButton(
                    label = delay.toInt().toString(),
                    isSelected = selectedDelay == delay,
                    onClick = { onDelaySelected(delay) },
                    selectedColor = Green600,
                    modifier = Modifier.weight(1f)
                )
            }
            // Add empty spacers to maintain alignment
            repeat(9 - Settings.AVAILABLE_DELAYS.drop(9).size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LimitChoicesSection(
    limitChoices: Boolean,
    onLimitChoicesChanged: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Limit Choices to Key",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onLimitChoicesChanged(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (limitChoices) Teal600 else Color.LightGray,
                    contentColor = if (limitChoices) Color.White else Color.DarkGray
                )
            ) {
                Text("Yes", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = { onLimitChoicesChanged(false) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!limitChoices) Teal600 else Color.LightGray,
                    contentColor = if (!limitChoices) Color.White else Color.DarkGray
                )
            ) {
                Text("No", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SelectionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
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
