package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Query("SELECT * FROM tests ORDER BY createdTimestamp DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE id = :id LIMIT 1")
    suspend fun getTestById(id: Int): TestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Update
    suspend fun updateTest(test: TestEntity)

    @Query("DELETE FROM tests WHERE id = :id")
    suspend fun deleteTestById(id: Int)

    @Query("SELECT * FROM questions WHERE testId = :testId")
    fun getQuestionsForTest(testId: Int): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE testId = :testId")
    suspend fun getQuestionsForTestSync(testId: Int): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE testId = :testId")
    suspend fun deleteQuestionsForTest(testId: Int)

    @Query("SELECT * FROM user_answers WHERE testId = :testId")
    fun getUserAnswersForTest(testId: Int): Flow<List<UserAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswers(answers: List<UserAnswerEntity>)

    @Query("DELETE FROM user_answers WHERE testId = :testId")
    suspend fun deleteUserAnswersForTest(testId: Int)

    @Query("SELECT * FROM study_materials ORDER BY createdTimestamp DESC")
    fun getAllStudyMaterials(): Flow<List<StudyMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity): Long

    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteStudyMaterialById(id: Int)
}
