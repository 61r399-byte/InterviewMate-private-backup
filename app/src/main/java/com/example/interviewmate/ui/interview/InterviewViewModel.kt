package com.example.interviewmate.ui.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.interviewmate.data.model.InterviewEntity
import com.example.interviewmate.data.model.InterviewWithItems
import com.example.interviewmate.data.repository.InterviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InterviewViewModel(
    private val repository: InterviewRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")

    val allInterviews: StateFlow<List<InterviewWithItems>> =
        repository.observeInterviews("")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val listUiState: StateFlow<InterviewListUiState> =
        combine(searchQuery, allInterviews) { query, interviews ->
            val trimmed = query.trim()
            val filtered = if (trimmed.isBlank()) {
                interviews
            } else {
                interviews.filter {
                    it.interview.company.contains(trimmed, ignoreCase = true) ||
                        it.interview.position.contains(trimmed, ignoreCase = true)
                }
            }
            InterviewListUiState(query = query, interviews = filtered)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InterviewListUiState()
        )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun observeInterview(id: Long): Flow<InterviewWithItems?> = repository.observeInterview(id)

    fun saveInterview(
        formState: InterviewFormState,
        existingInterview: InterviewEntity?,
        onSaved: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        val errors = formState.validate()
        if (errors.isNotEmpty()) {
            onError(errors.joinToString(separator = "\n"))
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.saveInterview(
                    interview = formState.toInterviewEntity(existingInterview),
                    items = formState.toItemEntities()
                )
            }.onSuccess(onSaved)
                .onFailure { onError("Save failed: ${it.message ?: "Unknown error"}") }
        }
    }

    fun deleteInterview(
        id: Long,
        onDeleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                repository.deleteInterview(id)
            }.onSuccess {
                onDeleted()
            }.onFailure {
                onError("Delete failed: ${it.message ?: "Unknown error"}")
            }
        }
    }
}

class InterviewViewModelFactory(
    private val repository: InterviewRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InterviewViewModel::class.java)) {
            return InterviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
