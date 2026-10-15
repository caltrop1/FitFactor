package com.sami.setra

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.local.database.SetraDatabase
import com.sami.setra.data.local.database.entity.AppMetaEntity
import com.sami.setra.data.local.preferences.DistanceUnit
import com.sami.setra.data.local.preferences.UserPreferences
import com.sami.setra.data.local.preferences.UserPreferencesRepository
import com.sami.setra.data.local.preferences.WeightUnit
import com.sami.setra.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val database = SetraDatabase.getDatabase(application)

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _pendingImportUri = MutableStateFlow<Uri?>(null)
    val pendingImportUri: StateFlow<Uri?> = _pendingImportUri.asStateFlow()

    init {
        // Initialize Room database app metadata
        viewModelScope.launch {
            database.appMetaDao().insertAppMeta(
                AppMetaEntity(
                    installedTimestamp = System.currentTimeMillis(),
                    appVersion = "1.0.0"
                )
            )
        }
    }

    fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val uri: Uri? = when (action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                }
            }
            else -> null
        }
        if (uri != null) {
            _pendingImportUri.value = uri
        }
    }

    fun consumePendingImportUri() {
        _pendingImportUri.value = null
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(themeMode)
        }
    }

    fun setWeightUnit(weightUnit: WeightUnit) {
        viewModelScope.launch {
            userPreferencesRepository.setWeightUnit(weightUnit)
        }
    }

    fun setDistanceUnit(distanceUnit: DistanceUnit) {
        viewModelScope.launch {
            userPreferencesRepository.setDistanceUnit(distanceUnit)
        }
    }
}
