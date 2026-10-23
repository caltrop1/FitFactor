package com.sami.setra.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.relation.ExerciseWithDetails
import com.sami.setra.data.local.database.relation.SessionExerciseWithDetails
import com.sami.setra.data.local.preferences.DistanceUnit
import com.sami.setra.data.model.ExerciseTracking
import com.sami.setra.data.model.exerciseTracking
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onFinishWorkoutClick: () -> Unit,
    onCancelWorkoutClick: () -> Unit,
    distanceUnit: DistanceUnit = DistanceUnit.KM
) {
    val session by viewModel.sessionState.collectAsState()
    val currentIdx by viewModel.currentExerciseIndex.collectAsState()
    val previousPerformance by viewModel.previousPerformance.collectAsState()
    val currentExerciseDetails by viewModel.currentExerciseDetails.collectAsState()

    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timerRemaining by viewModel.timerRemainingSeconds.collectAsState()
    val timerTotal by viewModel.timerTotalSeconds.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }
    var showFormDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Workout?") },
            text = { Text("Are you sure you want to cancel this active workout session? Your progress will be discarded.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelWorkout {
                            onCancelWorkoutClick()
                        }
                    }
                ) {
                    Text("Cancel Workout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Training")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = session?.session?.workoutName?.ifBlank { "Active Workout" } ?: "Active Workout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!session?.session?.routineName.isNullOrBlank()) {
                            Text(
                                text = session!!.session.routineName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Workout"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onFinishWorkoutClick,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Finish", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading workout session...", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val currentSession = session!!
            val exercises = currentSession.sortedExercises

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                // Workout Progress Overview Bar
                WorkoutProgressHeader(
                    completedExercises = currentSession.completedExercisesCount,
                    totalExercises = currentSession.totalExercisesCount,
                    completedSets = currentSession.completedSetsCount,
                    totalSets = currentSession.totalSetsCount
                )

                // Rest Timer Banner (if active or remaining > 0)
                AnimatedVisibility(visible = isTimerRunning || timerRemaining > 0) {
                    RestTimerBanner(
                        remainingSeconds = timerRemaining,
                        totalSeconds = timerTotal,
                        isRunning = isTimerRunning,
                        onPauseClick = { viewModel.pauseTimer() },
                        onResumeClick = { viewModel.resumeTimer() },
                        onSkipClick = { viewModel.skipTimer() },
                        onAdd30sClick = { viewModel.adjustTimer(30) },
                        onMinus10sClick = { viewModel.adjustTimer(-10) }
                    )
                }

                if (exercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exercises in this workout session.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val safeIdx = currentIdx.coerceIn(0, exercises.size - 1)
                    val activeExercise = exercises[safeIdx]

                    val lazyRowState = rememberLazyListState()
                    LaunchedEffect(safeIdx) {
                        if (exercises.isNotEmpty() && safeIdx in exercises.indices) {
                            lazyRowState.animateScrollToItem(safeIdx)
                        }
                    }

                    if (showFormDialog) {
                        ExerciseFormDialog(
                            exerciseWithDetails = currentExerciseDetails,
                            exerciseNameFallback = activeExercise.sessionExercise.exerciseNameSnapshot,
                            onDismiss = { showFormDialog = false }
                        )
                    }

                    // Exercise Chip Selector
                    LazyRow(
                        state = lazyRowState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(exercises) { idx, sessionEx ->
                            val isSelected = idx == safeIdx
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectExerciseIndex(idx) },
                                label = {
                                    Text(
                                        text = "${idx + 1}. ${sessionEx.sessionExercise.exerciseNameSnapshot}",
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = if (sessionEx.isCompleted) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    // Active Exercise Content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Exercise Header & Metadata
                        item {
                            ExerciseHeaderCard(
                                sessionEx = activeExercise,
                                exerciseIndex = safeIdx,
                                totalExercises = exercises.size,
                                onViewFormClick = { showFormDialog = true }
                            )
                        }

                        // Previous Performance Card
                        if (previousPerformance.isNotEmpty()) {
                            item {
                                PreviousPerformanceCard(sets = previousPerformance)
                            }
                        }

                        // Sets Table Header
                        val tracking = exerciseTracking(activeExercise.exercise?.category, activeExercise.exercise?.equipment, activeExercise.exercise?.mechanic)
                        item {
                            SetsTableHeader(tracking, distanceUnit)
                        }

                        // Set Rows
                        itemsIndexed(activeExercise.sortedCompletedSets) { setIndex, completedSet ->
                            SetRowCard(
                                setNumber = setIndex + 1,
                                completedSet = completedSet,
                                tracking = tracking,
                                distanceUnit = distanceUnit,
                                onUpdateActuals = { weight, reps, duration, distance ->
                                    viewModel.updateSetActuals(completedSet, weight, reps, duration, distance)
                                },
                                onToggleComplete = { isDone ->
                                    viewModel.toggleSetCompletion(completedSet, isDone)
                                }
                            )
                        }

                        // Add Set Button
                        item {
                            OutlinedButton(
                                onClick = { viewModel.addSetToCurrentExercise() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Set")
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Bottom Navigation Bar for Exercises
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.selectExerciseIndex(safeIdx - 1) },
                                enabled = safeIdx > 0
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Exercise")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Prev")
                            }

                            Text(
                                text = "${safeIdx + 1} / ${exercises.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (safeIdx < exercises.size - 1) {
                                Button(
                                    onClick = { viewModel.selectExerciseIndex(safeIdx + 1) }
                                ) {
                                    Text("Next")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Exercise")
                                }
                            } else {
                                Button(
                                    onClick = onFinishWorkoutClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Finish", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Finish Workout")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutProgressHeader(
    completedExercises: Int,
    totalExercises: Int,
    completedSets: Int,
    totalSets: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Exercises: $completedExercises / $totalExercises",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Sets: $completedSets / $totalSets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val progress = if (totalSets > 0) completedSets.toFloat() / totalSets else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun RestTimerBanner(
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onSkipClick: () -> Unit,
    onAdd30sClick: () -> Unit,
    onMinus10sClick: () -> Unit
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Rest Timer",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rest Timer: $timeFormatted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onMinus10sClick) {
                        Text("-10s", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = onAdd30sClick) {
                        Text("+30s", style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(
                        onClick = if (isRunning) onPauseClick else onResumeClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause Timer" else "Resume Timer"
                        )
                    }
                    IconButton(onClick = onSkipClick, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Skip Rest Timer")
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ExerciseHeaderCard(
    sessionEx: SessionExerciseWithDetails,
    exerciseIndex: Int,
    totalExercises: Int,
    onViewFormClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewFormClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercise ${exerciseIndex + 1} of $totalExercises",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (sessionEx.isCompleted) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sessionEx.sessionExercise.exerciseNameSnapshot,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = onViewFormClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View exercise form",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Form", style = MaterialTheme.typography.labelMedium)
                }
            }

            val exercise = sessionEx.exercise
            if (exercise != null) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (exercise.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = exercise.category.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (!exercise.equipment.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = exercise.equipment.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviousPerformanceCard(
    sets: List<CompletedSetEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Previous Performance",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val summaryText = sets.joinToString(separator = "  •  ") { s ->
                val w = s.weight?.let { if (it % 1.0 == 0.0) "${it.toInt()}kg" else "${it}kg" } ?: ""
                val r = s.reps?.let { "${it.toInt()} reps" } ?: s.durationSeconds?.let { "${it}s" } ?: ""
                if (w.isNotBlank() && r.isNotBlank()) "$w × $r" else w.ifBlank { r }
            }
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SetsTableHeader(tracking: ExerciseTracking, distanceUnit: DistanceUnit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("SET", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
        Text("TARGET", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (tracking.externalResistance) Text("KG", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        if (tracking.repetitions) Text("REPS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        if (tracking.duration) Text("SEC", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        if (tracking.distance) Text(distanceUnit.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text("DONE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun SetRowCard(
    setNumber: Int,
    completedSet: CompletedSetEntity,
    tracking: ExerciseTracking,
    distanceUnit: DistanceUnit,
    onUpdateActuals: (Double?, Double?, Int?, Double?) -> Unit,
    onToggleComplete: (Boolean) -> Unit
) {
    var weightText by remember(completedSet.weight) {
        mutableStateOf(completedSet.weight?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var repsText by remember(completedSet.reps) { mutableStateOf(completedSet.reps?.toInt()?.toString() ?: "") }
    var durationText by remember(completedSet.durationSeconds) { mutableStateOf(completedSet.durationSeconds?.toString() ?: "") }
    var distanceText by remember(completedSet.distanceMeters) {
        val value = completedSet.distanceMeters?.let { if (distanceUnit == DistanceUnit.KM) it / 1000.0 else it / 1609.344 }
        mutableStateOf(value?.takeIf { it > 0 }?.toString() ?: "")
    }

    val isDone = completedSet.isCompleted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.width(36.dp)) {
                    Text("$setNumber", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (completedSet.setType != "NORMAL") Text(completedSet.setType.take(1), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    val targetSummary = buildList {
                        if (tracking.externalResistance) completedSet.targetWeight?.let { add("${it}kg") }
                        if (tracking.repetitions) completedSet.targetReps?.let { add("${it.toInt()} reps") }
                        if (tracking.duration) completedSet.targetDurationSeconds?.let { add("${it}s") }
                        if (tracking.distance) completedSet.targetDistanceMeters?.let { add("${it / if (distanceUnit == DistanceUnit.KM) 1000.0 else 1609.344} ${distanceUnit.name.lowercase()}") }
                    }.joinToString(" · ").ifBlank { "No target" }
                    Text(targetSummary, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onToggleComplete(!isDone) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = if (isDone) "Mark set incomplete" else "Complete set", tint = if (isDone) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (tracking.externalResistance) {
                    MetricInput(weightText, "kg", Modifier.weight(1f), { input ->
                        weightText = input
                        onUpdateActuals(input.toDoubleOrNull(), repsText.toDoubleOrNull(), durationText.toIntOrNull(), distanceValue(distanceText, distanceUnit))
                    })
                }
                if (tracking.repetitions) {
                    MetricInput(repsText, "reps", Modifier.weight(1f), { input ->
                        repsText = input
                        onUpdateActuals(weightText.toDoubleOrNull(), input.toDoubleOrNull(), durationText.toIntOrNull(), distanceValue(distanceText, distanceUnit))
                    })
                }
                if (tracking.duration) {
                    MetricInput(durationText, "sec", Modifier.weight(1f), { input ->
                        durationText = input
                        onUpdateActuals(weightText.toDoubleOrNull(), repsText.toDoubleOrNull(), input.toIntOrNull(), distanceValue(distanceText, distanceUnit))
                    })
                }
                if (tracking.distance) {
                    MetricInput(distanceText, distanceUnit.name.lowercase(), Modifier.weight(1f), { input ->
                        distanceText = input
                        onUpdateActuals(weightText.toDoubleOrNull(), repsText.toDoubleOrNull(), durationText.toIntOrNull(), distanceValue(input, distanceUnit))
                    })
                }
            }
        }
    }
}

@Composable
private fun MetricInput(value: String, label: String, modifier: Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.height(58.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
        singleLine = true
    )
}

private fun distanceValue(value: String, unit: DistanceUnit): Double? = value.toDoubleOrNull()?.let {
    it * if (unit == DistanceUnit.KM) 1000.0 else 1609.344
}

@Composable
private fun ExerciseFormDialog(
    exerciseWithDetails: ExerciseWithDetails?,
    exerciseNameFallback: String,
    onDismiss: () -> Unit
) {
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exerciseWithDetails?.exercise?.name ?: exerciseNameFallback,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close exercise form preview"
                        )
                    }
                }

                // Images Section
                val images = exerciseWithDetails?.images?.sortedBy { it.ordering } ?: emptyList()
                if (images.isNotEmpty()) {
                    Text(
                        text = "Tap image to view full screen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val context = LocalContext.current
                        images.forEach { img ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clickable { fullScreenImagePath = img.assetPath },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("file:///android_asset/${img.assetPath}")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = exerciseWithDetails?.exercise?.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize(),
                                        loading = {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        error = {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.FitnessCenter,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLow,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No preview images for this exercise",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Exercise Details / Badges
                val ex = exerciseWithDetails?.exercise
                if (ex != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ex.category.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = ex.category.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (!ex.equipment.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = ex.equipment.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (ex.level.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = ex.level.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    val primaryMuscles = exerciseWithDetails.primaryMuscles
                    if (primaryMuscles.isNotEmpty()) {
                        Column {
                            Text(
                                text = "Primary Muscles",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = primaryMuscles.joinToString(" • ") { it.displayName },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (!ex.instructions.isNullOrBlank()) {
                        Column {
                            Text(
                                text = "Instructions",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ex.instructions,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Preview")
                }
            }
        }
    }

    // Fullscreen Image Overlay
    if (fullScreenImagePath != null) {
        Dialog(
            onDismissRequest = { fullScreenImagePath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize()
            ) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                if (newScale > 1f) {
                                    offset += pan
                                } else {
                                    offset = Offset.Zero
                                }
                                scale = newScale
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val context = LocalContext.current
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/$fullScreenImagePath")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Full screen exercise form image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    fullScreenImagePath = null
                                }
                            }
                    )

                    IconButton(
                        onClick = { fullScreenImagePath = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close full screen image"
                        )
                    }
                }
            }
        }
    }
}
