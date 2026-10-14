package com.sami.setra.ui.screens.routines

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.local.database.relation.RoutineWithDays
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.repository.RoutineRepository
import com.sami.setra.data.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineRepository(application)
    private val sessionRepository = WorkoutSessionRepository(application)

    val routines: StateFlow<List<RoutineWithDays>> = repository.getAllActiveRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<WorkoutSessionWithDetails?> = sessionRepository.getActiveWorkoutSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun createNewRoutine(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newRoutineId = repository.createNewRoutine()
            onCreated(newRoutineId)
        }
    }

    fun duplicateRoutine(routineId: Long, onDuplicated: (Long) -> Unit) {
        viewModelScope.launch {
            val newRoutineId = repository.duplicateRoutine(routineId)
            if (newRoutineId > 0) {
                onDuplicated(newRoutineId)
            }
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            repository.deleteRoutine(routineId)
        }
    }
}
