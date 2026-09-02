package com.example.interviewmate.ui.report

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interviewmate.data.model.QuestionEntity
import com.example.interviewmate.engine.ReportData
import com.example.interviewmate.engine.ReportEngine
import com.example.interviewmate.ui.interview.InterviewViewModel
import com.example.interviewmate.ui.question.QuestionViewModel
import com.example.interviewmate.util.InterviewConstants
import java.util.Locale

@Composable
fun ReportScreen(
    interviewId: Long,
    interviewViewModel: InterviewViewModel,
    questionViewModel: QuestionViewModel,
    onNavigateBack: () -> Unit
) {
    val interviewWithItems by interviewViewModel.observeInterview(interviewId)
        .collectAsStateWithLifecycle(initialValue = null)
    val questions by questionViewModel.allQuestions.collectAsStateWithLifecycle()

    val current = interviewWithItems
    if (current == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Generating review report",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val report = ReportEngine.generate(
        items = current.items,
        questions = questions
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Review Report",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        item {
            ReportSummaryCard(
                company = current.interview.company,
                position = current.interview.position,
                report = report
            )
        }

        item {
            CategoryScoreCard(report = report)
        }

        item {
            AdviceCard(report = report)
        }

        item {
            Text(
                text = "Recommended Practice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (report.recommendations.isEmpty()) {
            item {
                EmptyRecommendationCard(weaknesses = report.weaknesses)
            }
        } else {
            items(
                items = report.recommendations,
                key = { it.id }
            ) { question ->
                RecommendationCard(question)
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(
    company: String,
    position: String,
    report: ReportData
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "$company - $position",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatScore(report.totalAverage),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            LinearProgressIndicator(
                progress = { (report.totalAverage / 5f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryScoreCard(report: ReportData) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Category Scores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            InterviewConstants.Categories.forEach { category ->
                val score = report.categoryScores[category]
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = score?.let { formatScore(it) } ?: "Not Seen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (category in report.weaknesses) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    LinearProgressIndicator(
                        progress = { ((score ?: 0f) / 5f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AdviceCard(report: ReportData) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(weaknessSummary(report.weaknesses)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${report.recommendations.size} recommended") }
                )
            }
            Text(
                text = report.advice,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RecommendationCard(question: QuestionEntity) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "Difficulty ${question.difficulty}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = question.answerHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyRecommendationCard(weaknesses: List<String>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = if (weaknesses.isEmpty()) {
                "Add interview questions to get practice recommendations based on weak areas."
            } else {
                "No matching questions are available for ${weaknesses.joinToString(", ")} yet."
            },
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatScore(score: Float): String {
    return String.format(Locale.getDefault(), "%.1f", score)
}

private fun weaknessSummary(weaknesses: List<String>): String {
    return when (weaknesses.size) {
        0 -> "No weak areas yet"
        1 -> "${weaknesses.first()} is the current weak area"
        else -> "${weaknesses.joinToString(", ")} are tied as weak areas"
    }
}
