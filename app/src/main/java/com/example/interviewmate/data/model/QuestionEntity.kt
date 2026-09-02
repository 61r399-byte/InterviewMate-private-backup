package com.example.interviewmate.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val question: String,
    @ColumnInfo(name = "answer_hint")
    val answerHint: String,
    val difficulty: Int,
    val company: String?
)
