package com.example.interviewmate.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class InterviewWithItems(
    @Embedded
    val interview: InterviewEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "interview_id"
    )
    val items: List<InterviewItemEntity>
)
