package com.sami.setra.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_session",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["routineId"]),
        Index(value = ["workoutId"]),
        Index(value = ["status"]),
        Index(value = ["startedAt"])
    ]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long? = null,
    val workoutId: Long? = null,
    val routineName: String = "",
    val workoutName: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val status: String = "IN_PROGRESS", // "IN_PROGRESS", "COMPLETED", "CANCELLED"
    val notes: String? = null
)
