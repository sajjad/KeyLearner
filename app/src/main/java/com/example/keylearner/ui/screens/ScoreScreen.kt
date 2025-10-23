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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.keylearner.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.keylearner.data.model.GameScores
import com.example.keylearner.data.model.PositionProgressPoint
import com.example.keylearner.data.model.ViewMode
import com.example.keylearner.ui.theme.*
import com.example.keylearner.viewmodel.ChartBarData
import com.example.keylearner.viewmodel.ScoreViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlin.math.abs

/**
 * Score Screen - Results and statistics
 *
 * Shows scores with toggle between Current Game and All Time views.
 * Displays stacked bar chart and statistics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    currentGameScores: GameScores?,
    onReplay: () -> Unit,
    onBackToStart: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    viewModel: ScoreViewModel = viewModel()
) {
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedKey by viewModel.selectedKey.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val cumulativeStats by viewModel.cumulativeStats.collectAsState()
    val progressData by viewModel.progressData.collectAsState()
    val exportImportStatus by viewModel.exportImportStatus.collectAsState()
    val currentGameScoresState by viewModel.currentGameScores.collectAsState()
    val selectedChordFilters by viewModel.selectedChordFilters.collectAsState()

    var availableKeys by remember { mutableStateOf(currentGameScores?.getPlayedKeys() ?: emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize with current game scores (if provided)
    LaunchedEffect(currentGameScores) {
        if (currentGameScores != null) {
            viewModel.setCurrentGameScores(currentGameScores)
        } else {
            // No current game scores - switch to All Time view
            viewModel.setViewModeToAllTime()
        }
    }

    // Update available keys when view mode changes
    LaunchedEffect(viewMode) {
        availableKeys = when (viewMode) {
            ViewMode.CURRENT_GAME -> currentGameScores?.getPlayedKeys() ?: emptyList()
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

    // Handle export/import status messages
    LaunchedEffect(exportImportStatus) {
        when (val status = exportImportStatus) {
            is com.example.keylearner.viewmodel.ExportImportStatus.ExportSuccess -> {
                snackbarHostState.showSnackbar("Export successful - file saved")
                viewModel.clearExportImportStatus()
            }
            is com.example.keylearner.viewmodel.ExportImportStatus.ImportSuccess -> {
                snackbarHostState.showSnackbar("Import successful - scores loaded")
                viewModel.clearExportImportStatus()

                // Reload available keys list in UI
                if (viewMode == ViewMode.ALL_TIME) {
                    viewModel.loadAllTimeKeys { keys ->
                        availableKeys = keys
                    }
                }
            }
            is com.example.keylearner.viewmodel.ExportImportStatus.Error -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.clearExportImportStatus()
            }
            else -> {} // Idle, Exporting, Importing - no message
        }
    }

    val isDarkTheme = isSystemInDarkTheme()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = if (isDarkTheme) {
                            listOf(DarkGrey800, DarkGrey900)
                        } else {
                            listOf(GreenLight, TealLight)
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
            // Header with title and buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentGameScores != null) "Game Results" else "Historical Statistics",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Green600
                )

                Spacer(modifier = Modifier.weight(1f))

                // Export Button (Download)
                IconButton(
                    onClick = onExport,
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_export),
                        contentDescription = "Export scores to CSV",
                        tint = Teal600,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Import Button (Upload)
                IconButton(
                    onClick = onImport,
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_import),
                        contentDescription = "Import scores from CSV",
                        tint = Teal600,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // View Mode Toggle Button (only shown if there's a current game)
                if (currentGameScores != null) {
                    Spacer(modifier = Modifier.width(8.dp))

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
            }

            // View Mode Label (only shown if there's a current game)
            if (currentGameScores != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (viewMode) {
                        ViewMode.CURRENT_GAME -> "Current Game"
                        ViewMode.ALL_TIME -> "All Time"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (viewMode) {
                        ViewMode.CURRENT_GAME -> {
                            // Derive stats from observed StateFlow
                            currentGameScoresState?.let { scores ->
                                var totalCorrect = 0
                                var totalWrong = 0

                                scores.keyScores.values.forEach { keyScore ->
                                    totalCorrect += keyScore.getTotalCorrect()
                                    totalWrong += keyScore.getTotalWrong()
                                }

                                val totalAttempts = totalCorrect + totalWrong
                                val accuracy = if (totalAttempts > 0) {
                                    (totalCorrect.toFloat() / totalAttempts * 100)
                                } else {
                                    0f
                                }

                                val stats = com.example.keylearner.viewmodel.GameStats(
                                    totalQuestions = totalAttempts,
                                    correctAnswers = totalCorrect,
                                    wrongAnswers = totalWrong,
                                    accuracy = accuracy
                                )

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

            Spacer(modifier = Modifier.height(16.dp))

            // Performance Analysis Section (Response Time + Progress Comparison)
            if (selectedKey != null) {
                ResponseTimeAnalysisSection(
                    viewModel = viewModel,
                    selectedKey = selectedKey!!,
                    chartData = chartData,
                    viewMode = viewMode,
                    progressData = progressData,
                    selectedChordFilters = selectedChordFilters
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            // Only show Replay button if there's a current game
            if (currentGameScores != null) {
                Button(
                    onClick = onReplay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green600,
                        contentColor = BlueLight
                    )
                ) {
                    Text(
                        text = "Replay with Same Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                        )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onBackToStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue600,
                    contentColor = BlueLight
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
}

@Composable
fun CurrentGameStatistics(stats: com.example.keylearner.viewmodel.GameStats) {
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
fun AllTimeStatistics(stats: com.example.keylearner.data.repository.CumulativeStats) {
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
fun StatItem(label: String, value: String, color: Color = Color.DarkGray) {
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

/**
 * Performance Analysis Section
 *
 * Displays chord filter chips, response time scatter chart, progress comparison line chart, and statistics
 */
