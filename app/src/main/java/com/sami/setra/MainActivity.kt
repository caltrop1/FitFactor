package com.sami.setra

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sami.setra.navigation.Screen
import com.sami.setra.navigation.SetraNavHost
import com.sami.setra.navigation.bottomNavItems
import com.sami.setra.navigation.navigateToBottomNavDestination
import com.sami.setra.ui.components.SetraScaffold
import com.sami.setra.ui.theme.SetraTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.handleIntent(intent)

        setContent {
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()

            SetraTheme(themeMode = userPreferences.themeMode) {
                SetraApp(
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent)
    }
}

@Composable
fun SetraApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val pendingImportUri by viewModel.pendingImportUri.collectAsStateWithLifecycle()

    LaunchedEffect(pendingImportUri) {
        if (pendingImportUri != null) {
            navController.navigateToBottomNavDestination(Screen.Routines)
        }
    }

    val currentRoute = navBackStackEntry?.destination?.route
    val isExerciseDetailScreen = currentRoute?.contains("ExerciseDetail") == true
    val isSplitDetailScreen = currentRoute?.contains("SplitDetail") == true
    val isListScreen = currentRoute?.contains("ExerciseList") == true

    val currentDestination: Screen = when {
        currentRoute?.contains("Routine") == true -> Screen.Routines
        currentRoute?.contains("Create") == true -> Screen.Create
        currentRoute?.contains("Progress") == true -> Screen.Progress
        currentRoute?.contains("Profile") == true || currentRoute?.contains("Settings") == true -> Screen.Settings
        isListScreen -> Screen.ExerciseList
        else -> Screen.Splits
    }

    SetraScaffold(
        currentDestination = currentDestination,
        bottomNavItems = bottomNavItems,
        onNavigateToDestination = { destination ->
            navController.navigateToBottomNavDestination(destination)
        },
        topBar = {},
        modifier = modifier
    ) { innerPadding ->
        SetraNavHost(
            navController = navController,
            userPreferences = userPreferences,
            onThemeModeChange = viewModel::setThemeMode,
            onWeightUnitChange = viewModel::setWeightUnit,
            onDistanceUnitChange = viewModel::setDistanceUnit,
            onDefaultRestSecondsChange = viewModel::setDefaultRestSeconds,
            pendingImportUri = pendingImportUri,
            onConsumePendingImportUri = viewModel::consumePendingImportUri,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
