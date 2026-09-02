package com.example.interviewmate.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "interview_items",
    foreignKeys = [
        ForeignKey(
            entity = InterviewEntity::class,
            parentColumns = ["id"],
            childColumns = ["interview_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("interview_id")]
)
data class InterviewItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "interview_id")
    val interviewId: Long,
    val category: String,
    val question: String,
    @ColumnInfo(name = "my_answer")
    val myAnswer: String,
    @ColumnInfo(name = "self_rating")
    val selfRating: Int
)
