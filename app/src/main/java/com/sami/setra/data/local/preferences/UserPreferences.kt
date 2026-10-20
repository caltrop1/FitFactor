package com.sami.setra.data.local.preferences

import com.sami.setra.ui.theme.ThemeMode

enum class WeightUnit {
    KG, LBS
}

enum class DistanceUnit {
    KM, MILES
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val defaultRestSeconds: Int = 90
)
