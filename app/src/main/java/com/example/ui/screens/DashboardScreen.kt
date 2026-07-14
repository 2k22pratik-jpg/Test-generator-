package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.CleanDropdownSelector
import com.example.ui.theme.CleanOnPrimaryContainer
import com.example.ui.theme.CleanPrimary
import com.example.ui.theme.CleanPrimaryContainer
import com.example.ui.theme.CleanSubText
import com.example.ui.theme.WarningOrange

data class ExamTemplate(
    val id: String,
    val name: String,
    val defaultSubject: String,
    val defaultTopic: String,
    val defaultDifficulty: String,
    val defaultQuestionType: String,
    val defaultTimeLimit: Int,
    val patternDescription: String
)

val examTemplatesList = listOf(
    ExamTemplate("ssc_cgl", "SSC CGL", "General Awareness", "Indian History & Polity", "Medium", "MCQ", 15, "Factual & Core Concepts"),
    ExamTemplate("rrb_ntpc", "RRB NTPC", "General Science", "Physics & Chemistry", "Medium", "MCQ", 10, "Direct & Concept Application"),
    ExamTemplate("upsc", "UPSC", "Polity & Constitution", "Fundamental Rights", "Hard", "Assertion-Reason", 20, "High Conceptual & Analytical"),
    ExamTemplate("jee", "JEE Mains", "Mathematics", "Calculus & Algebra", "Hard", "Numerical", 25, "Application & Numeric Problem"),
    ExamTemplate("olympiad", "Olympiad", "Science", "Cell Biology", "Hard", "Multiple Correct MCQ", 15, "Tricky Conceptual"),
    ExamTemplate("true_false", "True/False Special", "General Studies", "General Knowledge", "Easy", "True/False", 5, "Factual Check")
)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(0) } // 0 = AI Wizard, 1 = Upload Material

    // AI Wizard Form States
    var examName by remember { mutableStateOf("SSC CGL") }
    var subject by remember { mutableStateOf("General Awareness") }
    var topic by remember { mutableStateOf("Indian History") }
    var difficulty by remember { mutableStateOf("Medium") }
    var numQuestions by remember { mutableStateOf(5f) }
    var language by remember { mutableStateOf("English") }
    var questionType by remember { mutableStateOf("MCQ") }
    var bloomLevel by remember { mutableStateOf("Application") }
    var timeLimit by remember { mutableStateOf(10f) }

    // Upload Material Form States
    var materialTitle by remember { mutableStateOf("") }
    var textMaterial by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadExamName by remember { mutableStateOf("SSC CGL Style") }
    var uploadDifficulty by remember { mutableStateOf("Medium") }
    var uploadNumQuestions by remember { mutableStateOf(5f) }
    var uploadQuestionType by remember { mutableStateOf("MCQ") }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = uris
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        // App header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AIGen Test Pro",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CleanOnPrimaryContainer
                    )
                    Text(
                        text = "Advanced AI Test Engine",
                        style = MaterialTheme.typography.titleSmall,
                        color = CleanSubText,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CleanPrimaryContainer)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI symbol",
                        tint = CleanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Mode toggler
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMode == 0) CleanPrimaryContainer else Color.Transparent)
                        .clickable { selectedMode = 0 }
                        .padding(vertical = 12.dp)
                        .testTag("tab_wizard"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI Wizard Mode",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMode == 0) CleanPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMode == 1) CleanPrimaryContainer else Color.Transparent)
                        .clickable { selectedMode = 1 }
                        .padding(vertical = 12.dp)
                        .testTag("tab_upload"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Upload & Convert",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMode == 1) CleanPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        if (selectedMode == 0) {
            // AI WIZARD MODE SCREEN CONTENT
            item {
                Text(
                    text = "Select Popular Templates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(examTemplatesList) { template ->
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable {
                                    examName = template.name
                                    subject = template.defaultSubject
                                    topic = template.defaultTopic
                                    difficulty = template.defaultDifficulty
                                    questionType = template.defaultQuestionType
                                    timeLimit = template.defaultTimeLimit.toFloat()
                                }
                                .testTag("template_${template.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                if (examName == template.name) CleanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = template.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (examName == template.name) CleanPrimary else MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = template.patternDescription,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleanSubText,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "${template.defaultDifficulty} • ${template.defaultQuestionType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = WarningOrange,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Input Fields Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Configure Test Parameters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Exam Name Input
                        OutlinedTextField(
                            value = examName,
                            onValueChange = { examName = it },
                            label = { Text("Target Exam (e.g., RRB Group D, SSC CGL)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_exam_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CleanPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Subject Input
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject / Section") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_subject"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CleanPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Topic Input
                        OutlinedTextField(
                            value = topic,
                            onValueChange = { topic = it },
                            label = { Text("Topic Tag") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_topic"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CleanPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Question Type Dropdown
                        CleanDropdownSelector(
                            label = "Question Pattern Style",
                            options = listOf("MCQ", "Multiple Correct MCQ", "True/False", "Assertion-Reason", "Match the Following", "Sequence Arrangement", "Numerical Problems"),
                            selectedOption = questionType,
                            onOptionSelected = { questionType = it }
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Difficulty Dropdown
                        CleanDropdownSelector(
                            label = "Difficulty level",
                            options = listOf("Easy", "Medium", "Hard", "PYQ Level"),
                            selectedOption = difficulty,
                            onOptionSelected = { difficulty = it }
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Bloom's Taxonomy Dropdown
                        CleanDropdownSelector(
                            label = "Bloom's Cognitive Level",
                            options = listOf("Remember (Factual)", "Understand (Conceptual)", "Apply (Numerical/Contextual)", "Analyze (Assertion/Logic)", "Evaluate"),
                            selectedOption = bloomLevel,
                            onOptionSelected = { bloomLevel = it }
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Language Dropdown
                        CleanDropdownSelector(
                            label = "Question Language",
                            options = listOf("English", "Hindi", "Bilingual"),
                            selectedOption = language,
                            onOptionSelected = { language = it }
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Number of Questions Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Number of Questions",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${numQuestions.toInt()} Qs",
                                fontWeight = FontWeight.Bold,
                                color = CleanPrimary,
                                fontSize = 16.sp
                            )
                        }
                        Slider(
                            value = numQuestions,
                            onValueChange = { numQuestions = it },
                            valueRange = 5f..20f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = CleanPrimary,
                                activeTrackColor = CleanPrimary,
                                inactiveTrackColor = CleanPrimary.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.testTag("slider_num_questions")
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Time Limit Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Time Limit (Minutes)",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${timeLimit.toInt()} Min",
                                fontWeight = FontWeight.Bold,
                                color = CleanPrimary,
                                fontSize = 16.sp
                            )
                        }
                        Slider(
                            value = timeLimit,
                            onValueChange = { timeLimit = it },
                            valueRange = 5f..60f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = CleanPrimary,
                                activeTrackColor = CleanPrimary,
                                inactiveTrackColor = CleanPrimary.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.testTag("slider_time_limit")
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Generate Button
                        Button(
                            onClick = {
                                viewModel.generateTestFromWizard(
                                    examName = examName,
                                    subject = subject,
                                    topic = topic,
                                    difficulty = difficulty,
                                    numQuestions = numQuestions.toInt(),
                                    language = language,
                                    questionType = questionType,
                                    bloomLevel = bloomLevel,
                                    timeLimit = timeLimit.toInt()
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_generate_wizard"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "wizard active",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Smart Test", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        } else {
            // MODE 2 - UPLOAD & CONVERT FROM MATERIAL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Upload Study Materials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Paste text notes, upload images/photos of scanned materials, or write formulas to auto convert them to a custom test.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanSubText,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Title
                        OutlinedTextField(
                            value = materialTitle,
                            onValueChange = { materialTitle = it },
                            label = { Text("Material Name / Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("material_title"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CleanPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Text Field notes
                        OutlinedTextField(
                            value = textMaterial,
                            onValueChange = { textMaterial = it },
                            label = { Text("Paste Notes, Textbook paragraphs, or text contents") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("material_text"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CleanPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Image attachment layout
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CleanPrimaryContainer.copy(alpha = 0.4f))
                                .clickable { imagePickerLauncher.launch("image/*") }
                                .padding(vertical = 24.dp, horizontal = 12.dp)
                                .testTag("btn_select_images"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (selectedImageUris.isEmpty()) Icons.Default.CloudUpload else Icons.Default.Image,
                                    contentDescription = "Upload images notes",
                                    tint = CleanPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (selectedImageUris.isEmpty()) "Attach Scanned Notes / Images" else "${selectedImageUris.size} Images Attached Successfully",
                                    fontWeight = FontWeight.Bold,
                                    color = CleanPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Tap to browse local device photos for Gemini OCR",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleanSubText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        if (selectedImageUris.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(selectedImageUris) { uri ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, CleanPrimary.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = "Image notes",
                                                tint = CleanPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = uri.lastPathSegment?.takeLast(12) ?: "image_scanned",
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        Text(
                            text = "Output Exam Parameters",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Output Exam Pattern
                        OutlinedTextField(
                            value = uploadExamName,
                            onValueChange = { uploadExamName = it },
                            label = { Text("Output Target Exam Style") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CleanPrimary),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        CleanDropdownSelector(
                            label = "Question Pattern",
                            options = listOf("MCQ", "Multiple Correct MCQ", "True/False", "Assertion-Reason", "Match the Following", "Numerical Problems"),
                            selectedOption = uploadQuestionType,
                            onOptionSelected = { uploadQuestionType = it }
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        CleanDropdownSelector(
                            label = "Difficulty level",
                            options = listOf("Easy", "Medium", "Hard"),
                            selectedOption = uploadDifficulty,
                            onOptionSelected = { uploadDifficulty = it }
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Number of questions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Number of Questions",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${uploadNumQuestions.toInt()} Qs",
                                fontWeight = FontWeight.Bold,
                                color = CleanPrimary,
                                fontSize = 16.sp
                            )
                        }
                        Slider(
                            value = uploadNumQuestions,
                            onValueChange = { uploadNumQuestions = it },
                            valueRange = 5f..15f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = CleanPrimary,
                                activeTrackColor = CleanPrimary
                            ),
                            modifier = Modifier.testTag("slider_upload_num_questions")
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Convert
                        Button(
                            onClick = {
                                viewModel.convertMaterialToTest(
                                    title = materialTitle.ifBlank { "Scanned Notes Material" },
                                    textMaterial = textMaterial,
                                    imageUris = selectedImageUris,
                                    examName = uploadExamName,
                                    difficulty = uploadDifficulty,
                                    numQuestions = uploadNumQuestions.toInt(),
                                    questionType = uploadQuestionType
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_generate_upload"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Convert now",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Convert Notes to Test", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
