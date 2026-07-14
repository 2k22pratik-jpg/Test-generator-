package com.example.data

import kotlinx.coroutines.flow.Flow

class TestRepository(private val testDao: TestDao) {
    val allTests: Flow<List<TestEntity>> = testDao.getAllTests()
    val allStudyMaterials: Flow<List<StudyMaterialEntity>> = testDao.getAllStudyMaterials()

    suspend fun getTestById(id: Int): TestEntity? {
        return testDao.getTestById(id)
    }

    suspend fun insertTest(test: TestEntity): Long {
        return testDao.insertTest(test)
    }

    suspend fun updateTest(test: TestEntity) {
        testDao.updateTest(test)
    }

    suspend fun deleteTest(id: Int) {
        testDao.deleteTestById(id)
        testDao.deleteQuestionsForTest(id)
        testDao.deleteUserAnswersForTest(id)
    }

    fun getQuestionsForTest(testId: Int): Flow<List<QuestionEntity>> {
        return testDao.getQuestionsForTest(testId)
    }

    suspend fun getQuestionsForTestSync(testId: Int): List<QuestionEntity> {
        return testDao.getQuestionsForTestSync(testId)
    }

    suspend fun insertQuestions(questions: List<QuestionEntity>) {
        testDao.insertQuestions(questions)
    }

    fun getUserAnswersForTest(testId: Int): Flow<List<UserAnswerEntity>> {
        return testDao.getUserAnswersForTest(testId)
    }

    suspend fun insertUserAnswers(answers: List<UserAnswerEntity>) {
        testDao.insertUserAnswers(answers)
    }

    suspend fun insertStudyMaterial(material: StudyMaterialEntity): Long {
        return testDao.insertStudyMaterial(material)
    }

    suspend fun deleteStudyMaterial(id: Int) {
        testDao.deleteStudyMaterialById(id)
    }
}
