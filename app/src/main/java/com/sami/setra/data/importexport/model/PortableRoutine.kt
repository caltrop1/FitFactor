package com.sami.setra.data.importexport.model

import kotlinx.serialization.Serializable

@Serializable
data class PortableRoutine(
    val format: String = FORMAT_IDENTIFIER,
    val formatVersion: Int = CURRENT_SCHEMA_VERSION,
    val metadata: PortableMetadata,
    val days: List<PortableDay> = emptyList()
) {
    companion object {
        const val FORMAT_IDENTIFIER = "fitfactor-routine"
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

@Serializable
data class PortableMetadata(
    val name: String,
    val description: String = "",
    val creator: String? = null,
    val createdAt: String? = null
)

@Serializable
data class PortableDay(
    val dayOfWeek: Int, // 1 = Monday, ..., 7 = Sunday
    val ordering: Int = 0,
    val workouts: List<PortableWorkout> = emptyList()
)

@Serializable
data class PortableWorkout(
    val name: String,
    val description: String = "",
    val ordering: Int = 0,
    val exercises: List<PortableExercise> = emptyList()
)

@Serializable
data class PortableExercise(
    val exerciseId: String,
    val exerciseName: String,
    val ordering: Int = 0,
    val notes: String? = null,
    val supersetGroupIndex: Int? = null,
    val sets: List<PortableSetTemplate> = emptyList()
)

@Serializable
data class PortableSetTemplate(
    val ordering: Int = 0,
    val setType: String = "NORMAL", // "NORMAL", "WARMUP", "DROP", "FAILURE"
    val targetReps: Double? = 10.0,
    val targetWeight: Double? = null,
    val durationSeconds: Int? = null,
    val restSeconds: Int? = 90,
    val targetRpe: Double? = null,
    val targetRir: Int? = null,
    val tempo: String? = null,
    val isWarmup: Boolean = false,
    val isDropSet: Boolean = false,
    val isFailureSet: Boolean = false,
    val notes: String? = null
)
