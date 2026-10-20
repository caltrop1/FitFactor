package com.sami.setra.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sami.setra.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
        val DEFAULT_REST_SECONDS = intPreferencesKey("default_rest_seconds")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val weightUnitString = preferences[PreferencesKeys.WEIGHT_UNIT] ?: WeightUnit.KG.name
        val distanceUnitString = preferences[PreferencesKeys.DISTANCE_UNIT] ?: DistanceUnit.KM.name

        UserPreferences(
            themeMode = runCatching { ThemeMode.valueOf(themeModeString) }.getOrDefault(ThemeMode.SYSTEM),
            weightUnit = runCatching { WeightUnit.valueOf(weightUnitString) }.getOrDefault(WeightUnit.KG),
            distanceUnit = runCatching { DistanceUnit.valueOf(distanceUnitString) }.getOrDefault(DistanceUnit.KM),
            defaultRestSeconds = (preferences[PreferencesKeys.DEFAULT_REST_SECONDS] ?: 90).coerceIn(15, 600)
        )
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setWeightUnit(weightUnit: WeightUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = weightUnit.name
        }
    }

    suspend fun setDistanceUnit(distanceUnit: DistanceUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISTANCE_UNIT] = distanceUnit.name
        }
    }

    suspend fun setDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REST_SECONDS] = seconds.coerceIn(15, 600)
        }
    }
}
