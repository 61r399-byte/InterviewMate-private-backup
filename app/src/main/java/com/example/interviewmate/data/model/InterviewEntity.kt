package com.example.interviewmate.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interviews")
data class InterviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val company: String,
    val position: String,
    val date: Long,
    val round: String,
    val result: String,
    val notes: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
