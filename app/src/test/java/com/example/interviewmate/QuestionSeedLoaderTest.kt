package com.example.interviewmate

import com.example.interviewmate.data.local.QuestionSeedLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuestionSeedLoaderTest {
    @Test
    fun parseQuestions_readsFiftyValidFormalQuestions() {
        val questions = loadFormalQuestions()

        assertEquals(50, questions.size)
    }

    @Test
    fun parseQuestions_hasExpectedCategoryCounts() {
        val categoryCounts = loadFormalQuestions().groupingBy { it.category }.eachCount()

        assertEquals(15, categoryCounts["Algorithms"])
        assertEquals(12, categoryCounts["System Design"])
        assertEquals(11, categoryCounts["Project Experience"])
        assertEquals(12, categoryCounts["Behavioral"])
    }

    @Test
    fun parseQuestions_allowsNullCompany() {
        val questions = loadFormalQuestions()
        val parsed = QuestionSeedLoader.parseQuestions(
            """
            [
              {
                "category": "Project Experience",
                "question": "Introduce a project.",
                "answerHint": "Explain the background, action, and result.",
                "difficulty": 3,
                "company": null
              }
            ]
            """.trimIndent()
        )

        assertTrue(questions.any { it.company == null })
        assertEquals(1, parsed.size)
        assertNull(parsed.single().company)
    }

    private fun loadFormalQuestions() = QuestionSeedLoader.parseQuestions(questionsJsonFile().readText())

    private fun questionsJsonFile(): File {
        val modulePath = File("src/main/assets/questions.json")
        return if (modulePath.exists()) {
            modulePath
        } else {
            File("app/src/main/assets/questions.json")
        }
    }
}
