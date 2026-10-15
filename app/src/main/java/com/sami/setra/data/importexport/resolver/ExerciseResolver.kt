package com.sami.setra.data.importexport.resolver

import com.sami.setra.data.importexport.model.PortableRoutine
import com.sami.setra.data.local.database.dao.ExerciseDao
import com.sami.setra.data.local.database.entity.ExerciseEntity

sealed interface ExerciseMatchStatus {
    data class ExactMatch(val exercise: ExerciseEntity) : ExerciseMatchStatus
    data class NameMatch(val exercise: ExerciseEntity) : ExerciseMatchStatus
    data class Unresolved(val exerciseId: String, val exerciseName: String) : ExerciseMatchStatus
}

data class ExerciseResolutionItem(
    val originalExerciseId: String,
    val originalExerciseName: String,
    val initialStatus: ExerciseMatchStatus,
    val mappedExerciseId: String? = null,
    val isSkipped: Boolean = false
) {
    val isResolved: Boolean
        get() = isSkipped || mappedExerciseId != null
}

data class ExerciseResolutionSummary(
    val items: List<ExerciseResolutionItem>
) {
    val unresolvedItems: List<ExerciseResolutionItem>
        get() = items.filter { !it.isResolved }

    val hasUnresolved: Boolean
        get() = unresolvedItems.isNotEmpty()

    val totalExercisesCount: Int
        get() = items.size

    val matchedCount: Int
        get() = items.count { it.initialStatus is ExerciseMatchStatus.ExactMatch || it.initialStatus is ExerciseMatchStatus.NameMatch }

    val missingCount: Int
        get() = items.count { it.initialStatus is ExerciseMatchStatus.Unresolved }
}

class ExerciseResolver(
    private val exerciseDao: ExerciseDao
) {

    suspend fun resolveExercises(routine: PortableRoutine): ExerciseResolutionSummary {
        val uniqueExercisesMap = mutableMapOf<String, String>() // id -> name
        routine.days.forEach { day ->
            day.workouts.forEach { workout ->
                workout.exercises.forEach { exercise ->
                    if (!uniqueExercisesMap.containsKey(exercise.exerciseId)) {
                        uniqueExercisesMap[exercise.exerciseId] = exercise.exerciseName
                    }
                }
            }
        }

        val allLocalExercises = exerciseDao.getAllExercisesSync()
        val localById = allLocalExercises.associateBy { it.id }
        val localByName = allLocalExercises.associateBy { it.name.lowercase().trim() }

        val items = uniqueExercisesMap.map { (exId, exName) ->
            val foundExactEntity = localById[exId]
            if (foundExactEntity != null) {
                ExerciseResolutionItem(
                    originalExerciseId = exId,
                    originalExerciseName = exName,
                    initialStatus = ExerciseMatchStatus.ExactMatch(foundExactEntity),
                    mappedExerciseId = foundExactEntity.id
                )
            } else {
                val foundNameEntity = localByName[exName.lowercase().trim()]
                if (foundNameEntity != null) {
                    ExerciseResolutionItem(
                        originalExerciseId = exId,
                        originalExerciseName = exName,
                        initialStatus = ExerciseMatchStatus.NameMatch(foundNameEntity),
                        mappedExerciseId = foundNameEntity.id
                    )
                } else {
                    ExerciseResolutionItem(
                        originalExerciseId = exId,
                        originalExerciseName = exName,
                        initialStatus = ExerciseMatchStatus.Unresolved(exId, exName),
                        mappedExerciseId = null
                    )
                }
            }
        }

        return ExerciseResolutionSummary(items)
    }
}
