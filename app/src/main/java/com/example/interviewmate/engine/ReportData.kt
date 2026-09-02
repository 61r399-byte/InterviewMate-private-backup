package com.example.interviewmate.engine

import com.example.interviewmate.data.model.QuestionEntity

data class ReportData(
    val totalAverage: Float,
    val summary: String,
    val categoryScores: Map<String, Float>,
    val weaknesses: List<String>,
    val recommendations: List<QuestionEntity>,
    val advice: String
)
