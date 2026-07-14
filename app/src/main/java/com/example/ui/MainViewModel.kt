package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.GeminiContent
import com.example.api.GeminiGenerationConfig
import com.example.api.GeminiInlineData
import com.example.api.GeminiPart
import com.example.api.GeminiRequest
import com.example.api.GeneratedQuestion
import com.example.api.GeneratedTestResponse
import com.example.api.RetrofitClient
import com.example.data.QuestionEntity
import com.example.data.StudyMaterialEntity
import com.example.data.TestEntity
import com.example.data.TestRepository
import com.example.data.UserAnswerEntity
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class MainViewModel(
    private val repository: TestRepository,
    private val context: Context
) : ViewModel() {

    // --- Database Flows ---
    val allTests: StateFlow<List<TestEntity>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyMaterials: StateFlow<List<StudyMaterialEntity>> = repository.allStudyMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State Variables ---
    private val _uiState = MutableStateFlow<UiState>(UiState.Dashboard)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    // --- Settings ---
    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // --- Active Test CBT Session ---
    private val _activeTest = MutableStateFlow<TestEntity?>(null)
    val activeTest: StateFlow<TestEntity?> = _activeTest.asStateFlow()

    private val _activeQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val activeQuestions: StateFlow<List<QuestionEntity>> = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // Map of questionId to selectedAnswer
    private val _userAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, String>> = _userAnswers.asStateFlow()

    // Set of questionIds marked for review
    private val _markedForReview = MutableStateFlow<Set<Int>>(emptySet())
    val markedForReview: StateFlow<Set<Int>> = _markedForReview.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)

    // For Result Screen
    private val _testResultAnswers = MutableStateFlow<List<UserAnswerEntity>>(emptyList())
    val testResultAnswers: StateFlow<List<UserAnswerEntity>> = _testResultAnswers.asStateFlow()

    init {
        startTimerJob()
    }

    fun setUiState(state: UiState) {
        _uiState.value = state
    }

    fun selectModel(modelName: String) {
        _selectedModel.value = modelName
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
    }

    private fun getApiKey(): String {
        return _customApiKey.value.ifBlank { BuildConfig.GEMINI_API_KEY }
    }

    // --- Timer Handling ---
    private fun startTimerJob() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isTimerRunning.value) {
                    _elapsedSeconds.value += 1
                }
            }
        }
    }

    // --- AI Test Generation (Mode 1 - AI Wizard) ---
    fun generateTestFromWizard(
        examName: String,
        subject: String,
        topic: String,
        difficulty: String,
        numQuestions: Int,
        language: String,
        questionType: String,
        bloomLevel: String,
        timeLimit: Int
    ) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading("Generating custom questions using Gemini AI...")
            try {
                val model = _selectedModel.value
                val apiKey = getApiKey()

                val prompt = """
                    You are a highly professional test generator. Create a test matching these exact requirements:
                    - Exam Target: $examName
                    - Subject: $subject
                    - Topic: $topic
                    - Difficulty Level: $difficulty
                    - Number of Questions requested: $numQuestions (Provide between 5 to 10 unique, non-duplicate, high-quality exam questions matching the style of the target exam)
                    - Question Type requested: $questionType
                    - Cognitive level (Bloom's Taxonomy): $bloomLevel
                    - Primary Language: $language
                    
                    Return a JSON object matching this schema:
                    {
                      "title": "A highly relevant test title",
                      "questions": [
                        {
                          "questionText": "Question description",
                          "questionType": "$questionType",
                          "options": ["Option A", "Option B", "Option C", "Option D"],
                          "correctAnswer": "Option A", // This must exactly match one of the items in the options array, or represent 'True' / 'False' for True/False questionType. If Match the Following, option can display matching parts like 'A-2, B-4, C-1, D-3'.
                          "explanation": "Detailed step-by-step verified explanation of why this answer is correct",
                          "difficulty": "$difficulty",
                          "topicTag": "$topic",
                          "estimatedTimeSeconds": 60,
                          "confidenceScore": 0.95,
                          "reference": "Reference syllabus concept or books (e.g. PYQ target)"
                        }
                      ]
                    }
                    
                    Ensure extreme fact correctness, no duplicate questions, proper formatting, and correct option choice. Double check the answer correctness before rendering. Use JSON only, no markdown formatting blocks.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.5f
                    ),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are a professional examiners committee specializing in exams like RRB, SSC, UPSC, JEE, and Olympiads. You must strictly output JSON matching the requested structure without any markdown wrap or extra description.")))
                )

                val response = RetrofitClient.service.generateContent(model, apiKey, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("No response received from AI model.")

                val cleanJson = cleanJsonString(jsonText)
                val testResponse = parseGeneratedTestResponse(cleanJson)

                if (testResponse == null || testResponse.questions.isEmpty()) {
                    throw Exception("Could not parse test response from Gemini API. Clean JSON: $cleanJson")
                }

                saveTestToDb(
                    testResponse = testResponse,
                    examName = examName,
                    subject = subject,
                    topic = topic,
                    difficulty = difficulty,
                    numQuestions = testResponse.questions.size,
                    language = language,
                    questionType = questionType,
                    bloomLevel = bloomLevel,
                    timeLimitMinutes = timeLimit
                )

                _loadingState.value = LoadingState.Success("Test generated successfully!")
                _uiState.value = UiState.Library
            } catch (e: Exception) {
                Log.e("MainViewModel", "Test generation failed", e)
                _loadingState.value = LoadingState.Error("Failed to generate test: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    // --- Mode 2 - Generate From Uploaded Material (OCR / Images / Text) ---
    fun convertMaterialToTest(
        title: String,
        textMaterial: String,
        imageUris: List<Uri>,
        examName: String,
        difficulty: String,
        numQuestions: Int,
        questionType: String
    ) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading("Extracting data and generating questions...")
            try {
                val model = _selectedModel.value
                val apiKey = getApiKey()

                var extractedText = textMaterial

                // If images are uploaded, let's OCR them with Gemini!
                if (imageUris.isNotEmpty()) {
                    _loadingState.value = LoadingState.Loading("Processing images and extracting text using Gemini AI OCR...")
                    val textBuilder = StringBuilder()
                    if (extractedText.isNotBlank()) {
                        textBuilder.append(extractedText).append("\n\n")
                    }

                    for ((idx, uri) in imageUris.withIndex()) {
                        val base64Image = readImageAsBase64(uri)
                        if (base64Image != null) {
                            val ocrPrompt = "Extract all text, concepts, formulas, diagrams explanation and chapters from this image cleanly. Ignore background noise."
                            val ocrRequest = GeminiRequest(
                                contents = listOf(
                                    GeminiContent(parts = listOf(
                                        GeminiPart(text = ocrPrompt),
                                        GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Image))
                                    ))
                                )
                            )
                            val ocrResponse = RetrofitClient.service.generateContent(model, apiKey, ocrRequest)
                            val ocrText = ocrResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            if (!ocrText.isNullOrBlank()) {
                                textBuilder.append("--- Extracted Notes Page ${idx + 1} ---\n")
                                textBuilder.append(ocrText).append("\n\n")
                            }
                        }
                    }
                    extractedText = textBuilder.toString()
                }

                if (extractedText.isBlank()) {
                    throw Exception("No text material provided or extracted. Please enter notes or select image(s).")
                }

                // Save study material to database
                val materialId = repository.insertStudyMaterial(
                    StudyMaterialEntity(
                        title = title,
                        fileName = if (imageUris.isNotEmpty()) "Uploaded Image Notes (${imageUris.size})" else "Pasted Text Material",
                        fileType = if (imageUris.isNotEmpty()) "Image" else "Text",
                        extractedText = extractedText,
                        importantConcepts = "Pending auto analysis"
                    )
                )

                _loadingState.value = LoadingState.Loading("Creating custom test based ONLY on your notes...")

                val prompt = """
                    Based ONLY on the following uploaded study material/notes, generate a high-quality test of $numQuestions questions.
                    The questions must be exam-oriented, matching the $examName style, with $difficulty difficulty level, and of type $questionType.
                    
                    Study Material/Notes:
                    $extractedText
                    
                    Return a JSON object matching this schema:
                    {
                      "title": "Test from study material: $title",
                      "questions": [
                        {
                          "questionText": "Question description based on notes",
                          "questionType": "$questionType",
                          "options": ["Option A", "Option B", "Option C", "Option D"],
                          "correctAnswer": "Option A", // must match one of the options, or represent True/False for True/False type.
                          "explanation": "Detailed explanation referencing the notes text",
                          "difficulty": "$difficulty",
                          "topicTag": "Notes Section",
                          "estimatedTimeSeconds": 60,
                          "confidenceScore": 0.99,
                          "reference": "Based on provided study material: $title"
                        }
                      ]
                    }
                    
                    Strictly restrict question creation to concepts, formulas, and definitions found in the provided notes. Ensure correctness. Return JSON only without markdown wrapping block.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.4f
                    )
                )

                val response = RetrofitClient.service.generateContent(model, apiKey, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("No response received from model.")

                val cleanJson = cleanJsonString(jsonText)
                val testResponse = parseGeneratedTestResponse(cleanJson)

                if (testResponse == null || testResponse.questions.isEmpty()) {
                    throw Exception("Could not parse test questions from notes. Response: $cleanJson")
                }

                saveTestToDb(
                    testResponse = testResponse,
                    examName = examName,
                    subject = "Study Notes",
                    topic = title,
                    difficulty = difficulty,
                    numQuestions = testResponse.questions.size,
                    language = "English",
                    questionType = questionType,
                    bloomLevel = "Application/Recall",
                    timeLimitMinutes = numQuestions * 2
                )

                _loadingState.value = LoadingState.Success("Successfully converted material into a test!")
                _uiState.value = UiState.Library
            } catch (e: Exception) {
                Log.e("MainViewModel", "Conversion failed", e)
                _loadingState.value = LoadingState.Error("Material conversion failed: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    private fun readImageAsBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            // Resize to prevent huge payload issues (max 1024 width/height)
            val scale = 1024f / Math.max(bitmap.width, bitmap.height)
            val finalBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }

            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to read image as base64", e)
            null
        }
    }

    private fun cleanJsonString(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun parseGeneratedTestResponse(jsonStr: String): GeneratedTestResponse? {
        return try {
            val adapter: JsonAdapter<GeneratedTestResponse> =
                RetrofitClient.moshiInstance.adapter(GeneratedTestResponse::class.java)
            adapter.fromJson(jsonStr)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Moshi parsing error: ${e.message}", e)
            null
        }
    }

    private suspend fun saveTestToDb(
        testResponse: GeneratedTestResponse,
        examName: String,
        subject: String,
        topic: String,
        difficulty: String,
        numQuestions: Int,
        language: String,
        questionType: String,
        bloomLevel: String,
        timeLimitMinutes: Int
    ) = withContext(Dispatchers.IO) {
        val testEntity = TestEntity(
            title = testResponse.title.ifBlank { "Practice Test for $examName" },
            examName = examName,
            subject = subject,
            topic = topic,
            difficulty = difficulty,
            numQuestions = numQuestions,
            language = language,
            questionType = questionType,
            bloomLevel = bloomLevel,
            timeLimitMinutes = timeLimitMinutes
        )

        val testId = repository.insertTest(testEntity).toInt()

        val questionEntities = testResponse.questions.map { q ->
            QuestionEntity(
                testId = testId,
                questionType = q.questionType,
                questionText = q.questionText,
                options = q.options,
                correctAnswer = q.correctAnswer,
                explanation = q.explanation,
                difficulty = q.difficulty,
                topicTag = q.topicTag,
                estimatedTimeSeconds = q.estimatedTimeSeconds,
                confidenceScore = q.confidenceScore,
                reference = q.reference
            )
        }

        repository.insertQuestions(questionEntities)
    }

    // --- CBT Exam Session Operations ---
    fun startTestCbt(test: TestEntity) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading("Loading test environment...")
            try {
                _activeTest.value = test
                val questions = repository.getQuestionsForTestSync(test.id)
                _activeQuestions.value = questions
                _currentQuestionIndex.value = 0
                _userAnswers.value = emptyMap()
                _markedForReview.value = emptySet()
                _elapsedSeconds.value = 0
                _isTimerRunning.value = true

                _uiState.value = UiState.CbtExam
                _loadingState.value = LoadingState.Idle
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Failed to load CBT exam: ${e.message}")
            }
        }
    }

    fun selectAnswer(answer: String) {
        val questions = _activeQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (questions.isNotEmpty() && currentIndex in questions.indices) {
            val qId = questions[currentIndex].id
            val updated = _userAnswers.value.toMutableMap()
            updated[qId] = answer
            _userAnswers.value = updated
        }
    }

    fun clearResponse() {
        val questions = _activeQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (questions.isNotEmpty() && currentIndex in questions.indices) {
            val qId = questions[currentIndex].id
            val updated = _userAnswers.value.toMutableMap()
            updated.remove(qId)
            _userAnswers.value = updated
        }
    }

    fun toggleMarkForReview() {
        val questions = _activeQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (questions.isNotEmpty() && currentIndex in questions.indices) {
            val qId = questions[currentIndex].id
            val currentMarked = _markedForReview.value.toMutableSet()
            if (currentMarked.contains(qId)) {
                currentMarked.remove(qId)
            } else {
                currentMarked.add(qId)
            }
            _markedForReview.value = currentMarked
        }
    }

    fun setCurrentQuestion(index: Int) {
        if (index in _activeQuestions.value.indices) {
            _currentQuestionIndex.value = index
        }
    }

    fun saveAndNext() {
        val currentIndex = _currentQuestionIndex.value
        val total = _activeQuestions.value.size
        if (currentIndex < total - 1) {
            _currentQuestionIndex.value = currentIndex + 1
        }
    }

    fun navigatePrevious() {
        val currentIndex = _currentQuestionIndex.value
        if (currentIndex > 0) {
            _currentQuestionIndex.value = currentIndex - 1
        }
    }

    fun submitTestCbt() {
        viewModelScope.launch {
            _isTimerRunning.value = false
            _loadingState.value = LoadingState.Loading("Evaluating your responses and generating personalized insights...")
            try {
                val test = _activeTest.value ?: return@launch
                val questions = _activeQuestions.value
                val selectedAnswers = _userAnswers.value
                val marked = _markedForReview.value

                var correctCount = 0
                val userAnswersToInsert = questions.map { q ->
                    val selected = selectedAnswers[q.id] ?: ""
                    val isCorrect = selected.trim().lowercase() == q.correctAnswer.trim().lowercase()
                    if (isCorrect) correctCount++

                    UserAnswerEntity(
                        testId = test.id,
                        questionId = q.id,
                        selectedAnswer = selected,
                        isCorrect = isCorrect,
                        isMarkedForReview = marked.contains(q.id),
                        timeSpentSeconds = 10 // Mock/average duration per question
                    )
                }

                // Insert into DB
                repository.insertUserAnswers(userAnswersToInsert)
                _testResultAnswers.value = userAnswersToInsert

                // Build performance calculations
                val accuracy = if (questions.isNotEmpty()) {
                    (correctCount.toFloat() / questions.size) * 100f
                } else {
                    0f
                }

                // Call Gemini to get super high-quality, customized, smart feedback!
                val scoreStr = "$correctCount / ${questions.size}"
                val performanceSummary = "Score: $scoreStr, Accuracy: ${String.format("%.1f", accuracy)}%, Time Spent: ${_elapsedSeconds.value}s."

                var weakTopics = "Review needed"
                var strongTopics = "Well grasped"
                var suggestions = "Practice more custom tests to lock in your mastery."

                try {
                    val model = _selectedModel.value
                    val apiKey = getApiKey()

                    val feedbackPrompt = """
                        The user just completed a test on "${test.topic}" (${test.subject}) for the "${test.examName}" exam.
                        Performance: $performanceSummary
                        
                        Questions & Answers Details:
                        ${questions.mapIndexed { idx, q -> 
                            "Q${idx+1}: ${q.questionText}\nTopic: ${q.topicTag}\nUser selected: ${selectedAnswers[q.id] ?: "Not Answered"}\nCorrect answer: ${q.correctAnswer}\n"
                        }.joinToString("\n")}
                        
                        Review this performance. Provide smart topic analysis in a simple JSON output:
                        {
                          "weakTopics": "topics the user struggled with (comma-separated)",
                          "strongTopics": "topics the user solved correctly (comma-separated)",
                          "improvementSuggestions": "2-3 highly action-oriented study tips customized to their mistakes."
                        }
                    """.trimIndent()

                    val request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = feedbackPrompt)))),
                        generationConfig = GeminiGenerationConfig(responseMimeType = "application/json")
                    )

                    val response = RetrofitClient.service.generateContent(model, apiKey, request)
                    val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (resultText != null) {
                        val cleanResult = cleanJsonString(resultText)
                        val map = parseFeedbackJson(cleanResult)
                        if (map != null) {
                            weakTopics = map["weakTopics"] ?: weakTopics
                            strongTopics = map["strongTopics"] ?: strongTopics
                            suggestions = map["improvementSuggestions"] ?: suggestions
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "AI feedback failed, falling back to local heuristic", e)
                    // local analysis fallback
                    val weakSet = mutableSetOf<String>()
                    val strongSet = mutableSetOf<String>()
                    questions.forEach { q ->
                        val ans = selectedAnswers[q.id] ?: ""
                        if (ans.trim().lowercase() == q.correctAnswer.trim().lowercase()) {
                            strongSet.add(q.topicTag.ifBlank { "General" })
                        } else {
                            weakSet.add(q.topicTag.ifBlank { "General" })
                        }
                    }
                    weakTopics = weakSet.joinToString(", ").ifBlank { "None" }
                    strongTopics = strongSet.joinToString(", ").ifBlank { "None" }
                    suggestions = "Focus on the topics with wrong answers and review detailed explanations carefully."
                }

                val updatedTest = test.copy(
                    score = correctCount,
                    maxScore = questions.size,
                    percentage = (correctCount.toFloat() / questions.size) * 100f,
                    accuracy = accuracy,
                    elapsedTimeSeconds = _elapsedSeconds.value.toLong(),
                    weakTopics = weakTopics,
                    strongTopics = strongTopics,
                    improvementSuggestions = suggestions
                )

                repository.updateTest(updatedTest)
                _activeTest.value = updatedTest

                _loadingState.value = LoadingState.Success("Test submitted successfully!")
                _uiState.value = UiState.Result(updatedTest.id)
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Submission failed: ${e.message}")
            }
        }
    }

    private fun parseFeedbackJson(jsonStr: String): Map<String, String>? {
        return try {
            val type = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val adapter: JsonAdapter<Map<String, String>> = RetrofitClient.moshiInstance.adapter(type)
            adapter.fromJson(jsonStr)
        } catch (e: Exception) {
            null
        }
    }

    // --- Result Detail Loading ---
    fun viewTestResults(testId: Int) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading("Loading results...")
            try {
                val test = repository.getTestById(testId)
                if (test != null) {
                    _activeTest.value = test
                    val questions = repository.getQuestionsForTestSync(testId)
                    _activeQuestions.value = questions

                    // Fetch user responses
                    repository.getUserAnswersForTest(testId).collect { answers ->
                        _testResultAnswers.value = answers
                        _uiState.value = UiState.Result(testId)
                        _loadingState.value = LoadingState.Idle
                    }
                } else {
                    throw Exception("Test not found in database.")
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Failed to load results: ${e.message}")
            }
        }
    }

    // --- Material Deletion ---
    fun deleteStudyMaterial(id: Int) {
        viewModelScope.launch {
            repository.deleteStudyMaterial(id)
        }
    }

    // --- Test Deletion ---
    fun deleteTest(id: Int) {
        viewModelScope.launch {
            repository.deleteTest(id)
        }
    }

    fun dismissLoading() {
        _loadingState.value = LoadingState.Idle
    }
}

// --- Sealed Classes for Navigation and State Management ---

sealed interface UiState {
    object Dashboard : UiState
    object Library : UiState
    object CbtExam : UiState
    data class Result(val testId: Int) : UiState
    object Analytics : UiState
    object Settings : UiState
}

sealed interface LoadingState {
    object Idle : LoadingState
    data class Loading(val message: String) : LoadingState
    data class Success(val message: String) : LoadingState
    data class Error(val message: String) : LoadingState
}

// --- Factory ---
class MainViewModelFactory(
    private val repository: TestRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
