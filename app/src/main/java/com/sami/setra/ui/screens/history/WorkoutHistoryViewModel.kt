package com.sami.setra.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter(val label: String, val days: Int) {
    ALL("All Time", Int.MAX_VALUE),
    LAST_30_DAYS("Last 30 Days", 30),
    LAST_7_DAYS("Last 7 Days", 7)
}

class WorkoutHistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = WorkoutSessionRepository(application)

    val selectedFilter = MutableStateFlow(HistoryFilter.ALL)

    val completedSessions: StateFlow<List<WorkoutSessionWithDetails>> = combine(
        repository.getAllCompletedSessions(),
        selectedFilter
    ) { sessions, filter ->
        if (filter == HistoryFilter.ALL) {
            sessions
        } else {
            val cutoff = System.currentTimeMillis() - (filter.days.toLong() * 24 * 3600 * 1000)
            sessions.filter { (it.session.completedAt ?: it.session.startedAt) >= cutoff }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutSession(sessionId)
        }
    }
}
