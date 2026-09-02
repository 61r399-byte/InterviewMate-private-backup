package com.example.interviewmate.ui.interview

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interviewmate.data.model.InterviewWithItems
import com.example.interviewmate.util.InterviewConstants

@Composable
fun InterviewEditScreen(
    interviewId: Long?,
    viewModel: InterviewViewModel,
    onNavigateBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val existingInterview by if (interviewId == null) {
        remember { mutableStateOf<InterviewWithItems?>(null) }
    } else {
        viewModel.observeInterview(interviewId).collectAsStateWithLifecycle(initialValue = null)
    }
    var loadedInterviewId by remember(interviewId) { mutableStateOf<Long?>(null) }
    var formState by remember(interviewId) { mutableStateOf(InterviewFormState()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(interviewId, existingInterview?.interview?.id) {
        if (
            interviewId != null &&
            existingInterview != null &&
            loadedInterviewId != existingInterview?.interview?.id
        ) {
            formState = existingInterview!!.toFormState()
            loadedInterviewId = existingInterview!!.interview.id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (interviewId == null) "New Interview" else "Edit Interview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(
                enabled = !isSaving,
                onClick = {
                    isSaving = true
                    errorMessage = null
                    viewModel.saveInterview(
                        formState = formState,
                        existingInterview = existingInterview?.interview,
                        onSaved = {
                            isSaving = false
                            onSaved(it)
                        },
                        onError = {
                            isSaving = false
                            errorMessage = it
                        }
                    )
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Text(text = if (isSaving) "Saving" else "Save")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Basic Info")
        OutlinedTextField(
            value = formState.company,
            onValueChange = { formState = formState.copy(company = it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Company *") }
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = formState.position,
            onValueChange = { formState = formState.copy(position = it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Role *") }
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = formState.dateText,
            onValueChange = { formState = formState.copy(dateText = it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Date yyyy-MM-dd") }
        )

        Spacer(modifier = Modifier.height(14.dp))
        OptionChips(
            title = "Round",
            options = InterviewConstants.Rounds,
            selected = formState.round,
            onSelected = { formState = formState.copy(round = it) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        OptionChips(
            title = "Result",
            options = InterviewConstants.Results,
            selected = formState.result,
            onSelected = { formState = formState.copy(result = it) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = formState.notes,
            onValueChange = { formState = formState.copy(notes = it) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            label = { Text("Overall Notes") }
        )

        Spacer(modifier = Modifier.height(22.dp))
        SectionTitle("Interview Questions")

        formState.items.forEachIndexed { index, item ->
            QuestionEditor(
                index = index,
                item = item,
                canDelete = formState.items.size > 1,
                onItemChange = { updated ->
                    formState = formState.copy(
                        items = formState.items.map {
                            if (it.localId == item.localId) updated else it
                        }
                    )
                },
                onDelete = {
                    formState = formState.copy(
                        items = formState.items.filterNot { it.localId == item.localId }
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = {
                formState = formState.copy(items = formState.items + emptyInterviewItemForm())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text("Add Question")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun OptionChips(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Composable
private fun QuestionEditor(
    index: Int,
    item: InterviewItemForm,
    canDelete: Boolean,
    onItemChange: (InterviewItemForm) -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete question")
                    }
                }
            }

            OptionChips(
                title = "Category",
                options = InterviewConstants.Categories,
                selected = item.category,
                onSelected = { onItemChange(item.copy(category = it)) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = item.question,
                onValueChange = { onItemChange(item.copy(question = it)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Question *") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = item.myAnswer,
                onValueChange = { onItemChange(item.copy(myAnswer = it)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("My Answer") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            RatingSelector(
                rating = item.selfRating,
                onRatingChange = { onItemChange(item.copy(selfRating = it)) }
            )
        }
    }
}

@Composable
private fun RatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Column {
        Text(
            text = "Self Rating",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            (1..5).forEach { score ->
                FilterChip(
                    selected = score == rating,
                    onClick = { onRatingChange(score) },
                    label = { Text("$score") }
                )
            }
        }
    }
}
