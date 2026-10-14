package com.sami.setra.ui.screens.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.sami.setra.ui.components.SetraCard
import com.sami.setra.ui.components.SetraPrimaryButton
import com.sami.setra.ui.components.SetraSecondaryButton
import com.sami.setra.ui.theme.Dimens
import com.sami.setra.ui.theme.SetraTheme

@Composable
fun CreateScreen(
    onCreateRoutineClick: () -> Unit,
    onBrowseSplitsClick: () -> Unit,
    onBrowseExercisesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = Dimens.spaceMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() }
        ) {
            Text(
                text = "Create & Build",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Design a new routine or start with a built-in split.",
                style = MaterialTheme.typography.bodySmall,
                color = SetraTheme.extendedColors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

        // Card 1: Create Custom Routine
        SetraCard(
            onClick = onCreateRoutineClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.spaceSmall)) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimens.spaceSmall)
                )
                Text(
                    text = "Create Custom Routine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spaceXSmall))
                Text(
                    text = "Build a brand new 7-day training week with custom workouts and set templates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SetraTheme.extendedColors.textMuted
                )
                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                SetraPrimaryButton(
                    text = "Build Routine",
                    onClick = onCreateRoutineClick
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        // Card 2: Browse Splits
        SetraCard(
            onClick = onBrowseSplitsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.spaceSmall)) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimens.spaceSmall)
                )
                Text(
                    text = "Use Pre-made Training Split",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spaceXSmall))
                Text(
                    text = "Choose from PPL, Upper/Lower, Arnold Split, Bro Split or Full Body.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SetraTheme.extendedColors.textMuted
                )
                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                SetraSecondaryButton(
                    text = "Browse Training Splits",
                    onClick = onBrowseSplitsClick
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        // Card 3: Browse Exercises
        SetraCard(
            onClick = onBrowseExercisesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.spaceSmall)) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimens.spaceSmall)
                )
                Text(
                    text = "Offline Exercise Database",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.spaceXSmall))
                Text(
                    text = "Explore over 800+ exercise instructions, muscle targets, and equipment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SetraTheme.extendedColors.textMuted
                )
                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                SetraSecondaryButton(
                    text = "Browse Exercises",
                    onClick = onBrowseExercisesClick
                )
            }
        }
    }
}
