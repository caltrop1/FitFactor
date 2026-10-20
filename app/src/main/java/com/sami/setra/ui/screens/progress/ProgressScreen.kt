package com.sami.setra.ui.screens.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.ui.components.PRCard
import com.sami.setra.ui.components.ScreenPlaceholder
import com.sami.setra.ui.components.SetraCard
import com.sami.setra.ui.components.charts.SetraBarChart
import com.sami.setra.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSessionDetail: (Long) -> Unit,
    onNavigateToExerciseList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val frequencyStats by viewModel.frequencyStats.collectAsStateWithLifecycle()
    val personalRecords by viewModel.personalRecords.collectAsStateWithLifecycle()
    val recentCompletedSessions by viewModel.recentCompletedSessions.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val stats = frequencyStats

        if (stats == null || stats.totalCompletedWorkouts == 0) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Dimens.spaceMedium),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScreenPlaceholder(
                    title = "No Workout History Yet",
                    subtitle = "Complete your first workout to start tracking training volume, personal records, consistency, and strength progression charts.",
                    badgeText = "Phase 6 Active",
                    icon = Icons.Default.BarChart,
                    upcomingFeatures = listOf(
                        "Automatic Personal Record (PR) detection",
                        "Weekly training volume & set density analytics",
                        "Individual exercise strength progression trends",
                        "Offline workout history log & read-only summaries"
                    )
                )

                Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                Button(
                    onClick = onNavigateToExerciseList,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Explore Exercise Library")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Dimens.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                contentPadding = PaddingValues(
                    top = Dimens.spaceSmall,
                    bottom = Dimens.spaceSmall
                )
            ) {
                item {
                    Text(
                        text = "Progress & Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Top Summary Stat Grid (4 Metrics)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                    ) {
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CheckCircle,
                            title = "Workouts",
                            value = "${stats.totalCompletedWorkouts}"
                        )
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FitnessCenter,
                            title = "Total Volume",
                            value = formatVolume(stats.totalVolumeKg)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                    ) {
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.EmojiEvents,
                            title = "Active PRs",
                            value = "${personalRecords.size}"
                        )
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Speed,
                            title = "This Week",
                            value = "${stats.workoutsThisWeek} sessions"
                        )
                    }
                }

                // Workout Frequency Chart
                item {
                    SetraCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Weekly Training Frequency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Completed sessions over the past 7 days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                            SetraBarChart(dataPoints = stats.weeklyBreakdown)
                        }
                    }
                }

                // Personal Records Section
                if (personalRecords.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personal Records (PRs)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            TextButton(onClick = onNavigateToExerciseList) {
                                Text("Exercise Library")
                            }
                        }
                    }

                    items(personalRecords.take(3)) { pr ->
                        PRCard(
                            record = pr,
                            onClick = { onNavigateToSessionDetail(pr.sessionId) }
                        )
                    }
                }

                // Recent Workout History Header & Items
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Workout History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = onNavigateToHistory) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("View All")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.height(16.dp)
                                )
                            }
                        }
                    }
                }

                items(recentCompletedSessions.take(3)) { session ->
                    HistorySessionCard(
                        sessionWithDetails = session,
                        onClick = { onNavigateToSessionDetail(session.session.id) }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.spaceSmall)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                        Text("Open Full Workout History Log")
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.spaceXSmall)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HistorySessionCard(
    sessionWithDetails: WorkoutSessionWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = sessionWithDetails.session
    val completedAt = session.completedAt ?: session.startedAt
    val sdf = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
    val dateFormatted = sdf.format(Date(completedAt))

    val durationMillis = maxOf(0L, completedAt - session.startedAt)
    val durationMin = durationMillis / (60 * 1000)

    var totalVol = 0.0
    for (ex in sessionWithDetails.sessionExercises) {
        for (st in ex.completedSets) {
            if (st.isCompleted && st.weight != null && st.reps != null && st.weight > 0 && st.reps > 0) {
                totalVol += st.weight * st.reps
            }
        }
    }

    SetraCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
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
                    text = session.workoutName.ifBlank { "Workout" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${durationMin}m",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (session.routineName.isNotBlank()) {
                Text(
                    text = session.routineName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

                    Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${sessionWithDetails.completedExercisesCount} ex • ${sessionWithDetails.completedSetsCount} sets • ${formatVolume(totalVol)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatVolume(volKg: Double): String {
    return if (volKg >= 1000.0) {
        String.format(Locale.getDefault(), "%.1ft", volKg / 1000.0)
    } else {
        "${volKg.toInt()} kg"
    }
}
