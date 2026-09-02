package com.example.interviewmate.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.interviewmate.data.model.QuestionEntity
import com.example.interviewmate.data.repository.QuestionRepository
import com.example.interviewmate.util.InterviewConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class QuestionUiState(
    val selectedCategory: String = InterviewConstants.AllCategories,
    val questions: List<QuestionEntity> = emptyList(),
    val expandedIds: Set<Long> = emptySet()
)

class QuestionViewModel(
    repository: QuestionRepository
) : ViewModel() {
    private val selectedCategory = MutableStateFlow(InterviewConstants.AllCategories)
    private val expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    val allQuestions: StateFlow<List<QuestionEntity>> =
        repository.questions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<QuestionUiState> =
        combine(
            selectedCategory,
            expandedIds,
            allQuestions
        ) { category, expanded, questions ->
            QuestionUiState(
                selectedCategory = category,
                questions = if (category == InterviewConstants.AllCategories) {
                    questions
                } else {
                    questions.filter { InterviewConstants.normalizeCategory(it.category) == category }
                },
                expandedIds = expanded
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = QuestionUiState()
        )

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun toggleQuestion(id: Long) {
        expandedIds.value = if (id in expandedIds.value) {
            expandedIds.value - id
        } else {
            expandedIds.value + id
        }
    }
}

class QuestionViewModelFactory(
    private val repository: QuestionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionViewModel::class.java)) {
            return QuestionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
