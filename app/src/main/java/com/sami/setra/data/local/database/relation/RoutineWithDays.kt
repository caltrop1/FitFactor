package com.sami.setra.data.local.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sami.setra.data.local.database.entity.RoutineDayEntity
import com.sami.setra.data.local.database.entity.RoutineEntity

data class RoutineWithDays(
    @Embedded val routine: RoutineEntity,
    @Relation(
        entity = RoutineDayEntity::class,
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val routineDays: List<RoutineDayWithWorkouts> = emptyList()
) {
    val activeDaysCount: Int
        get() = routineDays.count { it.workouts.isNotEmpty() }

    val restDaysCount: Int
        get() = 7 - activeDaysCount

    val sortedDays: List<RoutineDayWithWorkouts>
        get() = routineDays.sortedBy { it.routineDay.dayOfWeek }
}
