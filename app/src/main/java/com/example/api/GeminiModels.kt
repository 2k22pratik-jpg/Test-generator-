package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class ResponseFormatText(
    @Json(name = "mimeType") val mimeType: String
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    @Json(name = "text") val text: ResponseFormatText? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

// --- Domain Models for Parsed AI Questions ---

@JsonClass(generateAdapter = true)
data class GeneratedQuestion(
    @Json(name = "questionText") val questionText: String,
    @Json(name = "questionType") val questionType: String,
    @Json(name = "options") val options: List<String> = emptyList(),
    @Json(name = "correctAnswer") val correctAnswer: String,
    @Json(name = "explanation") val explanation: String,
    @Json(name = "difficulty") val difficulty: String = "Medium",
    @Json(name = "topicTag") val topicTag: String = "",
    @Json(name = "estimatedTimeSeconds") val estimatedTimeSeconds: Int = 60,
    @Json(name = "confidenceScore") val confidenceScore: Float = 0.9f,
    @Json(name = "reference") val reference: String = ""
)

@JsonClass(generateAdapter = true)
data class GeneratedTestResponse(
    @Json(name = "title") val title: String,
    @Json(name = "questions") val questions: List<GeneratedQuestion>
)
