package com.example.interviewmate

import com.example.interviewmate.data.model.InterviewItemEntity
import com.example.interviewmate.data.model.QuestionEntity
import com.example.interviewmate.engine.ReportEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportEngineTest {
    @Test
    fun generate_calculatesTotalAndCategoryAverages() {
        val report = ReportEngine.generate(
            items = listOf(
                item(category = "Algorithms", rating = 2),
                item(category = "Algorithms", rating = 4),
                item(category = "System Design", rating = 5)
            ),
            questions = emptyList()
        )

        assertEquals(3.666f, report.totalAverage, 0.01f)
        assertEquals(3.0f, report.categoryScores.getValue("Algorithms"), 0.01f)
        assertEquals(5.0f, report.categoryScores.getValue("System Design"), 0.01f)
    }

    @Test
    fun generate_usesLowestAppearingCategoryAsWeaknessWhenNotTied() {
        val report = ReportEngine.generate(
            items = listOf(
                item(category = "Algorithms", rating = 2),
                item(category = "System Design", rating = 4)
            ),
            questions = questions()
        )

        assertEquals(listOf("Algorithms"), report.weaknesses)
        assertTrue(report.advice.contains("Algorithms"))
    }

    @Test
    fun generate_recommendsUpToThreeMatchingQuestionsByDifficulty() {
        val report = ReportEngine.generate(
            items = listOf(item(category = "Algorithms", rating = 2)),
            questions = questions()
        )

        assertEquals(listOf("Algorithms"), report.weaknesses)
        assertEquals(3, report.recommendations.size)
        assertEquals(listOf(1, 2, 3), report.recommendations.map { it.difficulty })
        assertTrue(report.recommendations.all { it.category == "Algorithms" })
    }

    @Test
    fun generate_handlesTwoCategoriesTiedForLowestScore() {
        val report = ReportEngine.generate(
            items = listOf(
                item(category = "Algorithms", rating = 2),
                item(category = "Behavioral", rating = 2),
                item(category = "System Design", rating = 4)
            ),
            questions = tiedQuestions()
        )

        assertEquals(listOf("Algorithms", "Behavioral"), report.weaknesses)
        assertEquals(listOf("Algorithms", "Behavioral", "Algorithms"), report.recommendations.map { it.category })
        assertEquals(listOf("Algorithms A1", "Behavioral B1", "Algorithms A2"), report.recommendations.map { it.question })
    }

    @Test
    fun generate_handlesThreeCategoriesTiedForLowestScore() {
        val report = ReportEngine.generate(
            items = listOf(
                item(category = "Algorithms", rating = 2),
                item(category = "Behavioral", rating = 2),
                item(category = "Project Experience", rating = 2),
                item(category = "System Design", rating = 5)
            ),
            questions = tiedQuestions()
        )

        assertEquals(listOf("Algorithms", "Behavioral", "Project Experience"), report.weaknesses)
        assertEquals(listOf("Algorithms", "Behavioral", "Project Experience"), report.recommendations.map { it.category })
        assertEquals(listOf("Algorithms A1", "Behavioral B1", "Project C1"), report.recommendations.map { it.question })
    }

    @Test
    fun generate_fillsFromOtherTiedCategoriesWhenOneHasNoQuestions() {
        val report = ReportEngine.generate(
            items = listOf(
                item(category = "Algorithms", rating = 2),
                item(category = "Behavioral", rating = 2),
                item(category = "System Design", rating = 4)
            ),
            questions = listOf(
                question(id = 1, category = "Algorithms", difficulty = 1, question = "Algorithms A1"),
                question(id = 2, category = "Algorithms", difficulty = 2, question = "Algorithms A2"),
                question(id = 3, category = "Algorithms", difficulty = 3, question = "Algorithms A3")
            )
        )

        assertEquals(listOf("Algorithms", "Behavioral"), report.weaknesses)
        assertEquals(listOf("Algorithms A1", "Algorithms A2", "Algorithms A3"), report.recommendations.map { it.question })
    }

    @Test
    fun generate_doesNotTreatMissingCategoriesAsZeroScoreWeaknesses() {
        val report = ReportEngine.generate(
            items = listOf(
                item(category = "Algorithms", rating = 2),
                item(category = "System Design", rating = 4)
            ),
            questions = tiedQuestions()
        )

        assertEquals(listOf("Algorithms"), report.weaknesses)
        assertTrue("Behavioral" !in report.weaknesses)
        assertTrue("Project Experience" !in report.weaknesses)
    }

    @Test
    fun generate_deduplicatesRecommendedQuestions() {
        val duplicatedQuestion = question(id = 1, category = "Algorithms", difficulty = 1, question = "Two Sum")
        val report = ReportEngine.generate(
            items = listOf(item(category = "Algorithms", rating = 2)),
            questions = listOf(
                duplicatedQuestion,
                duplicatedQuestion.copy(id = 99),
                question(id = 2, category = "Algorithms", difficulty = 2, question = "Reverse Linked List")
            )
        )

        assertEquals(listOf("Algorithms"), report.weaknesses)
        assertEquals(listOf("Two Sum", "Reverse Linked List"), report.recommendations.map { it.question })
    }

    @Test
    fun generate_handlesEmptyItems() {
        val report = ReportEngine.generate(
            items = emptyList(),
            questions = questions()
        )

        assertEquals(0f, report.totalAverage, 0.0f)
        assertTrue(report.categoryScores.isEmpty())
        assertTrue(report.weaknesses.isEmpty())
        assertTrue(report.recommendations.isEmpty())
    }

    @Test
    fun generate_handlesSingleCategory() {
        val report = ReportEngine.generate(
            items = listOf(
                item(category = "Project Experience", rating = 4),
                item(category = "Project Experience", rating = 5)
            ),
            questions = listOf(question(id = 8, category = "Project Experience", difficulty = 3))
        )

        assertEquals(listOf("Project Experience"), report.weaknesses)
        assertEquals(4.5f, report.categoryScores.getValue("Project Experience"), 0.01f)
        assertEquals(1, report.recommendations.size)
    }

    @Test
    fun generate_handlesNoMatchingQuestionBankItems() {
        val report = ReportEngine.generate(
            items = listOf(item(category = "Behavioral", rating = 2)),
            questions = listOf(question(id = 1, category = "Algorithms", difficulty = 1))
        )

        assertEquals(listOf("Behavioral"), report.weaknesses)
        assertTrue(report.recommendations.isEmpty())
    }

    private fun item(
        category: String,
        rating: Int
    ) = InterviewItemEntity(
        interviewId = 1,
        category = category,
        question = "$category question",
        myAnswer = "answer",
        selfRating = rating
    )

    private fun questions(): List<QuestionEntity> = listOf(
        question(id = 1, category = "Algorithms", difficulty = 1, question = "Two Sum"),
        question(id = 2, category = "Algorithms", difficulty = 2, question = "Reverse Linked List"),
        question(id = 3, category = "Algorithms", difficulty = 3, question = "Binary Tree Depth"),
        question(id = 4, category = "Algorithms", difficulty = 4, question = "LRU Cache"),
        question(id = 5, category = "System Design", difficulty = 2, question = "Short URL")
    )

    private fun tiedQuestions(): List<QuestionEntity> = listOf(
        question(id = 1, category = "Algorithms", difficulty = 1, question = "Algorithms A1"),
        question(id = 2, category = "Algorithms", difficulty = 2, question = "Algorithms A2"),
        question(id = 3, category = "Algorithms", difficulty = 3, question = "Algorithms A3"),
        question(id = 4, category = "Behavioral", difficulty = 1, question = "Behavioral B1"),
        question(id = 5, category = "Behavioral", difficulty = 2, question = "Behavioral B2"),
        question(id = 6, category = "Project Experience", difficulty = 1, question = "Project C1"),
        question(id = 7, category = "Project Experience", difficulty = 2, question = "Project C2"),
        question(id = 8, category = "System Design", difficulty = 1, question = "System D1")
    )

    private fun question(
        id: Long,
        category: String,
        difficulty: Int,
        question: String = "$category seed $id"
    ) = QuestionEntity(
        id = id,
        category = category,
        question = question,
        answerHint = "hint",
        difficulty = difficulty,
        company = "General"
    )
}
