package com.example.interviewmate.ui.profile

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interviewmate.data.model.InterviewWithItems
import com.example.interviewmate.ui.interview.InterviewViewModel
import com.example.interviewmate.util.InterviewConstants
import com.example.interviewmate.util.formatDate
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ProfileScreen(
    viewModel: InterviewViewModel
) {
    val interviews by viewModel.allInterviews.collectAsStateWithLifecycle()
    val stats = calculateStats(interviews)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Interview counts, outcomes, and average skills",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(onClick = {}, label = { Text("Total ${interviews.size}") })
                        AssistChip(onClick = {}, label = { Text("Passed ${stats.passed}") })
                        AssistChip(onClick = {}, label = { Text("Pending ${stats.pending}") })
                        AssistChip(onClick = {}, label = { Text("Failed ${stats.failed}") })
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Skill Averages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    AbilityRadarChart(
                        categoryAverages = stats.categoryAverages,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                    if (stats.totalItems == 0) {
                        Text(
                            text = "Add interview questions and self-ratings to show the skill radar chart here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    InterviewConstants.Categories.forEach { category ->
                        val average = stats.categoryAverages[category] ?: 0f
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", average),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (average / 5f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Recent 3 Interviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (interviews.isEmpty()) {
            item {
                EmptyProfileCard()
            }
        } else {
            items(
                items = interviews.take(3),
                key = { it.interview.id }
            ) { interview ->
                RecentInterviewCard(interview)
            }
        }
    }
}

@Composable
private fun EmptyProfileCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "No recent records",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "After logging your first interview, the latest three records will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentInterviewCard(interview: InterviewWithItems) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = interview.interview.company,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${interview.interview.position} - ${formatDate(interview.interview.date)} - ${InterviewConstants.normalizeResult(interview.interview.result)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class ProfileStats(
    val passed: Int,
    val pending: Int,
    val failed: Int,
    val totalItems: Int,
    val categoryAverages: Map<String, Float>
)

private fun calculateStats(interviews: List<InterviewWithItems>): ProfileStats {
    val allItems = interviews.flatMap { it.items }
    val averages = InterviewConstants.Categories.associateWith { category ->
        val scores = allItems
            .filter { InterviewConstants.normalizeCategory(it.category) == category }
            .map { it.selfRating }
        if (scores.isEmpty()) 0f else scores.average().toFloat()
    }
    return ProfileStats(
        passed = interviews.count { InterviewConstants.normalizeResult(it.interview.result) == InterviewConstants.ResultPassed },
        pending = interviews.count { InterviewConstants.normalizeResult(it.interview.result) == InterviewConstants.ResultPending },
        failed = interviews.count { InterviewConstants.normalizeResult(it.interview.result) == InterviewConstants.ResultFailed },
        totalItems = allItems.size,
        categoryAverages = averages
    )
}

@Composable
private fun AbilityRadarChart(
    categoryAverages: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val fill = primary.copy(alpha = 0.20f)
    val outline = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelSize = with(LocalDensity.current) { 12.sp.toPx() }

    Canvas(modifier = modifier) {
        val labels = InterviewConstants.Categories
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) * 0.34f
        val angles = labels.indices.map { index ->
            -PI / 2.0 + 2.0 * PI * index / labels.size
        }

        fun pointFor(angle: Double, scale: Float): Offset {
            return Offset(
                x = center.x + cos(angle).toFloat() * radius * scale,
                y = center.y + sin(angle).toFloat() * radius * scale
            )
        }

        for (level in 1..5) {
            val scale = level / 5f
            val gridPath = Path()
            angles.forEachIndexed { index, angle ->
                val point = pointFor(angle, scale)
                if (index == 0) {
                    gridPath.moveTo(point.x, point.y)
                } else {
                    gridPath.lineTo(point.x, point.y)
                }
            }
            gridPath.close()
            drawPath(
                path = gridPath,
                color = outline,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        angles.forEach { angle ->
            drawLine(
                color = outline,
                start = center,
                end = pointFor(angle, 1f),
                strokeWidth = 1.dp.toPx()
            )
        }

        val scorePath = Path()
        labels.forEachIndexed { index, category ->
            val score = (categoryAverages[category] ?: 0f).coerceIn(0f, 5f)
            val point = pointFor(angles[index], score / 5f)
            if (index == 0) {
                scorePath.moveTo(point.x, point.y)
            } else {
                scorePath.lineTo(point.x, point.y)
            }
        }
        scorePath.close()
        drawPath(path = scorePath, color = fill)
        drawPath(
            path = scorePath,
            color = primary,
            style = Stroke(width = 2.dp.toPx())
        )

        labels.forEachIndexed { index, category ->
            val score = (categoryAverages[category] ?: 0f).coerceIn(0f, 5f)
            val point = pointFor(angles[index], score / 5f)
            drawCircle(
                color = primary,
                radius = 4.dp.toPx(),
                center = point
            )
        }

        val paint = Paint().apply {
            isAntiAlias = true
            color = textColor.toArgb()
            textSize = labelSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        labels.forEachIndexed { index, category ->
            val labelPoint = pointFor(angles[index], 1.22f)
            paint.textAlign = when {
                labelPoint.x < center.x - 8.dp.toPx() -> Paint.Align.RIGHT
                labelPoint.x > center.x + 8.dp.toPx() -> Paint.Align.LEFT
                else -> Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(
                category,
                labelPoint.x,
                labelPoint.y + labelSize / 3f,
                paint
            )
        }
    }
}
