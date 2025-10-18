package com.example.keylearner.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.keylearner.data.model.GameScores
import com.example.keylearner.data.model.ViewMode
import com.example.keylearner.ui.theme.*
import com.example.keylearner.viewmodel.ScoreViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * Score Screen - Results and statistics
 *
 * Shows scores with toggle between Current Game and All Time views.
 * Displays stacked bar chart and statistics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    currentGameScores: GameScores,
    onReplay: () -> Unit,
    onBackToStart: () -> Unit,
    viewModel: ScoreViewModel = viewModel()
) {
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedKey by viewModel.selectedKey.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val cumulativeStats by viewModel.cumulativeStats.collectAsState()

    var availableKeys by remember { mutableStateOf(currentGameScores.getPlayedKeys()) }

    // Initialize with current game scores
    LaunchedEffect(currentGameScores) {
        viewModel.setCurrentGameScores(currentGameScores)
    }

    // Update available keys when view mode changes
    LaunchedEffect(viewMode) {
        availableKeys = when (viewMode) {
            ViewMode.CURRENT_GAME -> currentGameScores.getPlayedKeys()
            ViewMode.ALL_TIME -> {
                viewModel.loadAllTimeKeys { keys ->
                    availableKeys = keys
                    // Select first key if current selection is not in the list
                    if (selectedKey == null || selectedKey !in keys) {
                        keys.firstOrNull()?.let { viewModel.selectKey(it) }
                    }
                }
                availableKeys // Return current value while loading
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GreenLight, TealLight)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with title and view mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Game Results",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Green600
                )

                // View Mode Toggle Button
                IconButton(
                    onClick = { viewModel.toggleViewMode() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (viewMode == ViewMode.ALL_TIME) Teal600 else Color.LightGray,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Toggle All Time View",
                        tint = if (viewMode == ViewMode.ALL_TIME) Color.White else Color.DarkGray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // View Mode Label
            Text(
                text = when (viewMode) {
                    ViewMode.CURRENT_GAME -> "Current Game"
                    ViewMode.ALL_TIME -> "All Time"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (viewMode) {
                        ViewMode.CURRENT_GAME -> {
                            val stats = viewModel.getCurrentGameStats()
                            if (stats != null) {
                                CurrentGameStatistics(stats)
                            }
                        }
                        ViewMode.ALL_TIME -> {
                            cumulativeStats?.let { AllTimeStatistics(it) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Key",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (availableKeys.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedKey ?: availableKeys.first(),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availableKeys.forEach { key ->
                                    DropdownMenuItem(
                                        text = { Text(key) },
                                        onClick = {
                                            viewModel.selectKey(key)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                if (chartData.isNotEmpty()) {
                    BarChartComposable(
                        data = chartData,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = onReplay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green600
                )
            ) {
                Text(
                    text = "Replay with Same Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBackToStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple600
                )
            ) {
                Text(
                    text = "Back to Start Screen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CurrentGameStatistics(stats: com.example.keylearner.viewmodel.GameStats) {
    Column {
        Text(
            text = "Current Game Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("Total Questions", stats.totalQuestions.toString())
            StatItem("Correct", stats.correctAnswers.toString(), Green600)
            StatItem("Wrong", stats.wrongAnswers.toString(), WrongOrange)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Accuracy: ${String.format("%.1f", stats.accuracy)}%",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Green600
        )
    }
}

@Composable
private fun AllTimeStatistics(stats: com.example.keylearner.data.repository.CumulativeStats) {
    Column {
        Text(
            text = "All Time Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("Games Played", stats.totalGamesPlayed.toString())
            StatItem("Correct", stats.totalCorrect.toString(), Green600)
            StatItem("Wrong", stats.totalWrong.toString(), WrongOrange)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Overall Accuracy: ${String.format("%.1f", stats.overallAccuracy)}%",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Green600
        )

        if (stats.mostPractisedKeys.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Most Practised: ${stats.mostPractisedKeys.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color = Color.DarkGray) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun BarChartComposable(
    data: List<com.example.keylearner.viewmodel.ChartBarData>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setTouchEnabled(false)
                legend.isEnabled = true

                // X-axis configuration
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = 0f
                    valueFormatter = IndexAxisValueFormatter(data.map { it.label })
                    setCenterAxisLabels(false)
                    setAvoidFirstLastClipping(false)
                }

                // Y-axis configuration
                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                    granularity = 1f
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            // Create stacked bar entries
            val stackedEntries = mutableListOf<BarEntry>()

            data.forEachIndexed { index, barData ->
                // For stacked bars: [correct, wrong]
                stackedEntries.add(
                    BarEntry(
                        index.toFloat(),
                        floatArrayOf(barData.correct.toFloat(), barData.wrong.toFloat())
                    )
                )
            }

            val stackedDataSet = BarDataSet(stackedEntries, "").apply {
                colors = listOf(
                    CorrectGreen.toArgb(),
                    WrongOrange.toArgb()
                )
                stackLabels = arrayOf("Correct", "Wrong")
                valueTextColor = AndroidColor.BLACK
                valueTextSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) value.toInt().toString() else ""
                    }
                }
            }

            val barData = BarData(stackedDataSet).apply {
                barWidth = 0.5f
            }

            chart.data = barData
            chart.invalidate()
        },
        modifier = modifier
    )
}
