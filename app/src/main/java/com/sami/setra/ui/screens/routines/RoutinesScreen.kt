package com.sami.setra.ui.screens.routines

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sami.setra.data.importexport.resolver.ExerciseResolutionItem
import com.sami.setra.ui.components.SetraCard
import com.sami.setra.ui.components.SetraPillToggle
import com.sami.setra.ui.components.SetraPrimaryButton
import com.sami.setra.ui.theme.ButtonShape
import com.sami.setra.ui.theme.Dimens
import com.sami.setra.ui.theme.SetraTheme
import kotlinx.coroutines.launch

@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel,
    onBrowseSplitsClick: () -> Unit,
    onRoutineClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onOpenSearch: () -> Unit = {},
    onResumeActiveWorkout: (sessionId: Long) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val importPreviewData by viewModel.importPreviewData.collectAsStateWithLifecycle()
    val importErrorMessage by viewModel.importErrorMessage.collectAsStateWithLifecycle()

    var selectedToggleIndex by remember { mutableIntStateOf(0) }
    var exerciseItemToMap by remember { mutableStateOf<ExerciseResolutionItem?>(null) }
    var currentExportRoutineId by remember { mutableLongStateOf(-1L) }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // File Pickers
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadImportFile(it) }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { targetUri: Uri? ->
        if (targetUri != null && currentExportRoutineId > 0L) {
            viewModel.exportRoutineToUri(currentExportRoutineId, targetUri) {
                Toast.makeText(context, "Routine exported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handleCreateRoutine() {
        viewModel.createNewRoutine { newRoutineId ->
            onRoutineClick(newRoutineId)
        }
    }

    fun handleImportRoutineClick() {
        openDocumentLauncher.launch(arrayOf("*/*"))
    }

    fun handleSaveRoutineFile(routineId: Long) {
        currentExportRoutineId = routineId
        coroutineScope.launch {
            val filename = viewModel.getExportFilename(routineId)
            createDocumentLauncher.launch(filename)
        }
    }

    fun handleShareRoutine(routineId: Long) {
        viewModel.prepareRoutineShare(routineId) { contentUri ->
            launchShareIntent(context, contentUri)
        }
    }

    // Import Preview Dialog
    if (importPreviewData != null) {
        ImportPreviewDialog(
            routine = importPreviewData!!.routine,
            resolutionSummary = importPreviewData!!.resolutionSummary,
            onDismiss = { viewModel.clearImportPreview() },
            onConfirmImport = { customName, updatedSummary ->
                viewModel.confirmImport(customName, updatedSummary) { importedId ->
                    Toast.makeText(context, "Routine imported successfully!", Toast.LENGTH_SHORT).show()
                    onRoutineClick(importedId)
                }
            },
            onMapExerciseClick = { item ->
                exerciseItemToMap = item
            }
        )
    }

    // Map Exercise Dialog
    if (exerciseItemToMap != null) {
        MapExercisePickerDialog(
            originalExerciseName = exerciseItemToMap!!.originalExerciseName,
            allExercises = allExercises,
            onDismiss = { exerciseItemToMap = null },
            onExerciseSelected = { selectedLocalId ->
                viewModel.mapExercise(exerciseItemToMap!!.originalExerciseId, selectedLocalId)
                exerciseItemToMap = null
            }
        )
    }

    // Error Dialog
    if (importErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearImportError() },
            title = { Text("Import Error", fontWeight = FontWeight.Bold) },
            text = { Text(importErrorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearImportError() }) {
                    Text("OK")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.spaceMedium),
        contentPadding = PaddingValues(
            top = statusBarTop,
            bottom = Dimens.spaceXXLarge
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                ) {
                    Text(
                        text = "My Routines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your saved routines and custom plans.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SetraTheme.extendedColors.textMuted
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.spaceMedium))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search routines",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(Dimens.iconMedium)
                        )
                    }
                }
            }
        }

        // Active Workout Banner Card (if session is in progress)
        if (activeSession != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Workout in Progress",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeSession!!.session.workoutName.ifBlank { "Workout Session" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (activeSession!!.session.routineName.isNotBlank()) {
                                Text(
                                    text = activeSession!!.session.routineName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = { onResumeActiveWorkout(activeSession!!.session.id) },
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Segmented Pill Toggle: [ My Routines ]  [ Created by Me ]
        item {
            SetraPillToggle(
                options = listOf("My Routines", "Created by Me"),
                selectedIndex = selectedToggleIndex,
                onOptionSelected = { selectedToggleIndex = it }
            )
        }

        // Saved Routines Count & Actions Row: [ Import ] [ + Create Routine ]
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Routines (${routines.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXSmall)) {
                    OutlinedButton(
                        onClick = { handleImportRoutineClick() },
                        shape = ButtonShape,
                        contentPadding = PaddingValues(horizontal = Dimens.spaceSmall, vertical = Dimens.spaceXSmall),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Import",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { handleCreateRoutine() },
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceXSmall),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Create",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (routines.isEmpty()) {
            item {
                SetraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spaceLarge)
                    ) {
                        Text(
                            text = "No routines created yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                        Text(
                            text = "Create a custom routine, import a .fitfactor file, or choose a built-in split.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SetraTheme.extendedColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                            SetraPrimaryButton(
                                text = "Create Routine",
                                onClick = { handleCreateRoutine() },
                                modifier = Modifier.weight(1f)
                            )
                            SetraPrimaryButton(
                                text = "Import Routine",
                                onClick = { handleImportRoutineClick() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } else {
            items(
                items = routines,
                key = { it.routine.id }
            ) { routineWithDays ->
                RoutineCard(
                    routineWithDays = routineWithDays,
                    onClick = { onRoutineClick(routineWithDays.routine.id) },
                    onDuplicateClick = {
                        viewModel.duplicateRoutine(routineWithDays.routine.id) { newId ->
                            onRoutineClick(newId)
                        }
                    },
                    onDeleteClick = {
                        viewModel.deleteRoutine(routineWithDays.routine.id)
                    },
                    onShareClick = {
                        handleShareRoutine(routineWithDays.routine.id)
                    },
                    onSaveFileClick = {
                        handleSaveRoutineFile(routineWithDays.routine.id)
                    }
                )
            }
        }
    }
}

private fun launchShareIntent(context: Context, contentUri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share FitFactor Routine"))
}
