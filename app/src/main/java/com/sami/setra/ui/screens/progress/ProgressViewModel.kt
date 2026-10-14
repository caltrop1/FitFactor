package com.sami.setra.ui.screens.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.model.PersonalRecord
import com.sami.setra.data.model.WorkoutFrequencyStats
import com.sami.setra.data.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProgressViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = WorkoutSessionRepository(application)

    val frequencyStats: StateFlow<WorkoutFrequencyStats?> = repository.getWorkoutFrequencyStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val personalRecords: StateFlow<List<PersonalRecord>> = repository.getPersonalRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentCompletedSessions: StateFlow<List<WorkoutSessionWithDetails>> = repository.getAllCompletedSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
