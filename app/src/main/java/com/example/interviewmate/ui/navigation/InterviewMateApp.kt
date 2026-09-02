package com.example.interviewmate.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.interviewmate.AppContainer
import com.example.interviewmate.ui.interview.InterviewDetailScreen
import com.example.interviewmate.ui.interview.InterviewEditScreen
import com.example.interviewmate.ui.interview.InterviewListScreen
import com.example.interviewmate.ui.interview.InterviewViewModel
import com.example.interviewmate.ui.interview.InterviewViewModelFactory
import com.example.interviewmate.ui.profile.ProfileScreen
import com.example.interviewmate.ui.question.QuestionBankScreen
import com.example.interviewmate.ui.question.QuestionViewModel
import com.example.interviewmate.ui.question.QuestionViewModelFactory
import com.example.interviewmate.ui.report.ReportScreen

@Composable
fun InterviewMateApp(
    container: AppContainer
) {
    val navController = rememberNavController()
    val interviewViewModel: InterviewViewModel = viewModel(
        factory = InterviewViewModelFactory(container.interviewRepository)
    )
    val questionViewModel: QuestionViewModel = viewModel(
        factory = QuestionViewModelFactory(container.questionRepository)
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                destination.icon?.let {
                                    Icon(imageVector = it, contentDescription = destination.label)
                                }
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Interviews.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Interviews.route) {
                InterviewListScreen(
                    viewModel = interviewViewModel,
                    onAddInterview = {
                        navController.navigate(AppDestination.InterviewNew.route)
                    },
                    onOpenInterview = {
                        navController.navigate(AppDestination.InterviewDetail.createRoute(it))
                    }
                )
            }

            composable(AppDestination.Questions.route) {
                QuestionBankScreen(viewModel = questionViewModel)
            }

            composable(AppDestination.Profile.route) {
                ProfileScreen(viewModel = interviewViewModel)
            }

            composable(AppDestination.InterviewNew.route) {
                InterviewEditScreen(
                    interviewId = null,
                    viewModel = interviewViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { interviewId ->
                        navController.navigate(AppDestination.InterviewDetail.createRoute(interviewId)) {
                            popUpTo(AppDestination.InterviewNew.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(
                route = AppDestination.InterviewDetail.route,
                arguments = listOf(navArgument("interviewId") { type = NavType.LongType })
            ) { entry ->
                val interviewId = entry.arguments?.getLong("interviewId") ?: return@composable
                InterviewDetailScreen(
                    interviewId = interviewId,
                    viewModel = interviewViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onEdit = {
                        navController.navigate(AppDestination.InterviewEdit.createRoute(interviewId))
                    },
                    onOpenReport = {
                        navController.navigate(AppDestination.Report.createRoute(interviewId))
                    },
                    onDeleted = {
                        navController.navigate(AppDestination.Interviews.route) {
                            popUpTo(AppDestination.Interviews.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = AppDestination.InterviewEdit.route,
                arguments = listOf(navArgument("interviewId") { type = NavType.LongType })
            ) { entry ->
                val interviewId = entry.arguments?.getLong("interviewId") ?: return@composable
                InterviewEditScreen(
                    interviewId = interviewId,
                    viewModel = interviewViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = AppDestination.Report.route,
                arguments = listOf(navArgument("interviewId") { type = NavType.LongType })
            ) { entry ->
                val interviewId = entry.arguments?.getLong("interviewId") ?: return@composable
                ReportScreen(
                    interviewId = interviewId,
                    interviewViewModel = interviewViewModel,
                    questionViewModel = questionViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
