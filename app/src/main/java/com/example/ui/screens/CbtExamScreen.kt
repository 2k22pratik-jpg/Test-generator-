package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.CleanOnPrimaryContainer
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.ui.theme.CleanSubText
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.IncorrectRed
import com.example.ui.theme.ReviewPurple

@Composable
fun CbtExamScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeTest by viewModel.activeTest.collectAsState()
    val questions by viewModel.activeQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val markedForReview by viewModel.markedForReview.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }
    var showPalettePanel by remember { mutableStateOf(false) }

    if (activeTest == null || questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active test loaded")
        }
        return
    }

    val test = activeTest!!
    val currentQuestion = questions[currentIndex]
    val selectedAnswer = userAnswers[currentQuestion.id] ?: ""
    val isCurrentMarked = markedForReview.contains(currentQuestion.id)

    // Compute remaining time
    val totalTimeSeconds = test.timeLimitMinutes * 60
    val remainingSeconds = (totalTimeSeconds - elapsedSeconds).coerceAtLeast(0)
    val remainingMinutesStr = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = test.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CleanOnPrimaryContainer,
                            maxLines = 1
                        )
                        Text(
                            text = "${test.examName} • Q ${currentIndex + 1}/${questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanSubText
                        )
                    }

                    // Live Countdown Timer
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (remainingSeconds < 60) IncorrectRed.copy(alpha = 0.15f) else CleanPrimaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Countdown Timer",
                            tint = if (remainingSeconds < 60) IncorrectRed else CleanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = remainingMinutesStr,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSeconds < 60) IncorrectRed else CleanPrimary,
                            fontSize = 14.sp
                        )
                    }

                    // Palette Toggle (for mobile optimization)
                    IconButton(
                        onClick = { showPalettePanel = !showPalettePanel },
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Toggle Question Palette",
                            tint = CleanPrimary
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Action navigation controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    IconButton(
                        onClick = { viewModel.navigatePrevious() },
                        enabled = currentIndex > 0,
                        modifier = Modifier.testTag("btn_cbt_prev")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous question",
                            tint = if (currentIndex > 0) CleanPrimary else Color.LightGray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Clear Response
                        TextButton(
                            onClick = { viewModel.clearResponse() },
                            colors = ButtonDefaults.textButtonColors(contentColor = IncorrectRed),
                            modifier = Modifier.testTag("btn_cbt_clear")
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "clear answer", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Mark for review
                        TextButton(
                            onClick = { viewModel.toggleMarkForReview() },
                            colors = ButtonDefaults.textButtonColors(contentColor = ReviewPurple),
                            modifier = Modifier.testTag("btn_cbt_review")
                        ) {
                            Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "mark bookmark", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCurrentMarked) "Unmark" else "Review",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Save and Next / Submit
                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { viewModel.saveAndNext() },
                            colors = ButtonDefaults.buttonColors(containerColor = CleanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_cbt_next")
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "next", modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { showSubmitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_cbt_submit")
                        ) {
                            Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = "Submit", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Question column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Question text Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                shape = RoundedCornerShape(50),
                                colors = CardDefaults.cardColors(containerColor = CleanPrimaryContainer.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Question ${currentIndex + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CleanPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            if (isCurrentMarked) {
                                Card(
                                    shape = RoundedCornerShape(50),
                                    colors = CardDefaults.cardColors(containerColor = ReviewPurple.copy(alpha = 0.15f))
                                ) {
                                    Text(
                                        text = "Marked for Review",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ReviewPurple,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options/Answers layout
                Text(
                    text = "Select your answer:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = CleanSubText,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                if (currentQuestion.questionType.lowercase() == "numerical") {
                    OutlinedTextField(
                        value = selectedAnswer,
                        onValueChange = { viewModel.selectAnswer(it) },
                        label = { Text("Enter numeric value response") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("numerical_input"),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else if (currentQuestion.options.isEmpty()) {
                    // True/False fallbacks
                    listOf("True", "False").forEach { option ->
                        val isSelected = selectedAnswer == option
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable { viewModel.selectAnswer(option) }
                                .testTag("option_$option"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CleanPrimaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CleanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CleanPrimary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    currentQuestion.options.forEachIndexed { idx, option ->
                        val isSelected = selectedAnswer == option
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable { viewModel.selectAnswer(option) }
                                .testTag("option_$idx"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CleanPrimaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CleanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CleanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A'.code + idx).toChar().toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CleanPrimary else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }

            // Question Palette Column (Desktop/Tablet panel, or shown via toggle menu)
            if (showPalettePanel) {
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .padding(end = 12.dp, bottom = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question Palette",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            IconButton(onClick = { showPalettePanel = false }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Close palette")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(questions) { index, q ->
                                val ans = userAnswers[q.id] ?: ""
                                val isMarked = markedForReview.contains(q.id)

                                val blockColor = when {
                                    isMarked -> ReviewPurple
                                    ans.isNotEmpty() -> CorrectGreen
                                    index == currentIndex -> CleanPrimary
                                    else -> Color.LightGray.copy(alpha = 0.4f)
                                }

                                val textColor = if (ans.isNotEmpty() || isMarked || index == currentIndex) Color.White else MaterialTheme.colorScheme.onBackground

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(blockColor)
                                        .clickable {
                                            viewModel.setCurrentQuestion(index)
                                            showPalettePanel = false
                                        }
                                        .testTag("palette_index_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (index + 1).toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Legend
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LegendRow(color = CorrectGreen, label = "Answered")
                            LegendRow(color = ReviewPurple, label = "Marked Review")
                            LegendRow(color = CleanPrimary, label = "Active Question")
                            LegendRow(color = Color.LightGray.copy(alpha = 0.4f), label = "Unvisited")
                        }
                    }
                }
            }
        }
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Exam?", fontWeight = FontWeight.Bold) },
            text = {
                val answeredCount = userAnswers.size
                Text("Are you sure you want to submit your responses? You have answered $answeredCount out of ${questions.size} questions.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitTestCbt()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen)
                ) {
                    Text("Yes, Submit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = CleanSubText)
    }
}