@Composable
fun ResponseTimeAnalysisSection(
    viewModel: ScoreViewModel,
    selectedKey: String,
    chartData: List<ChartBarData>,
    viewMode: ViewMode,
    progressData: Map<Int, List<PositionProgressPoint>>,
    selectedChordFilters: Set<Int>
) {
    val responseTimeData by viewModel.responseTimeData.collectAsState()
    val responseTimeStats by viewModel.responseTimeStats.collectAsState()

    // Get filtered data for the chart - recalculate when dependencies change
    val filteredData = remember(responseTimeData, selectedChordFilters) {
        responseTimeData.filter { it.position in selectedChordFilters }
    }

    // Determine if minor key
    val isMinor = selectedKey.endsWith("m")
    val rootKey = if (isMinor) selectedKey.dropLast(1) else selectedKey

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Chord Filter Chips
            Text(
                text = "Filter by Position",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Define note colors (A-G)
            val noteColors = mapOf(
                "A" to Color(0xFF00AA00),  // Green
                "B" to Color(0xFF0066CC),  // Blue
                "C" to Color(0xFFCC0000),  // Red
                "D" to Color(0xFFFFDD00),  // Yellow
                "E" to Color(0xFF87CEEB),  // Sky Blue
                "F" to Color(0xFF8B00FF),  // Violet
                "G" to Color(0xFFFF8800)   // Orange
            )

            // Row 1: Positions 1-4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (position in 1..4) {
                    val chord = com.example.keylearner.data.MusicTheory.getChord(rootKey, isMinor, position)
                    val label = "$position-${chord.displayName()}"
                    val isSelected = position in selectedChordFilters
                    // Extract root note and get its color
                    val rootNote = chord.note.replace("#", "").replace("♭", "").first().toString()
                    val lineColor = noteColors[rootNote] ?: Color.Gray

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleChordFilter(position) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        color = Color(0xFFEEEEEE),
                                        shape = MaterialTheme.shapes.extraSmall
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = lineColor,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal600,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Positions 5-7
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (position in 5..7) {
                    val chord = com.example.keylearner.data.MusicTheory.getChord(rootKey, isMinor, position)
                    val label = "$position-${chord.displayName()}"
                    val isSelected = position in selectedChordFilters
                    // Extract root note and get its color
                    val rootNote = chord.note.replace("#", "").replace("♭", "").first().toString()
                    val lineColor = noteColors[rootNote] ?: Color.Gray

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleChordFilter(position) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        color = Color(0xFFEEEEEE),
                                        shape = MaterialTheme.shapes.extraSmall
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = lineColor,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal600,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }

                // Clear All / Select All toggle button
                val allCleared = selectedChordFilters.isEmpty()
                FilterChip(
                    selected = !allCleared,  // Selected when chips are active
                    onClick = {
                        if (allCleared) {
                            viewModel.selectAllChordFilters()
                        } else {
                            viewModel.clearAllChordFilters()
                        }
                    },
                    label = {
                        Text(
                            text = if (allCleared) "Select All" else "Clear All",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE74C3C),  // Red when clearing
                        selectedLabelColor = Color.White,
                        containerColor = Color.LightGray,
                        labelColor = Color.DarkGray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = !allCleared
                    )
                )
            }

            // Scatter Chart
            if (responseTimeData.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    // Key on selected filters to force chart update when filters change
                    key(selectedChordFilters) {
                        ResponseTimeScatterChartComposable(
                            responseTimeData = filteredData,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }
            } else {
                // No response time data available at all
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No response time data available",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Progress Comparison (All Time view only)
            if (viewMode == ViewMode.ALL_TIME && selectedChordFilters.isNotEmpty() && progressData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                // Progress Comparison Line Chart
                Text(
                    text = "Progress Comparison",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    // Define position colors (used for progress chart)
                    val positionColors = listOf(
                        CorrectGreen, Color(0xFF3498DB), Color(0xFF9B59B6),
                        Color(0xFFE67E22), Color(0xFFE74C3C), Color(0xFF1ABC9C),
                        Color(0xFF34495E)
                    )

                    LineChartComposable(
                        dataMap = progressData,
                        positionColors = positionColors,
                        chartData = chartData,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Statistics Card (only show if there's response time data and filtered data)
            if (responseTimeData.isNotEmpty() && filteredData.isNotEmpty()) {
                responseTimeStats?.let { stats ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Statistics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Average Time
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Average",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = String.format("%.2fs", stats.averageTime),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Fastest Time
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Fastest",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = String.format("%.2fs", stats.fastestTime),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CorrectGreen
                                    )
                                }

                                // Slowest Time
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Slowest",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = String.format("%.2fs", stats.slowestTime),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WrongOrange
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Correct vs Incorrect Average Times
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Avg Correct",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = String.format("%.2fs", stats.averageCorrectTime),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CorrectGreen
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Avg Incorrect",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = String.format("%.2fs", stats.averageIncorrectTime),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WrongOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarChartComposable(
    data: List<com.example.keylearner.viewmodel.ChartBarData>,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) AndroidColor.WHITE else AndroidColor.BLACK

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setTouchEnabled(false)
                legend.isEnabled = true
                legend.textColor = textColor

                // X-axis configuration
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = 0f
                    valueFormatter = IndexAxisValueFormatter(data.map { it.label })
                    setCenterAxisLabels(false)
                    setAvoidFirstLastClipping(false)
                    this.textColor = textColor
                }

                // Y-axis configuration
                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                    granularity = 1f
                    this.textColor = textColor
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
                valueTextColor = textColor
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

            // Update X-axis labels and colors for the new key
            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(data.map { it.label })
                this.textColor = textColor
            }
            chart.axisLeft.textColor = textColor
            chart.legend.textColor = textColor

            chart.data = barData
            chart.invalidate()
        },
        modifier = modifier
    )
}

/**
 * Line chart for progress visualisation (multi-line support)
 */
@Composable
fun LineChartComposable(
    dataMap: Map<Int, List<PositionProgressPoint>>,
    positionColors: List<Color>,
    chartData: List<ChartBarData>,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) AndroidColor.WHITE else AndroidColor.BLACK

    // Define note colors (A-G)
    val noteColors = mapOf(
        "A" to Color(0xFF00AA00),  // Green
        "B" to Color(0xFF0066CC),  // Blue
        "C" to Color(0xFFCC0000),  // Red
        "D" to Color(0xFFFFDD00),  // Yellow
        "E" to Color(0xFF87CEEB),  // Sky Blue
        "F" to Color(0xFF8B00FF),  // Violet
        "G" to Color(0xFFFF8800)   // Orange
    )

    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setTouchEnabled(true)
                setPinchZoom(false)

                // X-axis configuration
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "S${value.toInt()}"  // S1, S2, S3, etc.
                        }
                    }
                    this.textColor = textColor
                }

                // Y-axis configuration
                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                    axisMaximum = 100f
                    granularity = 10f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()}%"
                        }
                    }
                    this.textColor = textColor
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            if (dataMap.isEmpty()) return@AndroidView

            val dataSets = mutableListOf<com.github.mikephil.charting.data.LineDataSet>()

            dataMap.entries.sortedBy { it.key }.forEachIndexed { index, (position, points) ->
                if (points.isEmpty()) return@forEachIndexed

                val entries = points.map { point ->
                    com.github.mikephil.charting.data.Entry(
                        point.sessionIndex.toFloat(),
                        point.accuracy
                    )
                }

                val chordLabel = chartData.getOrNull(position - 1)?.label ?: "$position"
                // Extract root note from chord label (e.g., "1-Em" -> "E")
                val chordName = chordLabel.split("-").getOrNull(1) ?: ""
                val rootNote = chordName.replace("#", "").replace("♭", "").replace("°", "").firstOrNull()?.toString() ?: ""
                val color = noteColors[rootNote] ?: Color.Gray

                val lineDataSet = com.github.mikephil.charting.data.LineDataSet(entries, chordLabel).apply {
                    this.color = color.toArgb()
                    lineWidth = 3f
                    setDrawCircles(false)  // Disable data point markers to prevent clutter when many data points
                    setDrawValues(false)  // Disable values on points (too cluttered with multiple lines)

                    // Apply different line styles for distinction
                    when (index % 3) {
                        0 -> {
                            // Solid line (default)
                        }
                        1 -> {
                            // Dashed line
                            enableDashedLine(10f, 5f, 0f)
                        }
                        2 -> {
                            // Dotted line
                            enableDashedLine(2f, 5f, 0f)
                        }
                    }

                    mode = com.github.mikephil.charting.data.LineDataSet.Mode.LINEAR
                }

                dataSets.add(lineDataSet)
            }

            val lineData = com.github.mikephil.charting.data.LineData(dataSets as List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet>)
            chart.data = lineData

            // Update axis colors
            chart.xAxis.textColor = textColor
            chart.axisLeft.textColor = textColor

            // Configure legend to appear below chart
            chart.legend.apply {
                isEnabled = true
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 10f
                this.textColor = textColor
            }

            chart.invalidate()
        },
        modifier = modifier
    )
}

