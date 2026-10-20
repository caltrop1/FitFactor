package com.sami.setra.data.local.preferences

import com.sami.setra.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun defaultUserPreferences_hasExpectedDefaults() {
        val prefs = UserPreferences()
        assertEquals(ThemeMode.SYSTEM, prefs.themeMode)
        assertEquals(WeightUnit.KG, prefs.weightUnit)
        assertEquals(DistanceUnit.KM, prefs.distanceUnit)
        assertEquals(90, prefs.defaultRestSeconds)
    }

    @Test
    fun userPreferences_customValues_areRetained() {
        val prefs = UserPreferences(
            themeMode = ThemeMode.DARK,
            weightUnit = WeightUnit.LBS,
            distanceUnit = DistanceUnit.MILES,
            defaultRestSeconds = 135
        )
        assertEquals(ThemeMode.DARK, prefs.themeMode)
        assertEquals(WeightUnit.LBS, prefs.weightUnit)
        assertEquals(DistanceUnit.MILES, prefs.distanceUnit)
        assertEquals(135, prefs.defaultRestSeconds)
    }
}
