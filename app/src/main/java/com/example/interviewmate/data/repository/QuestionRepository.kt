package com.example.interviewmate.data.repository

import com.example.interviewmate.data.local.QuestionDao
import com.example.interviewmate.data.local.QuestionSeedSource
import com.example.interviewmate.data.local.QuestionSeedTransactionRunner
import com.example.interviewmate.data.local.QuestionSeedVersionStore
import com.example.interviewmate.data.model.QuestionEntity
import kotlinx.coroutines.flow.Flow

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val seedSource: QuestionSeedSource,
    private val versionStore: QuestionSeedVersionStore,
    private val transactionRunner: QuestionSeedTransactionRunner
) {
    val questions: Flow<List<QuestionEntity>> = questionDao.observeQuestions()

    suspend fun seedQuestionsIfNeeded() {
        if (versionStore.getImportedVersion() >= QUESTION_SEED_VERSION) {
            return
        }

        val seedQuestions = seedSource.loadQuestions()
        if (seedQuestions.isEmpty()) {
            return
        }

        val questionCount = transactionRunner.runInTransaction {
            questionDao.deleteAllQuestions()
            questionDao.insertAllQuestions(seedQuestions)
            questionDao.getQuestionCount()
        }

        if (questionCount == seedQuestions.size) {
            versionStore.setImportedVersion(QUESTION_SEED_VERSION)
        }
    }

    companion object {
        const val QUESTION_SEED_VERSION = 3
    }
}