/**
 * Response Time Scatter Chart Composable
 *
 * Displays response times for each question as scatter points
 * Color-coded: Green for correct answers, Orange for incorrect answers
 */
@Composable
fun ResponseTimeScatterChartComposable(
    responseTimeData: List<com.example.keylearner.data.model.ResponseTimePoint>,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) AndroidColor.WHITE else AndroidColor.BLACK

    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.ScatterChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setTouchEnabled(true)
                setPinchZoom(true)
                isDoubleTapToZoomEnabled = false

                // X-axis configuration
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()  // Question number: 1, 2, 3...
                        }
                    }
                    this.textColor = textColor
                }

                // Y-axis configuration
                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                    granularity = 0.5f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2fs", value)
                        }
                    }
                    this.textColor = textColor
                }
                axisRight.isEnabled = false

                // Configure legend
                legend.apply {
                    isEnabled = true
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    xEntrySpace = 16f
                    yEntrySpace = 0f
                    this.textColor = textColor
                }
            }
        },
        update = { chart ->
            if (responseTimeData.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            // Define note colors (A-G)
            val noteColors = mapOf(
                "A" to AndroidColor.parseColor("#00AA00"),  // Green
                "B" to AndroidColor.parseColor("#0066CC"),  // Blue
                "C" to AndroidColor.parseColor("#CC0000"),  // Red
                "D" to AndroidColor.parseColor("#FFDD00"),  // Yellow
                "E" to AndroidColor.parseColor("#87CEEB"),  // Sky Blue
                "F" to AndroidColor.parseColor("#8B00FF"),  // Violet
                "G" to AndroidColor.parseColor("#FF8800")   // Orange
            )

            // Group data by note name and correctness
            data class NoteGroup(val note: String, val isCorrect: Boolean)
            val groupedData = mutableMapOf<NoteGroup, MutableList<com.github.mikephil.charting.data.Entry>>()

            responseTimeData.forEachIndexed { index, point ->
                // Extract root note from chord (e.g., "Em" -> "E", "F#" -> "F", "C" -> "C")
                val rootNote = point.chord.note.replace("#", "").replace("♭", "").first().toString()
                val group = NoteGroup(rootNote, point.isCorrect)

                val entry = com.github.mikephil.charting.data.Entry(
                    (index + 1).toFloat(),  // Renumbered question number (1-based)
                    point.responseTimeSeconds
                )

                groupedData.getOrPut(group) { mutableListOf() }.add(entry)
            }

            val dataSets = mutableListOf<com.github.mikephil.charting.interfaces.datasets.IScatterDataSet>()

            // Create datasets for each note/correctness combination
            groupedData.forEach { (group, entries) ->
                val noteColor = noteColors[group.note] ?: AndroidColor.GRAY
                val label = "${group.note}${if (group.isCorrect) "" else " (wrong)"}"

                val dataSet = com.github.mikephil.charting.data.ScatterDataSet(entries, label).apply {
                    color = noteColor
                    // Filled circle for correct, hollow circle for incorrect
                    setScatterShape(
                        if (group.isCorrect) {
                            com.github.mikephil.charting.charts.ScatterChart.ScatterShape.CIRCLE
                        } else {
                            com.github.mikephil.charting.charts.ScatterChart.ScatterShape.CIRCLE
                        }
                    )
                    scatterShapeSize = 14f

                    // For incorrect answers, make them hollow by using a different drawing mode
                    if (!group.isCorrect) {
                        scatterShapeHoleRadius = 6f  // Makes it hollow
                        scatterShapeHoleColor = if (isDarkTheme) AndroidColor.parseColor("#1E1E1E") else AndroidColor.WHITE
                    }

                    setDrawValues(false)  // Disable value labels
                }
                dataSets.add(dataSet)
            }

            val scatterData = com.github.mikephil.charting.data.ScatterData(dataSets)
            chart.data = scatterData

            // Update axis colors
            chart.xAxis.textColor = textColor
            chart.axisLeft.textColor = textColor
            chart.legend.textColor = textColor

            // Update legend
            chart.legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 12f
                yEntrySpace = 4f
                formSize = 10f
                this.textColor = textColor
            }

            // Set X-axis range based on filtered data
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = (responseTimeData.size + 1).toFloat()

            // Set Y-axis maximum to a reasonable value
            val maxTime = responseTimeData.maxOfOrNull { it.responseTimeSeconds } ?: 5f
            chart.axisLeft.axisMaximum = maxTime + 1f

            chart.invalidate()
        },
        modifier = modifier
    )
}
