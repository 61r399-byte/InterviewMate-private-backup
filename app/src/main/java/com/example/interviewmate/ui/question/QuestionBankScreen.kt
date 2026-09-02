package com.example.interviewmate.ui.question

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interviewmate.data.model.QuestionEntity
import com.example.interviewmate.util.InterviewConstants

@Composable
fun QuestionBankScreen(
    viewModel: QuestionViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = listOf(InterviewConstants.AllCategories) + InterviewConstants.Categories

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Question Bank",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Browse practice questions by category",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = category == uiState.selectedCategory,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Showing ${uiState.questions.size} questions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.questions.isEmpty()) {
            EmptyQuestionBank(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = uiState.questions,
                    key = { it.id }
                ) { question ->
                    QuestionCard(
                        question = question,
                        expanded = question.id in uiState.expandedIds,
                        onToggle = { viewModel.toggleQuestion(question.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyQuestionBank(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 96.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "No question bank data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The formal question bank is imported from questions.json on startup",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestionCard(
    question: QuestionEntity,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = InterviewConstants.normalizeCategory(question.category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onToggle, label = { Text("Difficulty ${question.difficulty}") })
                question.company?.takeIf { it.isNotBlank() }?.let { company ->
                    AssistChip(onClick = onToggle, label = { Text(company) })
                }
            }
            if (expanded) {
                Text(
                    text = question.answerHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
