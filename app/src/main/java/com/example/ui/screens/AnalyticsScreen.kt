package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AccuracyTrendChart
import com.example.ui.components.DailyHeatmapChart
import com.example.ui.components.StudyTimeDistributionChart
import com.example.ui.theme.CleanOnPrimaryContainer
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanSubText
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.WarningOrange

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tests by viewModel.allTests.collectAsState()

    // Calculate aggregated metrics
    val totalGenerated = tests.size
    val solvedTests = tests.filter { it.score != null }
    val totalSolved = solvedTests.size

    val avgAccuracy = if (totalSolved > 0) {
        solvedTests.map { it.accuracy ?: 0f }.average().toFloat()
    } else {
        0f
    }

    val avgTime = if (totalSolved > 0) {
        solvedTests.map { it.elapsedTimeSeconds ?: 0L }.average().toLong()
    } else {
        0L
    }

    // Build trend datasets
    val recentAccuracyList = solvedTests.take(5).map { it.accuracy ?: 0f }.reversed()
    val recentTimeList = solvedTests.take(5).map { it.elapsedTimeSeconds ?: 0L }.reversed()
    val recentDatesList = solvedTests.map { it.createdTimestamp }

    // Topic mastery grouping based on average accuracy
    val topicScores = mutableMapOf<String, MutableList<Float>>()
    solvedTests.forEach { test ->
        if (test.topic.isNotBlank()) {
            val list = topicScores.getOrPut(test.topic) { mutableListOf() }
            list.add(test.accuracy ?: 0f)
        }
    }
    val topicMastery = topicScores.mapValues { it.value.average().toFloat() }.toList().sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        // Screen Header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Analytics Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = CleanOnPrimaryContainer
            )
            Text(
                text = "Review your overall exam performance, consistency, and mastery",
                style = MaterialTheme.typography.bodyMedium,
                color = CleanSubText,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        // Consolidated Stats Grid Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Global Metrics Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MiniStatBlock(label = "Total Tests", value = totalGenerated.toString(), tint = CleanPrimary, modifier = Modifier.weight(1f))
                        MiniStatBlock(label = "Tests Solved", value = totalSolved.toString(), tint = WarningOrange, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        MiniStatBlock(label = "Avg Accuracy", value = "${String.format("%.1f", avgAccuracy)}%", tint = CorrectGreen, modifier = Modifier.weight(1f))
                        MiniStatBlock(label = "Avg Time", value = formatDuration(avgTime), tint = CleanSubText, modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Accuracy Line Trend Chart
        item {
            AccuracyTrendChart(percentages = recentAccuracyList)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Solving Duration Bar Chart
        item {
            StudyTimeDistributionChart(timesInSeconds = recentTimeList)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Consistency Heatmap
        item {
            DailyHeatmapChart(completionDates = recentDatesList)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Topic Mastery Progress Indicator Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Topic Mastery Levels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Calculated based on your average test accuracy by syllabus topic",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanSubText,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    if (topicMastery.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "info", tint = CleanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Topic statistics will group here once tests are submitted.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanSubText
                            )
                        }
                    } else {
                        topicMastery.forEach { (topic, score) ->
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = topic, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${String.format("%.1f", score)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (score >= 75f) CorrectGreen else if (score >= 45f) WarningOrange else Color.Red
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { score / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = if (score >= 75f) CorrectGreen else if (score >= 45f) WarningOrange else Color.Red,
                                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun MiniStatBlock(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = CleanSubText)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = tint, fontSize = 18.sp)
    }
}
