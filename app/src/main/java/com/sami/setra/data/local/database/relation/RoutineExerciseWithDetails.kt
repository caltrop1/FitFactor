package com.sami.setra.data.local.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sami.setra.data.local.database.entity.ExerciseEntity
import com.sami.setra.data.local.database.entity.RoutineExerciseEntity
import com.sami.setra.data.local.database.entity.SetTemplateEntity

data class RoutineExerciseWithDetails(
    @Embedded val routineExercise: RoutineExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineExerciseId"
    )
    val setTemplates: List<SetTemplateEntity> = emptyList()
) {
    val sortedSetTemplates: List<SetTemplateEntity>
        get() = setTemplates.sortedBy { it.ordering }
}
