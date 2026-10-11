package com.sami.setra.ui.screens.routines

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.local.database.entity.RoutineExerciseEntity
import com.sami.setra.data.local.database.entity.SetTemplateEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity
import com.sami.setra.data.local.database.relation.RoutineWithDays
import com.sami.setra.data.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutineDetailViewModel(
    application: Application,
    private val routineId: Long
) : AndroidViewModel(application) {

    private val repository = RoutineRepository(application)

    val routineState: StateFlow<RoutineWithDays?> = repository.getRoutineById(routineId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startWorkout(workoutId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionRepo = com.sami.setra.data.repository.WorkoutSessionRepository(getApplication())
            val sessionId = sessionRepo.startWorkout(workoutId)
            onStarted(sessionId)
        }
    }

    fun updateRoutineHeader(name: String, description: String) {
        viewModelScope.launch {
            repository.updateRoutineHeader(routineId, name, description)
        }
    }

    fun deleteRoutine(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRoutine(routineId)
            onDeleted()
        }
    }

    fun duplicateRoutine(onDuplicated: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = repository.duplicateRoutine(routineId)
            if (newId > 0) {
                onDuplicated(newId)
            }
        }
    }

    fun addWorkout(routineDayId: Long, name: String = "New Workout", description: String = "") {
        viewModelScope.launch {
            repository.addWorkout(routineDayId, name, description)
        }
    }

    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
        }
    }

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(workoutId)
        }
    }

    fun duplicateWorkout(workoutId: Long) {
        viewModelScope.launch {
            repository.duplicateWorkout(workoutId)
        }
    }

    fun moveWorkoutUp(workout: WorkoutEntity, dayWorkouts: List<WorkoutEntity>) {
        val index = dayWorkouts.indexOfFirst { it.id == workout.id }
        if (index > 0) {
            val mutableList = dayWorkouts.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index - 1]
            mutableList[index - 1] = temp
            viewModelScope.launch {
                repository.reorderWorkouts(mutableList)
            }
        }
    }

    fun moveWorkoutDown(workout: WorkoutEntity, dayWorkouts: List<WorkoutEntity>) {
        val index = dayWorkouts.indexOfFirst { it.id == workout.id }
        if (index >= 0 && index < dayWorkouts.size - 1) {
            val mutableList = dayWorkouts.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index + 1]
            mutableList[index + 1] = temp
            viewModelScope.launch {
                repository.reorderWorkouts(mutableList)
            }
        }
    }

    fun addExerciseToWorkout(workoutId: Long, exerciseId: String) {
        viewModelScope.launch {
            repository.addExerciseToWorkout(workoutId, exerciseId)
        }
    }

    fun deleteRoutineExercise(routineExerciseId: Long) {
        viewModelScope.launch {
            repository.deleteRoutineExercise(routineExerciseId)
        }
    }

    fun duplicateRoutineExercise(routineExerciseId: Long) {
        viewModelScope.launch {
            repository.duplicateRoutineExercise(routineExerciseId)
        }
    }

    fun moveExerciseUp(routineExercise: RoutineExerciseEntity, workoutExercises: List<RoutineExerciseEntity>) {
        val index = workoutExercises.indexOfFirst { it.id == routineExercise.id }
        if (index > 0) {
            val mutableList = workoutExercises.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index - 1]
            mutableList[index - 1] = temp
            viewModelScope.launch {
                repository.reorderRoutineExercises(mutableList)
            }
        }
    }

    fun moveExerciseDown(routineExercise: RoutineExerciseEntity, workoutExercises: List<RoutineExerciseEntity>) {
        val index = workoutExercises.indexOfFirst { it.id == routineExercise.id }
        if (index >= 0 && index < workoutExercises.size - 1) {
            val mutableList = workoutExercises.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index + 1]
            mutableList[index + 1] = temp
            viewModelScope.launch {
                repository.reorderRoutineExercises(mutableList)
            }
        }
    }

    fun addSetTemplate(routineExerciseId: Long) {
        viewModelScope.launch {
            repository.addSetTemplate(routineExerciseId)
        }
    }

    fun updateSetTemplate(setTemplate: SetTemplateEntity) {
        viewModelScope.launch {
            repository.updateSetTemplate(setTemplate)
        }
    }

    fun deleteSetTemplate(setTemplateId: Long) {
        viewModelScope.launch {
            repository.deleteSetTemplate(setTemplateId)
        }
    }

    fun moveSetTemplateUp(setTemplate: SetTemplateEntity, exerciseSets: List<SetTemplateEntity>) {
        val index = exerciseSets.indexOfFirst { it.id == setTemplate.id }
        if (index > 0) {
            val mutableList = exerciseSets.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index - 1]
            mutableList[index - 1] = temp
            viewModelScope.launch {
                repository.reorderSetTemplates(mutableList)
            }
        }
    }

    fun moveSetTemplateDown(setTemplate: SetTemplateEntity, exerciseSets: List<SetTemplateEntity>) {
        val index = exerciseSets.indexOfFirst { it.id == setTemplate.id }
        if (index >= 0 && index < exerciseSets.size - 1) {
            val mutableList = exerciseSets.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index + 1]
            mutableList[index + 1] = temp
            viewModelScope.launch {
                repository.reorderSetTemplates(mutableList)
            }
        }
    }

    class Factory(
        private val application: Application,
        private val routineId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoutineDetailViewModel(application, routineId) as T
        }
    }
}
