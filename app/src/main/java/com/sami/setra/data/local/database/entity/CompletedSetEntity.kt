package com.sami.setra.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completed_set",
    foreignKeys = [
        ForeignKey(
            entity = SessionExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionExerciseId"]),
        Index(value = ["isCompleted"])
    ]
)
data class CompletedSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionExerciseId: Long,
    val ordering: Int = 0,
    val setType: String = "NORMAL", // "NORMAL", "WARMUP", "DROP", "FAILURE"
    val targetWeight: Double? = null,
    val targetReps: Double? = null,
    val targetDurationSeconds: Int? = null,
    val restSeconds: Int? = 90,
    val weight: Double? = null,
    val reps: Double? = null,
    val durationSeconds: Int? = null,
    val rpe: Double? = null,
    val rir: Int? = null,
    val tempo: String? = null,
    val isWarmup: Boolean = false,
    val isDropSet: Boolean = false,
    val isFailureSet: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String? = null
)
