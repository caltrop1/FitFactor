package com.sami.setra.ui.screens.routines

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sami.setra.data.local.database.entity.SetTemplateEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity
import com.sami.setra.data.local.database.relation.RoutineDayWithWorkouts
import com.sami.setra.data.local.database.relation.RoutineExerciseWithDetails
import com.sami.setra.data.local.database.relation.WorkoutWithExercises
import com.sami.setra.data.model.ExerciseTracking
import com.sami.setra.data.model.exerciseTracking
import com.sami.setra.ui.components.SetraCard
import com.sami.setra.ui.components.SetraGlassSurface
import com.sami.setra.ui.components.SetraPrimaryButton
import com.sami.setra.ui.components.SetraSectionHeader
import com.sami.setra.ui.theme.ButtonShape
import com.sami.setra.ui.theme.Dimens
import com.sami.setra.ui.theme.SetraTheme

val DAY_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    viewModel: RoutineDetailViewModel,
    onBackClick: () -> Unit,
    onAddExerciseClick: (workoutId: Long) -> Unit,
    onRoutineDuplicated: (newRoutineId: Long) -> Unit,
    modifier: Modifier = Modifier,
    onStartWorkout: (sessionId: Long) -> Unit = {}
) {
    val routineWithDays by viewModel.routineState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { targetUri: Uri? ->
        if (targetUri != null) {
            viewModel.exportRoutineToUri(targetUri) {
                Toast.makeText(context, "Routine exported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showHeaderEditDialog by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }

    var workoutToEditName by remember { mutableStateOf<WorkoutEntity?>(null) }

    if (routineWithDays == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val routine = routineWithDays!!.routine
    val days = routineWithDays!!.sortedDays
    var expandedDayId by remember(routine.id) { mutableStateOf<Long?>(null) }

    // Header edit dialog
    if (showHeaderEditDialog) {
        var editName by remember { mutableStateOf(routine.name) }
        var editDesc by remember { mutableStateOf(routine.description) }

        AlertDialog(
            onDismissRequest = { showHeaderEditDialog = false },
            title = { Text("Edit Routine Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Routine Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateRoutineHeader(editName, editDesc)
                        showHeaderEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHeaderEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Workout rename dialog
    if (workoutToEditName != null) {
        var editWorkoutName by remember { mutableStateOf(workoutToEditName!!.name) }
        var editWorkoutDesc by remember { mutableStateOf(workoutToEditName!!.description) }

        AlertDialog(
            onDismissRequest = { workoutToEditName = null },
            title = { Text("Edit Workout", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editWorkoutName,
                        onValueChange = { editWorkoutName = it },
                        label = { Text("Workout Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                    OutlinedTextField(
                        value = editWorkoutDesc,
                        onValueChange = { editWorkoutDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateWorkout(
                            workoutToEditName!!.copy(
                                name = editWorkoutName.ifBlank { "Workout" },
                                description = editWorkoutDesc
                            )
                        )
                        workoutToEditName = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { workoutToEditName = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = routine.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Routine Options"
                        )
                    }
                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Details") },
                            onClick = {
                                showTopMenu = false
                                showHeaderEditDialog = true
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Routine") },
                            onClick = {
                                showTopMenu = false
                                viewModel.prepareRoutineShare { contentUri ->
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/octet-stream"
                                        putExtra(Intent.EXTRA_STREAM, contentUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share FitFactor Routine"))
                                }
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Save Routine File") },
                            onClick = {
                                showTopMenu = false
                                coroutineScope.launch {
                                    val filename = viewModel.getExportFilename()
                                    createDocumentLauncher.launch(filename)
                                }
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate Routine") },
                            onClick = {
                                showTopMenu = false
                                viewModel.duplicateRoutine { newId ->
                                    onRoutineDuplicated(newId)
                                }
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Routine") },
                            onClick = {
                                showTopMenu = false
                                viewModel.deleteRoutine(onBackClick)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall)
        ) {
            // Header Info Card
            SetraGlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceMedium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { heading() }
                        )

                        IconButton(onClick = { showHeaderEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Routine Name and Description",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (routine.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(Dimens.spaceXSmall))
                        Text(
                            text = routine.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SetraTheme.extendedColors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.spaceSmall))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceXSmall)
                        ) {
                            Text(
                                text = "${routineWithDays!!.activeDaysCount} Training Days • ${routineWithDays!!.restDaysCount} Rest Days",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            SetraSectionHeader(title = "Weekly Schedule")

            // Render 7 days
            days.forEachIndexed { dayIndex, dayWithWorkouts ->
                val dayName = DAY_NAMES.getOrElse(dayWithWorkouts.routineDay.dayOfWeek - 1) { "Day ${dayWithWorkouts.routineDay.dayOfWeek}" }
                val dayWorkouts = dayWithWorkouts.sortedWorkouts

                RoutineDayCard(
                    dayName = dayName,
                    dayWithWorkouts = dayWithWorkouts,
                    isExpanded = expandedDayId == dayWithWorkouts.routineDay.id,
                    onToggleExpanded = {
                        expandedDayId = if (expandedDayId == dayWithWorkouts.routineDay.id) {
                            null
                        } else {
                            dayWithWorkouts.routineDay.id
                        }
                    },
                    onStartWorkout = { workoutId ->
                        viewModel.startWorkout(workoutId) { sessionId ->
                            onStartWorkout(sessionId)
                        }
                    },
                    onAddWorkout = { viewModel.addWorkout(dayWithWorkouts.routineDay.id) },
                    onEditWorkout = { workoutToEditName = it.workout },
                    onDuplicateWorkout = { viewModel.duplicateWorkout(it.workout.id) },
                    onDeleteWorkout = { viewModel.deleteWorkout(it.workout.id) },
                    onMoveWorkoutUp = { viewModel.moveWorkoutUp(it.workout, dayWorkouts.map { w -> w.workout }) },
                    onMoveWorkoutDown = { viewModel.moveWorkoutDown(it.workout, dayWorkouts.map { w -> w.workout }) },
                    onAddExerciseClick = onAddExerciseClick,
                    onDeleteExercise = { viewModel.deleteRoutineExercise(it.routineExercise.id) },
                    onDuplicateExercise = { viewModel.duplicateRoutineExercise(it.routineExercise.id) },
                    onMoveExerciseUp = { ex, workout ->
                        viewModel.moveExerciseUp(
                            ex.routineExercise,
                            workout.sortedExercises.map { it.routineExercise }
                        )
                    },
                    onMoveExerciseDown = { ex, workout ->
                        viewModel.moveExerciseDown(
                            ex.routineExercise,
                            workout.sortedExercises.map { it.routineExercise }
                        )
                    },
                    onAddSetTemplate = { viewModel.addSetTemplate(it.routineExercise.id) },
                    onUpdateSetTemplate = { viewModel.updateSetTemplate(it) },
                    onDeleteSetTemplate = { viewModel.deleteSetTemplate(it) },
                    onMoveSetTemplateUp = { set, ex ->
                        viewModel.moveSetTemplateUp(set, ex.sortedSetTemplates)
                    },
                    onMoveSetTemplateDown = { set, ex ->
                        viewModel.moveSetTemplateDown(set, ex.sortedSetTemplates)
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            }

            Spacer(modifier = Modifier.height(Dimens.spaceXLarge))
        }
    }
}

@Composable
fun RoutineDayCard(
    dayName: String,
    dayWithWorkouts: RoutineDayWithWorkouts,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onStartWorkout: (workoutId: Long) -> Unit,
    onAddWorkout: () -> Unit,
    onEditWorkout: (WorkoutWithExercises) -> Unit,
    onDuplicateWorkout: (WorkoutWithExercises) -> Unit,
    onDeleteWorkout: (WorkoutWithExercises) -> Unit,
    onMoveWorkoutUp: (WorkoutWithExercises) -> Unit,
    onMoveWorkoutDown: (WorkoutWithExercises) -> Unit,
    onAddExerciseClick: (workoutId: Long) -> Unit,
    onDeleteExercise: (RoutineExerciseWithDetails) -> Unit,
    onDuplicateExercise: (RoutineExerciseWithDetails) -> Unit,
    onMoveExerciseUp: (RoutineExerciseWithDetails, WorkoutWithExercises) -> Unit,
    onMoveExerciseDown: (RoutineExerciseWithDetails, WorkoutWithExercises) -> Unit,
    onAddSetTemplate: (RoutineExerciseWithDetails) -> Unit,
    onUpdateSetTemplate: (SetTemplateEntity) -> Unit,
    onDeleteSetTemplate: (Long) -> Unit,
    onMoveSetTemplateUp: (SetTemplateEntity, RoutineExerciseWithDetails) -> Unit,
    onMoveSetTemplateDown: (SetTemplateEntity, RoutineExerciseWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val workouts = dayWithWorkouts.sortedWorkouts
    val isRestDay = workouts.isEmpty()

    SetraCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
        borderColor = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f) else Color.Transparent,
        onClick = onToggleExpanded
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 46.dp)
                        .clip(RoundedCornerShape(Dimens.radiusMedium))
                        .background(if (isExpanded && !isRestDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName.take(3),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpanded && !isRestDay) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(Dimens.spaceMedium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRestDay) "Rest Day" else workouts.firstOrNull()?.workout?.name ?: "Workout",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isRestDay) "Recovery & mobility" else workouts.firstOrNull()?.workout?.description.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = SetraTheme.extendedColors.textMuted,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                if (isExpanded) {
                    IconButton(onClick = onAddWorkout, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add workout", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse day" else "Expand day",
                    tint = if (isExpanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isExpanded && isRestDay) {
                Spacer(Modifier.height(Dimens.spaceSmall))
                Text("Rest and recover", style = MaterialTheme.typography.bodySmall, color = SetraTheme.extendedColors.textMuted)
            } else if (isExpanded) {
                Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                workouts.forEachIndexed { workoutIndex, workoutWithExercises ->
                    WorkoutBlock(
                        workoutWithExercises = workoutWithExercises,
                        isFirst = workoutIndex == 0,
                        isLast = workoutIndex == workouts.size - 1,
                        onStart = { onStartWorkout(workoutWithExercises.workout.id) },
                        onEdit = { onEditWorkout(workoutWithExercises) },
                        onDuplicate = { onDuplicateWorkout(workoutWithExercises) },
                        onDelete = { onDeleteWorkout(workoutWithExercises) },
                        onMoveUp = { onMoveWorkoutUp(workoutWithExercises) },
                        onMoveDown = { onMoveWorkoutDown(workoutWithExercises) },
                        onAddExerciseClick = { onAddExerciseClick(workoutWithExercises.workout.id) },
                        onDeleteExercise = onDeleteExercise,
                        onDuplicateExercise = onDuplicateExercise,
                        onMoveExerciseUp = { ex -> onMoveExerciseUp(ex, workoutWithExercises) },
                        onMoveExerciseDown = { ex -> onMoveExerciseDown(ex, workoutWithExercises) },
                        onAddSetTemplate = onAddSetTemplate,
                        onUpdateSetTemplate = onUpdateSetTemplate,
                        onDeleteSetTemplate = onDeleteSetTemplate,
                        onMoveSetTemplateUp = onMoveSetTemplateUp,
                        onMoveSetTemplateDown = onMoveSetTemplateDown
                    )

                    if (workoutIndex < workouts.size - 1) {
                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutBlock(
    workoutWithExercises: WorkoutWithExercises,
    isFirst: Boolean,
    isLast: Boolean,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onDeleteExercise: (RoutineExerciseWithDetails) -> Unit,
    onDuplicateExercise: (RoutineExerciseWithDetails) -> Unit,
    onMoveExerciseUp: (RoutineExerciseWithDetails) -> Unit,
    onMoveExerciseDown: (RoutineExerciseWithDetails) -> Unit,
    onAddSetTemplate: (RoutineExerciseWithDetails) -> Unit,
    onUpdateSetTemplate: (SetTemplateEntity) -> Unit,
    onDeleteSetTemplate: (Long) -> Unit,
    onMoveSetTemplateUp: (SetTemplateEntity, RoutineExerciseWithDetails) -> Unit,
    onMoveSetTemplateDown: (SetTemplateEntity, RoutineExerciseWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val workout = workoutWithExercises.workout
    val exercises = workoutWithExercises.sortedExercises
    var showWorkoutMenu by remember { mutableStateOf(false) }
    var expandedExerciseId by remember(workout.id) { mutableStateOf<Long?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.spaceXSmall)
    ) {
            // Workout Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (workout.description.isNotBlank()) {
                        Text(
                            text = workout.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SetraTheme.extendedColors.textMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onStart,
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = Dimens.spaceSmall, vertical = 0.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .padding(end = Dimens.spaceXSmall)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.spaceXSmall))
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!isFirst) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Move Workout Up")
                        }
                    }
                    if (!isLast) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Move Workout Down")
                        }
                    }

                    Box {
                        IconButton(onClick = { showWorkoutMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Workout Options")
                        }
                        DropdownMenu(
                            expanded = showWorkoutMenu,
                            onDismissRequest = { showWorkoutMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename Workout") },
                                onClick = {
                                    showWorkoutMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate Workout") },
                                onClick = {
                                    showWorkoutMenu = false
                                    onDuplicate()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Workout") },
                                onClick = {
                                    showWorkoutMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            // Compact exercise list; tap a row to reveal its full set editor.
            if (exercises.isEmpty()) {
                Text(
                    text = "No exercises in this workout.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SetraTheme.extendedColors.textMuted,
                    modifier = Modifier.padding(vertical = Dimens.spaceSmall)
                )
            } else {
                exercises.forEachIndexed { exIndex, routineExerciseWithDetails ->
                    val isExerciseExpanded = expandedExerciseId == routineExerciseWithDetails.routineExercise.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusSmall))
                            .clickable {
                                expandedExerciseId = if (isExerciseExpanded) null
                                else routineExerciseWithDetails.routineExercise.id
                            }
                            .padding(horizontal = Dimens.spaceSmall, vertical = Dimens.spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(Dimens.spaceSmall))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routineExerciseWithDetails.exercise?.name
                                    ?: routineExerciseWithDetails.routineExercise.exerciseId,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = exercisePrescriptionSummary(routineExerciseWithDetails),
                                style = MaterialTheme.typography.bodySmall,
                                color = SetraTheme.extendedColors.textMuted
                            )
                        }
                        Icon(
                            imageVector = if (isExerciseExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExerciseExpanded) "Hide exercise set details" else "Show exercise set details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isExerciseExpanded) {
                        RoutineExerciseBlock(
                            routineExerciseWithDetails = routineExerciseWithDetails,
                            isFirst = exIndex == 0,
                            isLast = exIndex == exercises.size - 1,
                            onDelete = { onDeleteExercise(routineExerciseWithDetails) },
                            onDuplicate = { onDuplicateExercise(routineExerciseWithDetails) },
                            onMoveUp = { onMoveExerciseUp(routineExerciseWithDetails) },
                            onMoveDown = { onMoveExerciseDown(routineExerciseWithDetails) },
                            onAddSet = { onAddSetTemplate(routineExerciseWithDetails) },
                            onUpdateSet = onUpdateSetTemplate,
                            onDeleteSet = onDeleteSetTemplate,
                            onMoveSetTemplateUp = { set -> onMoveSetTemplateUp(set, routineExerciseWithDetails) },
                            onMoveSetTemplateDown = { set -> onMoveSetTemplateDown(set, routineExerciseWithDetails) }
                        )
                    }

                    if (exIndex < exercises.size - 1) {
                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            TextButton(onClick = onAddExerciseClick) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(Dimens.spaceXSmall))
                Text("Add Exercise", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
    }
}

private fun exercisePrescriptionSummary(exercise: RoutineExerciseWithDetails): String {
    val sets = exercise.sortedSetTemplates
    val referenceNote = sets.firstNotNullOfOrNull { set ->
        set.notes?.takeIf { it.contains("prescription", ignoreCase = true) }
    }
    if (referenceNote != null) return referenceNote.substringAfter(':').trim()

    val setCount = sets.size.coerceAtLeast(1)
    val targetReps = sets.firstOrNull()?.targetReps?.toInt()?.toString()
    val duration = sets.firstOrNull()?.durationSeconds
    return when {
        targetReps != null -> "$setCount × $targetReps reps"
        duration != null -> "$setCount × ${duration}s"
        else -> "$setCount sets"
    }
}

@Composable
fun RoutineExerciseBlock(
    routineExerciseWithDetails: RoutineExerciseWithDetails,
    isFirst: Boolean,
    isLast: Boolean,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onAddSet: () -> Unit,
    onUpdateSet: (SetTemplateEntity) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onMoveSetTemplateUp: (SetTemplateEntity) -> Unit,
    onMoveSetTemplateDown: (SetTemplateEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val exerciseName = routineExerciseWithDetails.exercise?.name ?: routineExerciseWithDetails.routineExercise.exerciseId
    val exercise = routineExerciseWithDetails.exercise
    val tracking = exerciseTracking(exercise?.category, exercise?.equipment, exercise?.mechanic)
    val sets = routineExerciseWithDetails.sortedSetTemplates
    var showExMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusSmall))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Dimens.spaceSmall)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (routineExerciseWithDetails.exercise != null) {
                        Text(
                            text = "${routineExerciseWithDetails.exercise.equipment ?: "General"} • ${routineExerciseWithDetails.exercise.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SetraTheme.extendedColors.textMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isFirst) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Move Exercise Up")
                        }
                    }
                    if (!isLast) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Move Exercise Down")
                        }
                    }

                    Box {
                        IconButton(onClick = { showExMenu = true }, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Exercise Options")
                        }
                        DropdownMenu(
                            expanded = showExMenu,
                            onDismissRequest = { showExMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Duplicate Exercise") },
                                onClick = {
                                    showExMenu = false
                                    onDuplicate()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove Exercise") },
                                onClick = {
                                    showExMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceXSmall))

            // Set Templates List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spaceXSmall),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "SET", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SetraTheme.extendedColors.textMuted, modifier = Modifier.width(36.dp))
                Text(text = "TYPE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SetraTheme.extendedColors.textMuted, modifier = Modifier.width(60.dp))
                if (tracking.repetitions) Text(text = "REPS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SetraTheme.extendedColors.textMuted, modifier = Modifier.width(52.dp))
                if (tracking.externalResistance) Text(text = "KG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SetraTheme.extendedColors.textMuted, modifier = Modifier.width(52.dp))
                if (tracking.duration) Text(text = "SEC", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SetraTheme.extendedColors.textMuted, modifier = Modifier.width(52.dp))
                if (tracking.distance) Text(text = "M", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SetraTheme.extendedColors.textMuted, modifier = Modifier.width(52.dp))
                Text(text = "REST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SetraTheme.extendedColors.textMuted, modifier = Modifier.width(48.dp))
                Spacer(modifier = Modifier.width(28.dp))
            }

            sets.forEachIndexed { setIndex, setTemplate ->
                SetTemplateRow(
                    setIndex = setIndex + 1,
                    setTemplate = setTemplate,
                    tracking = tracking,
                    onUpdate = onUpdateSet,
                    onDelete = { onDeleteSet(setTemplate.id) }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceXSmall))

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(Dimens.spaceXSmall))
                Text(text = "+ Add Set", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SetTemplateRow(
    setIndex: Int,
    setTemplate: SetTemplateEntity,
    tracking: ExerciseTracking,
    onUpdate: (SetTemplateEntity) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Set #
        Text(
            text = "$setIndex",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(36.dp)
        )

        // Type Button (Normal / Warmup / Drop / Failure)
        val typeLabel = when (setTemplate.setType) {
            "WARMUP" -> "W"
            "DROP" -> "D"
            "FAILURE" -> "F"
            else -> "N"
        }
        val typeColor = when (setTemplate.setType) {
            "WARMUP" -> MaterialTheme.colorScheme.tertiary
            "DROP" -> MaterialTheme.colorScheme.secondary
            "FAILURE" -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primary
        }

        Box(
            modifier = Modifier
                .width(60.dp)
                .clip(CircleShape)
                .background(typeColor.copy(alpha = 0.2f))
                .clickable {
                    val nextType = when (setTemplate.setType) {
                        "NORMAL" -> "WARMUP"
                        "WARMUP" -> "DROP"
                        "DROP" -> "FAILURE"
                        else -> "NORMAL"
                    }
                    onUpdate(setTemplate.copy(setType = nextType))
                }
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = typeColor
            )
        }

        // Reps Input
        val repsText = setTemplate.targetReps?.toInt()?.toString() ?: ""
        if (tracking.repetitions) Box(
            modifier = Modifier
                .width(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedTextField(
                value = repsText,
                onValueChange = { newVal ->
                    val doubleVal = newVal.toDoubleOrNull() ?: 0.0
                    onUpdate(setTemplate.copy(targetReps = doubleVal))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // Weight Input
        val weightText = setTemplate.targetWeight?.toString() ?: ""
        if (tracking.externalResistance) Box(
            modifier = Modifier
                .width(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { newVal ->
                    val doubleVal = newVal.toDoubleOrNull()
                    onUpdate(setTemplate.copy(targetWeight = doubleVal))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("-", style = MaterialTheme.typography.bodySmall) },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        if (tracking.duration) {
            val durationText = setTemplate.durationSeconds?.toString() ?: ""
            OutlinedTextField(
                value = durationText,
                onValueChange = { onUpdate(setTemplate.copy(durationSeconds = it.toIntOrNull())) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(52.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                placeholder = { Text("sec", style = MaterialTheme.typography.labelSmall) }
            )
        }
        if (tracking.distance) {
            val distanceText = setTemplate.targetDistanceMeters?.let { (it / 1000.0).toString().trimEnd('0').trimEnd('.') } ?: ""
            OutlinedTextField(
                value = distanceText,
                onValueChange = { onUpdate(setTemplate.copy(targetDistanceMeters = it.toDoubleOrNull()?.times(1000.0))) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.width(52.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                placeholder = { Text("km", style = MaterialTheme.typography.labelSmall) }
            )
        }

        // Rest Seconds
        val restText = setTemplate.restSeconds?.let { "${it}s" } ?: "Default"
        Text(
            text = restText,
            style = MaterialTheme.typography.bodySmall,
            color = SetraTheme.extendedColors.textMuted,
            modifier = Modifier.width(48.dp)
        )

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete set",
                tint = SetraTheme.extendedColors.textMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
