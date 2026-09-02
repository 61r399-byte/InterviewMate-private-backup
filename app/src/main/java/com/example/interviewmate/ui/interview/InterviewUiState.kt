package com.example.interviewmate.ui.interview

import com.example.interviewmate.data.model.InterviewEntity
import com.example.interviewmate.data.model.InterviewItemEntity
import com.example.interviewmate.data.model.InterviewWithItems
import com.example.interviewmate.util.InterviewConstants
import com.example.interviewmate.util.formatDate
import com.example.interviewmate.util.parseDateOrNull
import com.example.interviewmate.util.todayText
import java.util.UUID

data class InterviewListUiState(
    val query: String = "",
    val interviews: List<InterviewWithItems> = emptyList()
)

data class InterviewFormState(
    val id: Long = 0,
    val company: String = "",
    val position: String = "",
    val dateText: String = todayText(),
    val round: String = InterviewConstants.Rounds.first(),
    val result: String = InterviewConstants.ResultPending,
    val notes: String = "",
    val items: List<InterviewItemForm> = listOf(emptyInterviewItemForm())
)

data class InterviewItemForm(
    val localId: String = UUID.randomUUID().toString(),
    val category: String = InterviewConstants.Categories.first(),
    val question: String = "",
    val myAnswer: String = "",
    val selfRating: Int = 3
)

fun emptyInterviewItemForm(): InterviewItemForm = InterviewItemForm()

fun InterviewFormState.validate(): List<String> {
    val errors = mutableListOf<String>()
    if (company.isBlank()) errors += "Company name cannot be blank"
    if (position.isBlank()) errors += "Role cannot be blank"
    if (parseDateOrNull(dateText) == null) errors += "Date must use yyyy-MM-dd"
    if (items.isEmpty()) errors += "Add at least one interview question"

    items.forEachIndexed { index, item ->
        if (item.question.isBlank()) {
            errors += "Question ${index + 1} cannot be blank"
        }
        if (InterviewConstants.normalizeCategory(item.category) !in InterviewConstants.Categories) {
            errors += "Question ${index + 1} has an invalid category"
        }
        if (item.selfRating !in 1..5) {
            errors += "Question ${index + 1} self-rating must be from 1 to 5"
        }
    }

    return errors
}

fun InterviewFormState.toInterviewEntity(existing: InterviewEntity?): InterviewEntity {
    return InterviewEntity(
        id = id,
        company = company.trim(),
        position = position.trim(),
        date = parseDateOrNull(dateText) ?: System.currentTimeMillis(),
        round = InterviewConstants.normalizeRound(round),
        result = InterviewConstants.normalizeResult(result),
        notes = notes.trim(),
        createdAt = existing?.createdAt ?: System.currentTimeMillis()
    )
}

fun InterviewFormState.toItemEntities(): List<InterviewItemEntity> {
    return items.map {
        InterviewItemEntity(
            interviewId = id,
            category = InterviewConstants.normalizeCategory(it.category),
            question = it.question.trim(),
            myAnswer = it.myAnswer.trim(),
            selfRating = it.selfRating
        )
    }
}

fun InterviewWithItems.toFormState(): InterviewFormState {
    return InterviewFormState(
        id = interview.id,
        company = interview.company,
        position = interview.position,
        dateText = formatDate(interview.date),
        round = InterviewConstants.normalizeRound(interview.round),
        result = InterviewConstants.normalizeResult(interview.result),
        notes = interview.notes,
        items = if (items.isEmpty()) {
            listOf(emptyInterviewItemForm())
        } else {
            items.map {
                InterviewItemForm(
                    category = InterviewConstants.normalizeCategory(it.category),
                    question = it.question,
                    myAnswer = it.myAnswer,
                    selfRating = it.selfRating
                )
            }
        }
    )
}
