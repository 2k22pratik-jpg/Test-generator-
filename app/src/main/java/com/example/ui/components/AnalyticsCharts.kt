package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanOnPrimaryContainer
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenContainer
import com.example.ui.theme.IncorrectRed
import com.example.ui.theme.WarningOrange

@Composable
fun AccuracyTrendChart(
    percentages: List<Float>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Accuracy Trend (%)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (percentages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete tests in your library to see trend charts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 40f
                    val paddingRight = 40f
                    val paddingTop = 20f
                    val paddingBottom = 40f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    // Draw grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = paddingTop + (chartHeight / gridLines) * i
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 2f
                        )
                    }

                    // Plot data points
                    val pointsCount = percentages.size
                    val stepX = if (pointsCount > 1) chartWidth / (pointsCount - 1) else chartWidth

                    val path = Path()
                    val drawPoints = mutableListOf<Offset>()

                    percentages.forEachIndexed { idx, score ->
                        val pct = score.coerceIn(0f, 100f)
                        val x = paddingLeft + idx * stepX
                        val y = paddingTop + chartHeight - (chartHeight * (pct / 100f))
                        val point = Offset(x, y)
                        drawPoints.add(point)

                        if (idx == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Draw connection path line
                    drawPath(
                        path = path,
                        color = CleanPrimary,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )

                    // Draw filled data points
                    drawPoints.forEach { point ->
                        drawCircle(
                            color = CleanPrimary,
                            radius = 12f,
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = point
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudyTimeDistributionChart(
    timesInSeconds: List<Long>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Solving Duration Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (timesInSeconds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Time logs will show here after solving tests",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 60f
                    val paddingRight = 40f
                    val paddingTop = 20f
                    val paddingBottom = 40f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    val maxTime = (timesInSeconds.maxOrNull() ?: 1L).toFloat().coerceAtLeast(10f)

                    val barCount = timesInSeconds.size
                    val spacing = 24f
                    val barWidth = (chartWidth - (spacing * (barCount - 1))) / barCount

                    timesInSeconds.forEachIndexed { idx, time ->
                        val barHeight = (time.toFloat() / maxTime) * chartHeight
                        val x = paddingLeft + idx * (barWidth + spacing)
                        val y = paddingTop + chartHeight - barHeight

                        drawRoundRect(
                            color = WarningOrange,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyHeatmapChart(
    completionDates: List<Long>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Consistency Heatmap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Last 14 days activity log:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Build a grid of 14 blocks, colored intensely based on matches in completionDates
            val curTime = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 13 downTo 0) {
                    val dayStart = curTime - (i * dayMillis)
                    val dayEnd = dayStart + dayMillis
                    val testsOnDay = completionDates.count { it in dayStart..dayEnd }

                    val blockColor = when {
                        testsOnDay == 0 -> Color.LightGray.copy(alpha = 0.3f)
                        testsOnDay == 1 -> CleanPrimary.copy(alpha = 0.4f)
                        testsOnDay == 2 -> CleanPrimary.copy(alpha = 0.7f)
                        else -> CleanPrimary
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(blockColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (testsOnDay > 0) {
                            Text(
                                text = testsOnDay.toString(),
                                style = androidx.compose.ui.text.TextStyle(
                                    color = if (testsOnDay >= 2) Color.White else CleanOnPrimaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
