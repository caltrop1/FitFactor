package com.sami.setra.ui.screens.profile

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sami.setra.data.local.preferences.DistanceUnit
import com.sami.setra.data.local.preferences.UserPreferences
import com.sami.setra.data.local.preferences.WeightUnit
import com.sami.setra.ui.theme.Dimens
import com.sami.setra.ui.theme.SetraTheme
import com.sami.setra.ui.theme.ThemeMode

@Composable
fun ProfileScreen(
    userPreferences: UserPreferences,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onDistanceUnitChange: (DistanceUnit) -> Unit,
    onDefaultRestSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
    ) {
        ProfileHeader()

        Column {
            ProfileSectionTitle(icon = Icons.Default.WbSunny, title = "Appearance")
            Spacer(Modifier.height(Dimens.spaceSmall))
            ProfilePanel {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Dimens.spaceSmall))
                ProfileSegments(
                    options = listOf(
                        SegmentOption("System", Icons.Default.Settings, userPreferences.themeMode == ThemeMode.SYSTEM),
                        SegmentOption("Light", Icons.Default.WbSunny, userPreferences.themeMode == ThemeMode.LIGHT),
                        SegmentOption("Dark", Icons.Default.DarkMode, userPreferences.themeMode == ThemeMode.DARK)
                    ),
                    onSelect = { index -> onThemeModeChange(ThemeMode.entries[index]) }
                )
            }
        }

        Column {
            ProfileSectionTitle(icon = Icons.Default.FitnessCenter, title = "Workout Defaults")
            Spacer(Modifier.height(Dimens.spaceSmall))
            ProfilePanel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Default Rest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val minutes = userPreferences.defaultRestSeconds / 60
                        val seconds = userPreferences.defaultRestSeconds % 60
                        Text(
                            text = if (minutes > 0 && seconds > 0) "${minutes}m ${seconds}s" else if (minutes > 0) "${minutes}m" else "${seconds}s",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    RoundActionButton(
                        icon = Icons.Default.Remove,
                        description = "Decrease default rest by 15 seconds",
                        onClick = {
                            onDefaultRestSecondsChange((userPreferences.defaultRestSeconds - 15).coerceAtLeast(15))
                        }
                    )
                    Spacer(Modifier.width(Dimens.spaceSmall))
                    RoundActionButton(
                        icon = Icons.Default.Add,
                        description = "Increase default rest by 15 seconds",
                        onClick = {
                            onDefaultRestSecondsChange((userPreferences.defaultRestSeconds + 15).coerceAtMost(600))
                        }
                    )
                }
            }
        }

        Column {
            ProfileSectionTitle(icon = Icons.Default.Straighten, title = "Units & Formatting")
            Spacer(Modifier.height(Dimens.spaceSmall))
            ProfilePanel(contentPadding = 8.dp) {
                UnitPreferenceRow(
                    icon = Icons.Default.FitnessCenter,
                    title = "Weight Unit",
                    options = WeightUnit.entries.map { it.name },
                    selectedIndex = WeightUnit.entries.indexOf(userPreferences.weightUnit),
                    onSelect = { onWeightUnitChange(WeightUnit.entries[it]) },
                    showIcons = false
                )
                ProfileDivider()
                UnitPreferenceRow(
                    icon = Icons.Default.LocationOn,
                    title = "Distance Unit",
                    options = DistanceUnit.entries.map { it.name },
                    selectedIndex = DistanceUnit.entries.indexOf(userPreferences.distanceUnit),
                    onSelect = { onDistanceUnitChange(DistanceUnit.entries[it]) },
                    showIcons = false
                )
            }
        }
        
        InfoPreferenceCard(
            icon = Icons.Default.Storage,
            title = "Exercise Database",
            subtitle = { ExerciseDatabaseLink() },
            detail = "yuhonas/free-exercise-db • Unlicense • Available offline"
        )

        InfoPreferenceCard(
            icon = Icons.Default.Shield,
            title = "Privacy & Data",
            subtitle = {
                Text(
                    text = "Your data stays on your device",
                    style = MaterialTheme.typography.bodySmall,
                    color = SetraTheme.extendedColors.textMuted
                )
            },
            detail = "100% Offline"
        )
        
    }
}

@Composable
private fun ProfileHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(42.dp)
        )

        Spacer(Modifier.width(Dimens.spaceMedium))
        Column(modifier = Modifier.weight(1f).semantics { heading() }) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ProfileSectionTitle(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
        modifier = Modifier.semantics { heading() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ProfilePanel(
    contentPadding: androidx.compose.ui.unit.Dp = 10.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(contentPadding), content = { content() })
    }
}

private data class SegmentOption(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean
)

@Composable
private fun ProfileSegments(
    options: List<SegmentOption>,
    onSelect: (Int) -> Unit,
    showIcons: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(if (showIcons) 36.dp else 32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (option.selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 1.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showIcons) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = if (option.selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = option.label,
                    style = if (showIcons) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall,
                    fontWeight = if (option.selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (option.selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun UnitPreferenceRow(
    icon: ImageVector,
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    showIcons: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(Dimens.spaceMedium))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(Dimens.spaceSmall))
        ProfileSegments(
            options = options.mapIndexed { index, label ->
                SegmentOption(label, Icons.Default.Settings, index == selectedIndex)
            },
            onSelect = onSelect,
            showIcons = showIcons,
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
private fun ProfileDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun RoundActionButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.background,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(46.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun InfoPreferenceCard(
    icon: ImageVector,
    title: String,
    subtitle: @Composable () -> Unit,
    detail: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(Dimens.spaceMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle()
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExerciseDatabaseLink() {
    val uriHandler = LocalUriHandler.current
    Text(
        text = "Free Exercise DB  ↗",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clickable { uriHandler.openUri("https://github.com/yuhonas/free-exercise-db") }
            .semantics { role = Role.Button }
    )
}
