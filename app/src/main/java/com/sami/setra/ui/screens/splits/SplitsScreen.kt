package com.sami.setra.ui.screens.splits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight

import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sami.setra.data.model.SplitTemplate
import com.sami.setra.R
import com.sami.setra.ui.components.SetraCard
import com.sami.setra.ui.components.SetraPillToggle
import com.sami.setra.ui.theme.Dimens
import com.sami.setra.ui.theme.SetraTheme
import com.sami.setra.ui.theme.SplitTealAccent

@Composable
fun SplitsScreen(
    splitsViewModel: SplitsViewModel,
    onSplitClick: (String) -> Unit,
    onRoutineClick: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val splits = splitsViewModel.splits
    val mySplits by splitsViewModel.routines.collectAsState()
    var selectedToggleIndex by rememberSaveable { mutableIntStateOf(0) }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f).semantics { heading() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_fitfactor_mark),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) { append("Fit") }
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Factor") }
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
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
                            contentDescription = "Search exercises or splits",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(Dimens.iconMedium)
                        )
                    }
                }
            }
        }

        // Segmented Pill Toggle: [ Browse ]  [ My Splits ]
        item {
            SetraPillToggle(
                options = listOf("Browse", "My Splits"),
                selectedIndex = selectedToggleIndex,
                onOptionSelected = { selectedToggleIndex = it }
            )
        }

        if (selectedToggleIndex == 0) item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.iconMedium)
                    )
                    Spacer(modifier = Modifier.width(Dimens.spaceXSmall))
                    Text(
                        text = "Popular Splits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

            }
        }

        if (selectedToggleIndex == 0) {
            items(splits.filter { it.id != "custom" }, key = { it.id }) { split ->
                SplitCard(split = split, onSplitClick = { onSplitClick(split.id) })
            }
        } else {
            if (mySplits.isEmpty()) {
                item {
                    SetraCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No saved splits yet. Choose a built-in split from Browse to add one here.",
                            modifier = Modifier.padding(Dimens.spaceMedium),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SetraTheme.extendedColors.textMuted
                        )
                    }
                }
            } else {
                items(mySplits, key = { it.routine.id }) { routine ->
                    com.sami.setra.ui.screens.routines.RoutineCard(
                        routineWithDays = routine,
                        onClick = { onRoutineClick(routine.routine.id) },
                        onDuplicateClick = {},
                        onDeleteClick = {}
                    )
                }
            }
        }

        // Custom Split Bottom Card
        if (selectedToggleIndex == 0) item {
            SetraCard(
                onClick = onCreateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(Dimens.radiusMedium))
                                .background(SplitTealAccent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SplitTealAccent,
                                modifier = Modifier.size(Dimens.iconMedium)
                            )
                        }

                        Spacer(modifier = Modifier.width(Dimens.spaceMedium))

                        Column {
                            Text(
                                text = "Create",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SetraTheme.extendedColors.textMuted,
                        modifier = Modifier.size(Dimens.iconMedium)
                    )
                }
            }
        }
    }
}
