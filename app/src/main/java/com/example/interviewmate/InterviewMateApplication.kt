package com.example.interviewmate

import android.app.Application
import android.content.Context
import com.example.interviewmate.data.local.AppDatabase
import com.example.interviewmate.data.local.QuestionSeedLoader
import com.example.interviewmate.data.local.RoomQuestionSeedTransactionRunner
import com.example.interviewmate.data.local.SharedPreferencesQuestionSeedVersionStore
import com.example.interviewmate.data.repository.InterviewRepository
import com.example.interviewmate.data.repository.QuestionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class InterviewMateApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = AppDatabase.getDatabase(context)

    val interviewRepository = InterviewRepository(database.interviewDao())
    val questionRepository = QuestionRepository(
        questionDao = database.questionDao(),
        seedSource = QuestionSeedLoader(context.applicationContext),
        versionStore = SharedPreferencesQuestionSeedVersionStore(context.applicationContext),
        transactionRunner = RoomQuestionSeedTransactionRunner(database)
    )

    init {
        applicationScope.launch {
            questionRepository.seedQuestionsIfNeeded()
        }
    }
}
