package com.sami.setra.data.local.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sami.setra.data.local.database.entity.RoutineDayEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity

data class RoutineDayWithWorkouts(
    @Embedded val routineDay: RoutineDayEntity,
    @Relation(
        entity = WorkoutEntity::class,
        parentColumn = "id",
        entityColumn = "routineDayId"
    )
    val workouts: List<WorkoutWithExercises> = emptyList()
) {
    val sortedWorkouts: List<WorkoutWithExercises>
        get() = workouts.sortedBy { it.workout.ordering }
}
