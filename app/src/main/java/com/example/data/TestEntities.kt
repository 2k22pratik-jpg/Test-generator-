package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val examName: String,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val numQuestions: Int,
    val language: String,
    val questionType: String,
    val bloomLevel: String,
    val timeLimitMinutes: Int,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val score: Int? = null,
    val maxScore: Int = 0,
    val percentage: Float? = null,
    val accuracy: Float? = null,
    val elapsedTimeSeconds: Long? = null,
    val weakTopics: String = "", // comma-separated or JSON
    val strongTopics: String = "", // comma-separated or JSON
    val improvementSuggestions: String = ""
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val testId: Int,
    val questionType: String,
    val questionText: String,
    val options: List<String>, // converted using Room TypeConverter
    val correctAnswer: String,
    val explanation: String,
    val difficulty: String = "",
    val topicTag: String = "",
    val estimatedTimeSeconds: Int = 60,
    val confidenceScore: Float = 1.0f,
    val reference: String = ""
)

@Entity(tableName = "user_answers")
data class UserAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val testId: Int,
    val questionId: Int,
    val selectedAnswer: String,
    val isCorrect: Boolean,
    val isMarkedForReview: Boolean = false,
    val timeSpentSeconds: Int = 0
)

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val fileName: String,
    val fileType: String,
    val extractedText: String,
    val importantConcepts: String = "", // key concepts extracted
    val createdTimestamp: Long = System.currentTimeMillis()
)

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listStringAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { listStringAdapter.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { listStringAdapter.fromJson(it) }
    }
}
