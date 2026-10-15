package com.sami.setra.ui.screens.routines

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sami.setra.data.importexport.model.PortableRoutine
import com.sami.setra.data.importexport.resolver.ExerciseResolutionItem
import com.sami.setra.data.importexport.resolver.ExerciseResolutionSummary
import com.sami.setra.ui.components.SetraCard
import com.sami.setra.ui.components.SetraPrimaryButton
import com.sami.setra.ui.theme.ButtonShape
import com.sami.setra.ui.theme.Dimens
import com.sami.setra.ui.theme.SetraTheme

@Composable
fun ImportPreviewDialog(
    routine: PortableRoutine,
    resolutionSummary: ExerciseResolutionSummary,
    onDismiss: () -> Unit,
    onConfirmImport: (customName: String, updatedResolution: ExerciseResolutionSummary) -> Unit,
    onMapExerciseClick: (item: ExerciseResolutionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var customName by remember(routine.metadata.name) { mutableStateOf(routine.metadata.name) }
    var currentResolution by remember(resolutionSummary) { mutableStateOf(resolutionSummary) }

    val totalWorkouts = routine.days.sumOf { it.workouts.size }
    val totalExercises = routine.days.sumOf { day -> day.workouts.sumOf { workout -> workout.exercises.size } }
    val activeDays = routine.days.count { it.workouts.isNotEmpty() }
    val restDays = (7 - activeDays).coerceAtLeast(0)

    val unresolvedItems = currentResolution.unresolvedItems

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(Dimens.spaceMedium),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.spaceLarge)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                        Text(
                            text = "Import Routine",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                // Editable Routine Name
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Routine Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.spaceSmall))

                // Creator & Description Card
                SetraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Dimens.spaceMedium)) {
                        if (!routine.metadata.creator.isNullOrBlank()) {
                            Text(
                                text = "Shared by ${routine.metadata.creator}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = if (routine.metadata.description.isNotBlank()) routine.metadata.description else "No description provided.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SetraTheme.extendedColors.textMuted
                        )

                        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "$activeDays training days · $restDays rest days",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$totalWorkouts workouts · $totalExercises exercises",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SetraTheme.extendedColors.textMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                // Unresolved / Missing Exercises Warning Section
                if (unresolvedItems.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(Dimens.spaceMedium)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                                Text(
                                    text = "${unresolvedItems.size} exercise${if (unresolvedItems.size == 1) "" else "s"} missing from database",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = "Map missing exercises to existing exercises or choose to skip them.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                }

                // Scrollable List of Routine Days & Exercises
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                ) {
                    if (currentResolution.items.isNotEmpty()) {
                        item {
                            Text(
                                text = "Exercises Status (${currentResolution.items.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = Dimens.spaceXSmall)
                            )
                        }

                        items(
                            items = currentResolution.items,
                            key = { it.originalExerciseId }
                        ) { resolutionItem ->
                            ExerciseStatusRow(
                                item = resolutionItem,
                                onMapClick = { onMapExerciseClick(resolutionItem) },
                                onToggleSkip = {
                                    val updatedList = currentResolution.items.map { existing ->
                                        if (existing.originalExerciseId == resolutionItem.originalExerciseId) {
                                            existing.copy(isSkipped = !existing.isSkipped)
                                        } else existing
                                    }
                                    currentResolution = ExerciseResolutionSummary(updatedList)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                        Text(
                            text = "Workouts Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(routine.days.filter { it.workouts.isNotEmpty() }) { day ->
                        val dayName = when (day.dayOfWeek) {
                            1 -> "Monday"
                            2 -> "Tuesday"
                            3 -> "Wednesday"
                            4 -> "Thursday"
                            5 -> "Friday"
                            6 -> "Saturday"
                            7 -> "Sunday"
                            else -> "Day ${day.dayOfWeek}"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.spaceXSmall)
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            day.workouts.forEach { workout ->
                                Text(
                                    text = "• ${workout.name} (${workout.exercises.size} exercises)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = ButtonShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel")
                    }

                    SetraPrimaryButton(
                        text = "Import Routine",
                        onClick = {
                            onConfirmImport(customName, currentResolution)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseStatusRow(
    item: ExerciseResolutionItem,
    onMapClick: () -> Unit,
    onToggleSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    SetraCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.originalExerciseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isSkipped) SetraTheme.extendedColors.textMuted else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.isSkipped) {
                    Text(
                        text = "Will be skipped",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (item.mappedExerciseId != null && item.mappedExerciseId != item.originalExerciseId) {
                    Text(
                        text = "Mapped to custom exercise",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Matched in database",
                        style = MaterialTheme.typography.bodySmall,
                        color = SetraTheme.extendedColors.textMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.mappedExerciseId == null && !item.isSkipped) {
                    TextButton(onClick = onMapClick) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Map", style = MaterialTheme.typography.labelMedium)
                    }
                }

                TextButton(
                    onClick = onToggleSkip,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (item.isSkipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = if (item.isSkipped) "Include" else "Skip",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
