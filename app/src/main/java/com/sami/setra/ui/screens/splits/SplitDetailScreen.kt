package com.sami.setra.ui.screens.splits

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sami.setra.data.model.SplitTemplate
import com.sami.setra.ui.components.SetraCard
import com.sami.setra.ui.components.SetraPrimaryButton
import com.sami.setra.ui.components.SetraSectionHeader
import com.sami.setra.ui.theme.Dimens
import com.sami.setra.ui.theme.SetraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitDetailScreen(
    split: SplitTemplate,
    onBackClick: () -> Unit,
    onUseSplitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDayIndex by remember(split.id) { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = split.name,
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
                .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceMedium)
        ) {
            // Split overview: icon, description, and compact metadata chips.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.radiusLarge),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(Modifier.width(Dimens.spaceMedium))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = split.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.semantics { heading() }
                        )

                        Spacer(Modifier.height(Dimens.spaceXSmall))

                        Text(
                            text = splitOverviewDescription(split.id),
                            style = MaterialTheme.typography.bodySmall,
                            color = SetraTheme.extendedColors.textMuted
                        )

                        Spacer(Modifier.height(Dimens.spaceSmall))

                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXSmall)) {
                            SplitInfoChip(
                                icon = Icons.Default.FitnessCenter,
                                label = split.targetFrequency.replace(" Days / Week", " days/week"),
                                modifier = Modifier.weight(1f)
                            )
                            SplitInfoChip(
                                icon = Icons.Default.BarChart,
                                label = splitDifficulty(split.id),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            SetraSectionHeader(title = "Weekly Schedule")

            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            split.days.forEachIndexed { index, day ->
                val isExpanded = expandedDayIndex == index
                SetraCard(
                    backgroundColor = if (isExpanded) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    borderColor = if (isExpanded) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    onClick = {
                        expandedDayIndex = if (isExpanded) null else index
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.spaceXSmall)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                        Box(
                            modifier = Modifier
                                .size(width = 56.dp, height = 48.dp)
                                .clip(RoundedCornerShape(Dimens.radiusMedium))
                                .background(
                                    if (!day.isRestDay && isExpanded) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                                Text(
                                    text = day.dayName.take(3),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!day.isRestDay && isExpanded) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }

                        Spacer(modifier = Modifier.width(Dimens.spaceMedium))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (day.isRestDay) "Rest Day" else day.workoutName.orEmpty(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (day.isRestDay) "Recovery & mobility" else day.workoutDescription.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SetraTheme.extendedColors.textMuted
                                )
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                contentDescription = if (isExpanded) "Hide day details" else "Show day details",
                                tint = if (isExpanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Dimens.iconMedium)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                            Text(
                                text = day.workoutDescription.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = SetraTheme.extendedColors.textMuted
                            )
                            if (!day.isRestDay && day.sampleExercises.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceXSmall)) {
                                    day.sampleExercises.forEach { exercise ->
                                        val prescription = if (exercise.repRange == "sets") {
                                            "${exercise.sets} sets"
                                        } else {
                                            "${exercise.sets} × ${exercise.repRange} reps"
                                        }
                                        Text(
                                            text = "• ${exercise.displayName} — $prescription",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            SetraPrimaryButton(
                text = "Use Split (Create My Routine)",
                onClick = onUseSplitClick
            )

            Spacer(modifier = Modifier.height(Dimens.spaceXXLarge))
        }
    }
}

@Composable
private fun SplitInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val chipBlue = androidx.compose.ui.graphics.Color(0xFF60A5FA)
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(chipBlue.copy(alpha = 0.14f))
            .padding(horizontal = Dimens.spaceSmall, vertical = Dimens.spaceXSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = chipBlue,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = chipBlue,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            softWrap = false
        )
    }
}

private fun splitOverviewDescription(splitId: String): String = when (splitId) {
    "full_body" -> "Train major muscle groups in each workout."
    "upper_lower" -> "Build strength with a balanced upper and lower body split."
    "push_pull_legs" -> "Train push, pull, and legs across the week."
    "arnold_split" -> "Pair chest and back, shoulders and arms, and legs."
    "bro_split" -> "Focus on one major body part each session."
    else -> "A flexible weekly training schedule."
}

private fun splitDifficulty(splitId: String): String = when (splitId) {
    "full_body" -> "Beginner"
    "upper_lower", "push_pull_legs" -> "Intermediate"
    "arnold_split", "bro_split" -> "Advanced"
    else -> "All Levels"
}
