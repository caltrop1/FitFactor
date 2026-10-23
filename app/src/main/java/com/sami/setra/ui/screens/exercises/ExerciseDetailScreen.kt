package com.sami.setra.ui.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.sami.setra.ui.components.PRCard
import com.sami.setra.ui.components.SetraCard
import com.sami.setra.ui.components.SetraGlassSurface
import com.sami.setra.ui.components.SetraSectionHeader
import com.sami.setra.ui.components.charts.SetraLineChart
import com.sami.setra.ui.screens.progress.HistorySessionCard
import com.sami.setra.ui.theme.Dimens
import com.sami.setra.ui.theme.SetraTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    viewModel: ExerciseDetailViewModel,
    onBackClick: () -> Unit,
    onSessionClick: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    val exerciseWithDetails by viewModel.exerciseDetails.collectAsStateWithLifecycle()
    val progressionPoints by viewModel.progressionPoints.collectAsStateWithLifecycle()
    val personalRecords by viewModel.personalRecords.collectAsStateWithLifecycle()
    val completedSessions by viewModel.completedSessions.collectAsStateWithLifecycle()

    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exerciseWithDetails?.exercise?.name ?: "Exercise Details",
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
        val details = exerciseWithDetails
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
            val exercise = details.exercise
            val context = LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceMedium)
            ) {
                // Exercise Images Section
                if (details.images.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        details.images.sortedBy { it.ordering }.forEach { img ->
                            SetraCard(
                                onClick = { fullScreenImagePath = img.assetPath },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(200.dp)
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("file:///android_asset/${img.assetPath}")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = exercise.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                                                tint = SetraTheme.extendedColors.textMuted,
                                                modifier = Modifier.size(Dimens.iconXLarge)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                }

                // Exercise Title & Badges
                SetraGlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spaceLarge)
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.semantics { heading() }
                        )

                        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                        ) {
                            if (!exercise.equipment.isNullOrBlank()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Equipment: ${exercise.equipment.lowercase().replaceFirstChar { it.uppercase() }}") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            AssistChip(
                                onClick = {},
                                label = { Text("Level: ${exercise.level.lowercase().replaceFirstChar { it.uppercase() }}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            AssistChip(
                                onClick = {},
                                label = { Text("Category: ${exercise.category.lowercase().replaceFirstChar { it.uppercase() }}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            if (!exercise.force.isNullOrBlank()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Force: ${exercise.force.lowercase().replaceFirstChar { it.uppercase() }}") }
                                )
                            }

                            if (!exercise.mechanic.isNullOrBlank()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Mechanic: ${exercise.mechanic.lowercase().replaceFirstChar { it.uppercase() }}") }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                // Personal Records Section
                if (personalRecords.isNotEmpty()) {
                    SetraSectionHeader(title = "Personal Records")
                    personalRecords.forEach { pr ->
                        PRCard(
                            record = pr,
                            modifier = Modifier.padding(vertical = Dimens.spaceXSmall)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                }

                // Exercise Weight Progression Chart
                if (progressionPoints.isNotEmpty()) {
                    SetraSectionHeader(title = "Weight Progression")
                    SetraGlassSurface(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.spaceMedium)
                        ) {
                            SetraLineChart(
                                points = progressionPoints,
                                valueSelector = { it.bestWeight },
                                unit = "kg"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                }

                // Primary Muscles
                if (details.primaryMuscles.isNotEmpty()) {
                    SetraSectionHeader(title = "Primary Muscles")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        details.primaryMuscles.forEach { muscle ->
                            SetraCard(
                                modifier = Modifier.padding(vertical = Dimens.spaceXSmall)
                            ) {
                                Text(
                                    text = muscle.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                }

                // Secondary Muscles
                if (details.secondaryMuscles.isNotEmpty()) {
                    SetraSectionHeader(title = "Secondary Muscles")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        details.secondaryMuscles.forEach { muscle ->
                            SetraCard(
                                modifier = Modifier.padding(vertical = Dimens.spaceXSmall)
                            ) {
                                Text(
                                    text = muscle.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                }

                // Instructions
                if (exercise.instructions.isNotBlank()) {
                    SetraSectionHeader(title = "Instructions")
                    val steps = exercise.instructions.split("\n\n").filter { it.isNotBlank() }

                    steps.forEachIndexed { index, step ->
                        SetraCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.spaceXSmall)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(Dimens.spaceMedium))

                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                }

                // Completed Workout History for this Exercise
                if (completedSessions.isNotEmpty()) {
                    SetraSectionHeader(title = "Exercise Training History")
                    completedSessions.forEach { session ->
                        HistorySessionCard(
                            sessionWithDetails = session,
                            onClick = { onSessionClick?.invoke(session.session.id) },
                            modifier = Modifier.padding(vertical = Dimens.spaceXSmall)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceXXLarge))
            }
        }
    }

    // Fullscreen Image Dialog
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
                        contentDescription = "Full size image",
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
                            .padding(Dimens.spaceLarge)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
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
