package com.sami.setra.data.local.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sami.setra.data.local.database.entity.SessionExerciseEntity
import com.sami.setra.data.local.database.entity.WorkoutSessionEntity

data class WorkoutSessionWithDetails(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        entity = SessionExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutSessionId"
    )
    val sessionExercises: List<SessionExerciseWithDetails> = emptyList()
) {
    val sortedExercises: List<SessionExerciseWithDetails>
        get() = sessionExercises.sortedBy { it.sessionExercise.ordering }

    val completedExercisesCount: Int
        get() = sessionExercises.count { it.isCompleted }

    val totalExercisesCount: Int
        get() = sessionExercises.size

    val completedSetsCount: Int
        get() = sessionExercises.sumOf { it.completedSetsCount }

    val totalSetsCount: Int
        get() = sessionExercises.sumOf { it.totalSetsCount }
}
