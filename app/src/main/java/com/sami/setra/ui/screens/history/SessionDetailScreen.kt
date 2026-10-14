package com.sami.setra.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sami.setra.ui.components.SetraGlassSurface
import com.sami.setra.ui.components.SetraSectionHeader
import com.sami.setra.ui.screens.progress.StatMetricCard
import com.sami.setra.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    viewModel: SessionDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessionWithDetails by viewModel.sessionDetails.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Completed Session",
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
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Session",
                            tint = MaterialTheme.colorScheme.error
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
        val details = sessionWithDetails

        if (details == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val session = details.session
            val completedAt = session.completedAt ?: session.startedAt
            val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault())
            val dateFormatted = sdf.format(Date(completedAt))

            val durationMillis = maxOf(0L, completedAt - session.startedAt)
            val durationSec = durationMillis / 1000
            val hours = durationSec / 3600
            val minutes = (durationSec % 3600) / 60
            val seconds = durationSec % 60
            val durationFormatted = if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }

            var totalVol = 0.0
            for (ex in details.sessionExercises) {
                for (st in ex.completedSets) {
                    if (st.isCompleted && st.weight != null && st.reps != null && st.weight > 0 && st.reps > 0) {
                        totalVol += st.weight * st.reps
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Dimens.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
            ) {
                // Header Details
                item {
                    SetraGlassSurface(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.spaceLarge)
                        ) {
                            Text(
                                text = session.workoutName.ifBlank { "Workout" },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (session.routineName.isNotBlank()) {
                                Text(
                                    text = session.routineName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!session.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                                Text(
                                    text = "Notes: ${session.notes}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Stats Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                    ) {
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Timer,
                            title = "Duration",
                            value = durationFormatted
                        )
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FitnessCenter,
                            title = "Exercises",
                            value = "${details.completedExercisesCount}/${details.totalExercisesCount}"
                        )
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CheckCircle,
                            title = "Sets Done",
                            value = "${details.completedSetsCount}/${details.totalSetsCount}"
                        )
                    }
                }

                // Performed Exercises
                item {
                    SetraSectionHeader(title = "Performed Exercises")
                }

                items(details.sortedExercises) { sessionEx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
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
                                    text = sessionEx.sessionExercise.exerciseNameSnapshot,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${sessionEx.completedSetsCount}/${sessionEx.totalSetsCount} sets",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (!sessionEx.sessionExercise.notesSnapshot.isNullOrBlank()) {
                                Text(
                                    text = "Notes: ${sessionEx.sessionExercise.notesSnapshot}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

                            sessionEx.sortedCompletedSets.forEachIndexed { idx, set ->
                                val setNum = idx + 1
                                val isDone = set.isCompleted
                                val weightStr = set.weight?.let { "${if (it % 1.0 == 0.0) it.toInt() else it} kg" } ?: "-"
                                val repsStr = set.reps?.let { "${it.toInt()} reps" } ?: set.durationSeconds?.let { "${it}s" } ?: "-"
                                val setTypeTag = if (set.isWarmup || set.setType == "WARMUP") " [Warmup]" else if (set.setType != "NORMAL") " [${set.setType}]" else ""

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Set $setNum$setTypeTag",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$weightStr × $repsStr ${if (isDone) "✓" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Dimens.spaceXXLarge))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Historical Session") },
            text = { Text("Are you sure you want to delete this completed workout record? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSession {
                            onBackClick()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
