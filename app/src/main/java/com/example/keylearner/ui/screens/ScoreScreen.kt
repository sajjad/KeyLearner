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
    viewModel: ScoreViewModel = viewModel()
) {
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedKey by viewModel.selectedKey.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val cumulativeStats by viewModel.cumulativeStats.collectAsState()
    val selectedPositions by viewModel.selectedPositions.collectAsState()
    val progressData by viewModel.progressData.collectAsState()

    var availableKeys by remember { mutableStateOf(currentGameScores?.getPlayedKeys() ?: emptyList()) }

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
                .statusBarsPadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with title and view mode toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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

                // View Mode Toggle Button (only shown if there's a current game)
                if (currentGameScores != null) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Define position colors (used for progress chart and position selector)
            val positionColors = listOf(
                CorrectGreen, Color(0xFF3498DB), Color(0xFF9B59B6),
                Color(0xFFE67E22), Color(0xFFE74C3C), Color(0xFF1ABC9C),
                Color(0xFF34495E)
            )

            // Position Selector (All Time view only)
            if (viewMode == ViewMode.ALL_TIME && selectedKey != null) {
                PositionSelectorCard(
                    selectedKey = selectedKey!!,
                    selectedPositions = selectedPositions,
                    onPositionToggled = { position ->
                        viewModel.togglePosition(position)
                    },
                    chartData = chartData,
                    positionColors = positionColors
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Progress visualisation (shown when positions are selected in All Time view)
            if (viewMode == ViewMode.ALL_TIME && selectedPositions.isNotEmpty() && progressData.isNotEmpty()) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),  // Taller for legend
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Progress Comparison",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LineChartComposable(
                            dataMap = progressData,
                            positionColors = positionColors,
                            chartData = chartData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Compact summary card
                ProgressSummaryCard(
                    selectedPositions = selectedPositions,
                    progressData = progressData,
                    positionColors = positionColors,
                    chartData = chartData
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
            }

            Button(
                onClick = onBackToStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue600
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

            // Update X-axis labels for the new key
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.label })

            chart.data = barData
            chart.invalidate()
        },
        modifier = modifier
    )
}

/**
 * Position selector card with chord chips (multi-select enabled)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PositionSelectorCard(
    selectedKey: String,
    selectedPositions: Set<Int>,
    onPositionToggled: (Int) -> Unit,
    chartData: List<ChartBarData>,
    positionColors: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Position to View Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Row 1: Positions 1-4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (position in 1..4) {
                    val chordLabel = chartData.getOrNull(position - 1)?.label ?: "$position"
                    val lineColor = positionColors[position - 1]
                    FilterChip(
                        selected = position in selectedPositions,
                        onClick = { onPositionToggled(position) },
                        label = {
                            Text(
                                text = chordLabel,
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
                            selected = position in selectedPositions
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Positions 5-7
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (position in 5..7) {
                    val chordLabel = chartData.getOrNull(position - 1)?.label ?: "$position"
                    val lineColor = positionColors[position - 1]
                    FilterChip(
                        selected = position in selectedPositions,
                        onClick = { onPositionToggled(position) },
                        label = {
                            Text(
                                text = chordLabel,
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
                            selected = position in selectedPositions
                        )
                    )
                }
                // Add spacer to balance the row
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
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

                val color = positionColors[position - 1]
                val chordLabel = chartData.getOrNull(position - 1)?.label ?: "$position"

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

            // Configure legend to appear below chart
            chart.legend.isEnabled = true
            chart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            chart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            chart.legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            chart.legend.setDrawInside(false)
            chart.legend.textSize = 10f

            chart.invalidate()
        },
        modifier = modifier
    )
}

/**
 * Compact summary card showing progress metrics for each selected position
 */
@Composable
private fun ProgressSummaryCard(
    selectedPositions: Set<Int>,
    progressData: Map<Int, List<PositionProgressPoint>>,
    positionColors: List<Color>,
    chartData: List<ChartBarData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progress Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            selectedPositions.sorted().forEach { position ->
                val data = progressData[position] ?: emptyList()
                if (data.isNotEmpty()) {
                    val color = positionColors[position - 1]
                    val chordLabel = chartData.getOrNull(position - 1)?.label ?: "$position"
                    val firstAccuracy = data.first().accuracy
                    val latestAccuracy = data.last().accuracy
                    val improvement = latestAccuracy - firstAccuracy

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color indicator
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, shape = MaterialTheme.shapes.small)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = chordLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row {
                            Text(
                                text = "${String.format("%.0f", latestAccuracy)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val arrow = when {
                                improvement > 5f -> "↑"
                                improvement < -5f -> "↓"
                                else -> "→"
                            }
                            val improvementColor = when {
                                improvement > 5f -> Green600
                                improvement < -5f -> WrongOrange
                                else -> Color.Gray
                            }
                            Text(
                                text = "$arrow${String.format("%.0f", kotlin.math.abs(improvement))}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = improvementColor
                            )
                        }
                    }
                }
            }
        }
    }
}
