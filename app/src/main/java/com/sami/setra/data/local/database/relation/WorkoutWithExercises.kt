package com.sami.setra.data.local.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sami.setra.data.local.database.entity.RoutineExerciseEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity

data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = RoutineExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val routineExercises: List<RoutineExerciseWithDetails> = emptyList()
) {
    val sortedExercises: List<RoutineExerciseWithDetails>
        get() = routineExercises.sortedBy { it.routineExercise.ordering }
}
