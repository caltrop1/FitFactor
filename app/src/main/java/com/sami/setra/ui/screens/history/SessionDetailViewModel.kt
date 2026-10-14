package com.sami.setra.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    application: Application,
    val sessionId: Long
) : AndroidViewModel(application) {

    private val repository = WorkoutSessionRepository(application)

    val sessionDetails: StateFlow<WorkoutSessionWithDetails?> = repository.getWorkoutSessionById(sessionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun deleteSession(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteWorkoutSession(sessionId)
            onDeleted()
        }
    }

    class Factory(
        private val application: Application,
        private val sessionId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SessionDetailViewModel(application, sessionId) as T
        }
    }
}
