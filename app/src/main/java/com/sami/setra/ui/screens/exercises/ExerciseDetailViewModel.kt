package com.sami.setra.ui.screens.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.local.database.relation.ExerciseWithDetails
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.model.ExerciseProgressionPoint
import com.sami.setra.data.model.PersonalRecord
import com.sami.setra.data.repository.ExerciseRepository
import com.sami.setra.data.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ExerciseRepository(application)
    private val sessionRepository = WorkoutSessionRepository(application)

    private val _exerciseDetails = MutableStateFlow<ExerciseWithDetails?>(null)
    val exerciseDetails: StateFlow<ExerciseWithDetails?> = _exerciseDetails.asStateFlow()

    private val _progressionPoints = MutableStateFlow<List<ExerciseProgressionPoint>>(emptyList())
    val progressionPoints: StateFlow<List<ExerciseProgressionPoint>> = _progressionPoints.asStateFlow()

    private val _personalRecords = MutableStateFlow<List<PersonalRecord>>(emptyList())
    val personalRecords: StateFlow<List<PersonalRecord>> = _personalRecords.asStateFlow()

    private val _completedSessions = MutableStateFlow<List<WorkoutSessionWithDetails>>(emptyList())
    val completedSessions: StateFlow<List<WorkoutSessionWithDetails>> = _completedSessions.asStateFlow()

    fun loadExercise(exerciseId: String) {
        viewModelScope.launch {
            repository.getExerciseById(exerciseId).collect { details ->
                _exerciseDetails.value = details
            }
        }
        viewModelScope.launch {
            sessionRepository.getExerciseProgression(exerciseId).collect { points ->
                _progressionPoints.value = points
            }
        }
        viewModelScope.launch {
            sessionRepository.getPersonalRecords(exerciseId).collect { prs ->
                _personalRecords.value = prs
            }
        }
        viewModelScope.launch {
            sessionRepository.getCompletedSessionsForExercise(exerciseId).collect { sessions ->
                _completedSessions.value = sessions
            }
        }
    }
}
