package com.example.interviewmate.data.repository

import com.example.interviewmate.data.local.InterviewDao
import com.example.interviewmate.data.model.InterviewEntity
import com.example.interviewmate.data.model.InterviewItemEntity
import com.example.interviewmate.data.model.InterviewWithItems
import kotlinx.coroutines.flow.Flow

class InterviewRepository(
    private val interviewDao: InterviewDao
) {
    fun observeInterviews(query: String): Flow<List<InterviewWithItems>> {
        val trimmed = query.trim()
        return if (trimmed.isBlank()) {
            interviewDao.observeInterviews()
        } else {
            interviewDao.searchInterviews(trimmed)
        }
    }

    fun observeInterview(id: Long): Flow<InterviewWithItems?> = interviewDao.observeInterview(id)

    suspend fun saveInterview(
        interview: InterviewEntity,
        items: List<InterviewItemEntity>
    ): Long = interviewDao.saveInterviewWithItems(interview, items)

    suspend fun deleteInterview(id: Long) {
        interviewDao.deleteInterviewById(id)
    }
}
