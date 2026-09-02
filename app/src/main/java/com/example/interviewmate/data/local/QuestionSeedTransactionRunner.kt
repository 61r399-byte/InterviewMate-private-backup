package com.example.interviewmate.data.local

import androidx.room.withTransaction

interface QuestionSeedTransactionRunner {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}

class RoomQuestionSeedTransactionRunner(
    private val database: AppDatabase
) : QuestionSeedTransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database.withTransaction(block)
    }
}
