package com.sami.setra.data.local.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.entity.ExerciseEntity
import com.sami.setra.data.local.database.entity.SessionExerciseEntity

data class SessionExerciseWithDetails(
    @Embedded val sessionExercise: SessionExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionExerciseId"
    )
    val completedSets: List<CompletedSetEntity> = emptyList()
) {
    val sortedCompletedSets: List<CompletedSetEntity>
        get() = completedSets.sortedBy { it.ordering }

    val isCompleted: Boolean
        get() = completedSets.isNotEmpty() && completedSets.all { it.isCompleted }

    val completedSetsCount: Int
        get() = completedSets.count { it.isCompleted }

    val totalSetsCount: Int
        get() = completedSets.size
}
