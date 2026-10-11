package com.sami.setra.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sami.setra.data.local.preferences.DistanceUnit
import com.sami.setra.data.local.preferences.UserPreferences
import com.sami.setra.data.local.preferences.WeightUnit
import com.sami.setra.ui.screens.create.CreateScreen
import com.sami.setra.ui.screens.exercises.ExerciseDetailScreen
import com.sami.setra.ui.screens.exercises.ExerciseDetailViewModel
import com.sami.setra.ui.screens.exercises.ExerciseListScreen
import com.sami.setra.ui.screens.exercises.ExerciseListViewModel
import com.sami.setra.ui.screens.exercises.ExercisePickerScreen
import com.sami.setra.ui.screens.profile.ProfileScreen
import com.sami.setra.ui.screens.progress.ProgressScreen
import com.sami.setra.ui.screens.routines.RoutineDetailScreen
import com.sami.setra.ui.screens.routines.RoutineDetailViewModel
import com.sami.setra.ui.screens.routines.RoutinesScreen
import com.sami.setra.ui.screens.routines.RoutinesViewModel
import com.sami.setra.ui.screens.splits.SplitDetailScreen
import com.sami.setra.ui.screens.splits.SplitsScreen
import com.sami.setra.ui.screens.splits.SplitsViewModel
import com.sami.setra.ui.screens.workout.ActiveWorkoutScreen
import com.sami.setra.ui.screens.workout.ActiveWorkoutViewModel
import com.sami.setra.ui.screens.workout.WorkoutSummaryScreen
import com.sami.setra.ui.theme.ThemeMode

@Composable
fun SetraNavHost(
    navController: NavHostController,
    userPreferences: UserPreferences,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onDistanceUnitChange: (DistanceUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val splitsViewModel: SplitsViewModel = viewModel()
    val routinesViewModel: RoutinesViewModel = viewModel()
    val exerciseListViewModel: ExerciseListViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splits,
        modifier = modifier
    ) {
        composable<Screen.Splits> {
            SplitsScreen(
                splitsViewModel = splitsViewModel,
                onSplitClick = { splitId ->
                    navController.navigate(Screen.SplitDetail(splitId))
                },
                onOpenSearch = {
                    navController.navigate(Screen.ExerciseList)
                }
            )
        }

        composable<Screen.SplitDetail> { backStackEntry ->
            val detail: Screen.SplitDetail = backStackEntry.toRoute()
            val split = splitsViewModel.getSplitById(detail.splitId)
            if (split != null) {
                SplitDetailScreen(
                    split = split,
                    onBackClick = { navController.popBackStack() },
                    onUseSplitClick = {
                        splitsViewModel.useSplit(split) { newRoutineId ->
                            navController.navigate(Screen.RoutineDetail(newRoutineId))
                        }
                    }
                )
            }
        }

        composable<Screen.Routines> {
            RoutinesScreen(
                viewModel = routinesViewModel,
                onBrowseSplitsClick = {
                    navController.navigateToBottomNavDestination(Screen.Splits)
                },
                onRoutineClick = { routineId ->
                    navController.navigate(Screen.RoutineDetail(routineId))
                },
                onOpenSearch = {
                    navController.navigate(Screen.ExerciseList)
                },
                onResumeActiveWorkout = { sessionId ->
                    navController.navigate(Screen.ActiveWorkout(sessionId))
                }
            )
        }

        composable<Screen.RoutineDetail> { backStackEntry ->
            val detail: Screen.RoutineDetail = backStackEntry.toRoute()
            val routineDetailViewModel: RoutineDetailViewModel = viewModel(
                factory = RoutineDetailViewModel.Factory(application, detail.routineId)
            )
            RoutineDetailScreen(
                viewModel = routineDetailViewModel,
                onBackClick = { navController.popBackStack() },
                onAddExerciseClick = { workoutId ->
                    navController.navigate(Screen.ExercisePicker(workoutId))
                },
                onRoutineDuplicated = { newRoutineId ->
                    navController.navigate(Screen.RoutineDetail(newRoutineId))
                },
                onStartWorkout = { sessionId ->
                    navController.navigate(Screen.ActiveWorkout(sessionId))
                }
            )
        }

        composable<Screen.ActiveWorkout> { backStackEntry ->
            val activeRoute: Screen.ActiveWorkout = backStackEntry.toRoute()
            val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(
                factory = ActiveWorkoutViewModel.Factory(application, activeRoute.sessionId)
            )
            ActiveWorkoutScreen(
                viewModel = activeWorkoutViewModel,
                onFinishWorkoutClick = {
                    navController.navigate(Screen.WorkoutSummary(activeRoute.sessionId))
                },
                onCancelWorkoutClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.WorkoutSummary> { backStackEntry ->
            val summaryRoute: Screen.WorkoutSummary = backStackEntry.toRoute()
            val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(
                factory = ActiveWorkoutViewModel.Factory(application, summaryRoute.sessionId)
            )
            WorkoutSummaryScreen(
                viewModel = activeWorkoutViewModel,
                onDoneClick = {
                    navController.navigate(Screen.Routines) {
                        popUpTo<Screen.Splits>()
                    }
                }
            )
        }

        composable<Screen.ExercisePicker> { backStackEntry ->
            val picker: Screen.ExercisePicker = backStackEntry.toRoute()
            val routineDetailViewModel: RoutineDetailViewModel = viewModel(
                factory = RoutineDetailViewModel.Factory(application, picker.workoutId)
            )
            ExercisePickerScreen(
                viewModel = exerciseListViewModel,
                onBackClick = { navController.popBackStack() },
                onExerciseSelected = { exerciseId ->
                    routineDetailViewModel.addExerciseToWorkout(picker.workoutId, exerciseId)
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Create> {
            CreateScreen(
                onCreateRoutineClick = {
                    routinesViewModel.createNewRoutine { newRoutineId ->
                        navController.navigate(Screen.RoutineDetail(newRoutineId))
                    }
                },
                onBrowseSplitsClick = {
                    navController.navigateToBottomNavDestination(Screen.Splits)
                },
                onBrowseExercisesClick = {
                    navController.navigate(Screen.ExerciseList)
                }
            )
        }

        composable<Screen.Progress> {
            ProgressScreen()
        }

        composable<Screen.Profile> {
            ProfileScreen(
                userPreferences = userPreferences,
                onThemeModeChange = onThemeModeChange,
                onWeightUnitChange = onWeightUnitChange,
                onDistanceUnitChange = onDistanceUnitChange
            )
        }

        composable<Screen.ExerciseList> {
            ExerciseListScreen(
                viewModel = exerciseListViewModel,
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail(exerciseId))
                }
            )
        }

        composable<Screen.ExerciseDetail> { backStackEntry ->
            val detail: Screen.ExerciseDetail = backStackEntry.toRoute()
            val exerciseDetailViewModel: ExerciseDetailViewModel = viewModel()
            ExerciseDetailScreen(
                exerciseId = detail.exerciseId,
                viewModel = exerciseDetailViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

fun NavHostController.navigateToBottomNavDestination(destination: Screen) {
    navigate(destination) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
