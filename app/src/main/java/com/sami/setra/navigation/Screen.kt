package com.sami.setra.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splits : Screen

    @Serializable
    data class SplitDetail(val splitId: String) : Screen

    @Serializable
    data object Routines : Screen

    @Serializable
    data class RoutineDetail(val routineId: Long) : Screen

    @Serializable
    data class ExercisePicker(val workoutId: Long) : Screen

    @Serializable
    data object Create : Screen

    @Serializable
    data object Progress : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object ExerciseList : Screen

    @Serializable
    data class ExerciseDetail(val exerciseId: String) : Screen

    @Serializable
    data class ActiveWorkout(val sessionId: Long) : Screen

    @Serializable
    data class WorkoutSummary(val sessionId: Long) : Screen

    @Serializable
    data object WorkoutHistory : Screen

    @Serializable
    data class SessionDetail(val sessionId: Long) : Screen
}

data class BottomNavItem(
    val route: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Splits,
        title = "Splits",
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView,
        contentDescription = "Training splits destination"
    ),
    BottomNavItem(
        route = Screen.Routines,
        title = "Routines",
        selectedIcon = Icons.AutoMirrored.Filled.FormatListBulleted,
        unselectedIcon = Icons.AutoMirrored.Outlined.FormatListBulleted,
        contentDescription = "My routines destination"
    ),
    BottomNavItem(
        route = Screen.Create,
        title = "Create",
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline,
        contentDescription = "Create workout or routine destination"
    ),
    BottomNavItem(
        route = Screen.Progress,
        title = "Progress",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        contentDescription = "Progress and history destination"
    ),
    BottomNavItem(
        route = Screen.Profile,
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "Profile and settings destination"
    )
)
