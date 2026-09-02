package com.example.interviewmate.engine

import com.example.interviewmate.data.model.InterviewItemEntity
import com.example.interviewmate.data.model.QuestionEntity
import com.example.interviewmate.util.InterviewConstants
import kotlin.math.abs

object ReportEngine {
    private const val Epsilon = 0.001f
    private const val MaxRecommendations = 3

    fun generate(
        items: List<InterviewItemEntity>,
        questions: List<QuestionEntity>
    ): ReportData {
        if (items.isEmpty()) {
            return ReportData(
                totalAverage = 0f,
                summary = "No questions have been added yet, so a full review is not available.",
                categoryScores = emptyMap(),
                weaknesses = emptyList(),
                recommendations = emptyList(),
                advice = "Add at least one interview question first, then use self-ratings to generate review advice."
            )
        }

        val totalAverage = items.map { it.selfRating }.average().toFloat()
        val categoryScores = items
            .groupBy { InterviewConstants.normalizeCategory(it.category) }
            .mapValues { (_, categoryItems) ->
                categoryItems.map { it.selfRating }.average().toFloat()
            }
        val minimumAverage = categoryScores.values.minOrNull()
        val weaknesses = if (minimumAverage == null) {
            emptyList()
        } else {
            categoryScores
                .filterValues { abs(it - minimumAverage) <= Epsilon }
                .keys
                .sortedWith(compareBy(::categoryRank).thenBy { it })
        }

        return ReportData(
            totalAverage = totalAverage,
            summary = summaryFor(totalAverage),
            categoryScores = categoryScores,
            weaknesses = weaknesses,
            recommendations = recommendationsFor(weaknesses, questions),
            advice = adviceFor(weaknesses, minimumAverage)
        )
    }

    private fun summaryFor(average: Float): String {
        return when {
            average >= 4.0f -> "Excellent performance with balanced strengths across areas."
            average >= 3.0f -> "Solid overall performance, with a few areas still worth improving."
            average >= 2.0f -> "There is room to improve, so focused practice is recommended."
            else -> "Start with the fundamentals and build up systematically."
        }
    }

    private fun adviceFor(weaknesses: List<String>, weaknessAverage: Float?): String {
        if (weaknesses.isEmpty() || weaknessAverage == null) {
            return "This interview does not have enough questions yet. Add more items before reviewing weak areas."
        }

        val weaknessText = weaknesses.joinToString(", ")
        return when {
            weaknessAverage < 2.0f -> "$weaknessText needs urgent attention. Start with foundational questions."
            weaknessAverage < 3.0f -> "$weaknessText is a clear weak area. Set aside targeted practice time."
            weaknessAverage < 3.5f -> "$weaknessText is not solid yet. Keep reinforcing it with review and drills."
            else -> "$weaknessText is mostly on track. Use medium and advanced questions to keep improving."
        }
    }

    private fun recommendationsFor(
        weaknesses: List<String>,
        questions: List<QuestionEntity>
    ): List<QuestionEntity> {
        if (weaknesses.isEmpty()) return emptyList()

        val candidatesByCategory = weaknesses.associateWith { category ->
            questions
                .asSequence()
                .filter { InterviewConstants.normalizeCategory(it.category) == category }
                .sortedWith(compareBy<QuestionEntity> { it.difficulty }.thenBy { it.id })
                .toList()
        }
        val selected = mutableListOf<QuestionEntity>()
        val selectedQuestionKeys = mutableSetOf<String>()

        while (selected.size < MaxRecommendations) {
            var addedThisRound = false

            for (category in weaknesses) {
                if (selected.size >= MaxRecommendations) break

                val nextQuestion = candidatesByCategory
                    .getValue(category)
                    .firstOrNull { it.normalizedQuestionKey() !in selectedQuestionKeys }

                if (nextQuestion != null) {
                    selected += nextQuestion
                    selectedQuestionKeys += nextQuestion.normalizedQuestionKey()
                    addedThisRound = true
                }
            }

            if (!addedThisRound) break
        }

        return selected
    }

    private fun categoryRank(category: String): Int {
        val index = InterviewConstants.Categories.indexOf(InterviewConstants.normalizeCategory(category))
        return if (index == -1) Int.MAX_VALUE else index
    }

    private fun QuestionEntity.normalizedQuestionKey(): String {
        return question.trim()
    }
}
