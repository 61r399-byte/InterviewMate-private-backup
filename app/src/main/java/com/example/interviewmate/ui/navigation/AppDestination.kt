package com.example.interviewmate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null
) {
    data object Interviews : AppDestination(
        route = "interviews",
        label = "Interviews",
        icon = Icons.AutoMirrored.Filled.List
    )

    data object Questions : AppDestination(
        route = "questions",
        label = "Question Bank",
        icon = Icons.Default.Info
    )

    data object Profile : AppDestination(
        route = "profile",
        label = "Profile",
        icon = Icons.Default.Person
    )

    data object InterviewNew : AppDestination(
        route = "interview/new",
        label = "New Interview"
    )

    data object InterviewDetail : AppDestination(
        route = "interview/{interviewId}",
        label = "Interview Details"
    ) {
        fun createRoute(interviewId: Long): String = "interview/$interviewId"
    }

    data object InterviewEdit : AppDestination(
        route = "interview/{interviewId}/edit",
        label = "Edit Interview"
    ) {
        fun createRoute(interviewId: Long): String = "interview/$interviewId/edit"
    }

    data object Report : AppDestination(
        route = "interview/{interviewId}/report",
        label = "Review Report"
    ) {
        fun createRoute(interviewId: Long): String = "interview/$interviewId/report"
    }
}

val TopLevelDestinations = listOf(
    AppDestination.Interviews,
    AppDestination.Questions,
    AppDestination.Profile
)
