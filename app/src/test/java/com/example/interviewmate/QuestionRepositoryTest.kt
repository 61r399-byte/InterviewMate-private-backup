package com.example.interviewmate

import com.example.interviewmate.data.local.QuestionDao
import com.example.interviewmate.data.local.QuestionSeedLoader
import com.example.interviewmate.data.local.QuestionSeedSource
import com.example.interviewmate.data.local.QuestionSeedTransactionRunner
import com.example.interviewmate.data.local.QuestionSeedVersionStore
import com.example.interviewmate.data.model.QuestionEntity
import com.example.interviewmate.data.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuestionRepositoryTest {
    @Test
    fun seedQuestionsIfNeeded_doesNotDuplicateQuestionsWhenStartedAgainAtSameVersion() = runBlocking {
        val state = FakeLocalState()
        val dao = FakeQuestionDao(state)
        val seedSource = FakeQuestionSeedSource(loadFormalQuestions())
        val versionStore = FakeQuestionSeedVersionStore(initialVersion = 0)
        val transactionRunner = FakeQuestionSeedTransactionRunner()
        val repository = repository(dao, seedSource, versionStore, transactionRunner)

        repository.seedQuestionsIfNeeded()
        repository.seedQuestionsIfNeeded()

        assertEquals(50, state.questions.size)
        assertEquals(1, dao.deleteCalls)
        assertEquals(1, seedSource.loadCalls)
        assertEquals(1, transactionRunner.transactionCalls)
        assertEquals(QuestionRepository.QUESTION_SEED_VERSION, versionStore.currentVersion)
    }

    @Test
    fun seedQuestionsIfNeeded_replacesOldSeedVersionWithFormalQuestionBank() = runBlocking {
        val state = FakeLocalState(
            questions = oldTemporaryQuestions().toMutableList()
        )
        val dao = FakeQuestionDao(state)
        val versionStore = FakeQuestionSeedVersionStore(initialVersion = 1)
        val repository = repository(
            dao = dao,
            seedSource = FakeQuestionSeedSource(loadFormalQuestions()),
            versionStore = versionStore,
            transactionRunner = FakeQuestionSeedTransactionRunner()
        )

        repository.seedQuestionsIfNeeded()

        assertEquals(50, state.questions.size)
        assertEquals(QuestionRepository.QUESTION_SEED_VERSION, versionStore.currentVersion)
        assertTrue(state.questions.none { it.question.startsWith("old temporary") })
    }

    @Test
    fun seedQuestionsIfNeeded_keepsInterviewRecordsWhenUpdatingQuestions() = runBlocking {
        val state = FakeLocalState(
            questions = oldTemporaryQuestions().toMutableList(),
            interviews = mutableListOf("interview-1", "interview-2")
        )
        val repository = repository(
            dao = FakeQuestionDao(state),
            seedSource = FakeQuestionSeedSource(loadFormalQuestions()),
            versionStore = FakeQuestionSeedVersionStore(initialVersion = 1),
            transactionRunner = FakeQuestionSeedTransactionRunner()
        )

        repository.seedQuestionsIfNeeded()

        assertEquals(50, state.questions.size)
        assertEquals(listOf("interview-1", "interview-2"), state.interviews)
    }

    private fun repository(
        dao: QuestionDao,
        seedSource: QuestionSeedSource,
        versionStore: QuestionSeedVersionStore,
        transactionRunner: QuestionSeedTransactionRunner
    ) = QuestionRepository(
        questionDao = dao,
        seedSource = seedSource,
        versionStore = versionStore,
        transactionRunner = transactionRunner
    )

    private fun loadFormalQuestions() = QuestionSeedLoader.parseQuestions(questionsJsonFile().readText())

    private fun oldTemporaryQuestions(): List<QuestionEntity> {
        return (1..16).map { index ->
            QuestionEntity(
                id = index.toLong(),
                category = "Algorithms",
                question = "old temporary question $index",
                answerHint = "old hint",
                difficulty = 1,
                company = "General"
            )
        }
    }

    private fun questionsJsonFile(): File {
        val modulePath = File("src/main/assets/questions.json")
        return if (modulePath.exists()) {
            modulePath
        } else {
            File("app/src/main/assets/questions.json")
        }
    }

    private data class FakeLocalState(
        val questions: MutableList<QuestionEntity> = mutableListOf(),
        val interviews: MutableList<String> = mutableListOf()
    )

    private class FakeQuestionDao(
        private val state: FakeLocalState
    ) : QuestionDao {
        var deleteCalls = 0
            private set
        private var nextId = (state.questions.maxOfOrNull { it.id } ?: 0L) + 1L

        override fun observeQuestions(): Flow<List<QuestionEntity>> {
            return flowOf(state.questions.toList())
        }

        override suspend fun getQuestionCount(): Int {
            return state.questions.size
        }

        override suspend fun deleteAllQuestions() {
            deleteCalls += 1
            state.questions.clear()
        }

        override suspend fun insertAllQuestions(questions: List<QuestionEntity>): List<Long> {
            return questions.map { question ->
                val id = if (question.id == 0L) nextId++ else question.id
                state.questions += question.copy(id = id)
                id
            }
        }
    }

    private class FakeQuestionSeedSource(
        private val questions: List<QuestionEntity>
    ) : QuestionSeedSource {
        var loadCalls = 0
            private set

        override fun loadQuestions(): List<QuestionEntity> {
            loadCalls += 1
            return questions
        }
    }

    private class FakeQuestionSeedVersionStore(
        initialVersion: Int
    ) : QuestionSeedVersionStore {
        var currentVersion = initialVersion
            private set

        override fun getImportedVersion(): Int {
            return currentVersion
        }

        override fun setImportedVersion(version: Int) {
            currentVersion = version
        }
    }

    private class FakeQuestionSeedTransactionRunner : QuestionSeedTransactionRunner {
        var transactionCalls = 0
            private set

        override suspend fun <T> runInTransaction(block: suspend () -> T): T {
            transactionCalls += 1
            return block()
        }
    }
}
