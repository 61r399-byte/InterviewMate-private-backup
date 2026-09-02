package com.example.interviewmate.ui.interview

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interviewmate.data.model.InterviewItemEntity
import com.example.interviewmate.util.InterviewConstants
import com.example.interviewmate.util.formatDate
import java.util.Locale

@Composable
fun InterviewDetailScreen(
    interviewId: Long,
    viewModel: InterviewViewModel,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenReport: () -> Unit,
    onDeleted: () -> Unit
) {
    val interviewWithItems by viewModel.observeInterview(interviewId)
        .collectAsStateWithLifecycle(initialValue = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Interview") },
            text = { Text("This interview and all of its questions will be removed from the local database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteInterview(
                            id = interviewId,
                            onDeleted = onDeleted,
                            onError = { errorMessage = it }
                        )
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val current = interviewWithItems
    if (current == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loading interview")
        }
        return
    }

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
                    text = "Interview Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }

        item {
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
                    Text(
                        text = current.interview.company,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = current.interview.position,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(formatDate(current.interview.date)) }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(InterviewConstants.normalizeRound(current.interview.round)) }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(InterviewConstants.normalizeResult(current.interview.result)) }
                        )
                    }
                    Text(
                        text = "Overall average ${current.averageText()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = onOpenReport,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                        Text("View Review Report")
                    }
                    if (current.interview.notes.isNotBlank()) {
                        Text(
                            text = current.interview.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        errorMessage?.let {
            item {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Text(
                text = "Questions and Answers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(
            items = current.items,
            key = { it.id }
        ) { item ->
            InterviewItemCard(item = item)
        }
    }
}

@Composable
private fun InterviewItemCard(item: InterviewItemEntity) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = InterviewConstants.normalizeCategory(item.category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${item.selfRating}/5") }
                )
            }
            Text(
                text = item.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (item.myAnswer.isNotBlank()) {
                Text(
                    text = item.myAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun com.example.interviewmate.data.model.InterviewWithItems.averageText(): String {
    if (items.isEmpty()) return "-"
    return String.format(Locale.getDefault(), "%.1f", items.map { it.selfRating }.average())
}
