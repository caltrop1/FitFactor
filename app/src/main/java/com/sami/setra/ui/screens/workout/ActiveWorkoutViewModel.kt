package com.sami.setra.ui.screens.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.repository.WorkoutSessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActiveWorkoutViewModel(
    application: Application,
    val sessionId: Long
) : AndroidViewModel(application) {

    private val repository = WorkoutSessionRepository(application)

    val sessionState: StateFlow<WorkoutSessionWithDetails?> = repository.getWorkoutSessionById(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    private val _previousPerformance = MutableStateFlow<List<CompletedSetEntity>>(emptyList())
    val previousPerformance: StateFlow<List<CompletedSetEntity>> = _previousPerformance.asStateFlow()

    // Timer State
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(0)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(0)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private var timerTargetEndTime: Long = 0L
    private var timerJob: Job? = null

    init {
        // Observe current exercise to fetch previous performance
        viewModelScope.launch {
            sessionState.collect { session ->
                if (session != null && session.sortedExercises.isNotEmpty()) {
                    val idx = _currentExerciseIndex.value.coerceIn(0, session.sortedExercises.size - 1)
                    val currentExercise = session.sortedExercises[idx]
                    val exId = currentExercise.sessionExercise.exerciseId
                    if (!exId.isNullOrBlank()) {
                        val prev = repository.getPreviousPerformanceForExercise(exId)
                        _previousPerformance.value = prev
                    } else {
                        _previousPerformance.value = emptyList()
                    }
                }
            }
        }
    }

    fun selectExerciseIndex(index: Int) {
        val maxIdx = (sessionState.value?.sortedExercises?.size ?: 1) - 1
        _currentExerciseIndex.value = index.coerceIn(0, maxOf(0, maxIdx))
        val session = sessionState.value
        if (session != null && session.sortedExercises.isNotEmpty()) {
            val currentExercise = session.sortedExercises[_currentExerciseIndex.value]
            val exId = currentExercise.sessionExercise.exerciseId
            viewModelScope.launch {
                if (!exId.isNullOrBlank()) {
                    _previousPerformance.value = repository.getPreviousPerformanceForExercise(exId)
                } else {
                    _previousPerformance.value = emptyList()
                }
            }
        }
    }

    fun updateSetActuals(completedSet: CompletedSetEntity, weight: Double?, reps: Double?, durationSeconds: Int?) {
        val updated = completedSet.copy(
            weight = weight,
            reps = reps,
            durationSeconds = durationSeconds
        )
        viewModelScope.launch {
            repository.updateCompletedSet(updated)
        }
    }

    fun toggleSetCompletion(completedSet: CompletedSetEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleSetCompletion(completedSet.id, isCompleted, completedSet)

            // Auto-trigger rest timer if completed and restSeconds configured
            if (isCompleted && (completedSet.restSeconds ?: 0) > 0) {
                startTimer(completedSet.restSeconds!!)
            }
        }
    }

    fun addSetToCurrentExercise() {
        val session = sessionState.value ?: return
        val exercises = session.sortedExercises
        if (exercises.isEmpty()) return
        val currentEx = exercises[_currentExerciseIndex.value]
        viewModelScope.launch {
            repository.addSetToExercise(currentEx.sessionExercise.id, currentEx.totalSetsCount)
        }
    }

    fun removeSetFromExercise(setId: Long) {
        viewModelScope.launch {
            repository.removeSetFromExercise(setId)
        }
    }

    // Timer Controls
    fun startTimer(seconds: Int) {
        timerJob?.cancel()
        _timerTotalSeconds.value = seconds
        _timerRemainingSeconds.value = seconds
        timerTargetEndTime = System.currentTimeMillis() + seconds * 1000L
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                val now = System.currentTimeMillis()
                val remainingMillis = timerTargetEndTime - now
                if (remainingMillis <= 0) {
                    _timerRemainingSeconds.value = 0
                    _isTimerRunning.value = false
                    break
                } else {
                    _timerRemainingSeconds.value = ((remainingMillis + 999) / 1000).toInt()
                }
                delay(200)
            }
        }
    }

    fun pauseTimer() {
        if (!_isTimerRunning.value) return
        timerJob?.cancel()
        _isTimerRunning.value = false
        val now = System.currentTimeMillis()
        val remainingMillis = maxOf(0L, timerTargetEndTime - now)
        _timerRemainingSeconds.value = ((remainingMillis + 999) / 1000).toInt()
    }

    fun resumeTimer() {
        if (_isTimerRunning.value || _timerRemainingSeconds.value <= 0) return
        startTimer(_timerRemainingSeconds.value)
    }

    fun skipTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _timerRemainingSeconds.value = 0
    }

    fun adjustTimer(deltaSeconds: Int) {
        val newRemaining = maxOf(0, _timerRemainingSeconds.value + deltaSeconds)
        if (newRemaining <= 0) {
            skipTimer()
        } else {
            _timerRemainingSeconds.value = newRemaining
            _timerTotalSeconds.value = maxOf(_timerTotalSeconds.value, newRemaining)
            timerTargetEndTime += deltaSeconds * 1000L
        }
    }

    fun finishWorkout(notes: String? = null, onFinished: () -> Unit) {
        viewModelScope.launch {
            skipTimer()
            repository.finishWorkoutSession(sessionId, notes)
            onFinished()
        }
    }

    fun cancelWorkout(onCancelled: () -> Unit) {
        viewModelScope.launch {
            skipTimer()
            repository.cancelWorkoutSession(sessionId)
            onCancelled()
        }
    }

    class Factory(
        private val application: Application,
        private val sessionId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActiveWorkoutViewModel(application, sessionId) as T
        }
    }
}
