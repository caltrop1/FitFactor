package com.sami.setra.data.model

/** Metrics inferred from the offline exercise database's category, equipment and mechanics. */
data class ExerciseTracking(
    val repetitions: Boolean,
    val externalResistance: Boolean,
    val duration: Boolean,
    val distance: Boolean
)

fun exerciseTracking(category: String?, equipment: String?, mechanic: String?): ExerciseTracking {
    val normalizedCategory = category.orEmpty().trim().lowercase()
    val normalizedEquipment = equipment.orEmpty().trim().lowercase()
    val normalizedMechanic = mechanic.orEmpty().trim().lowercase()

    if (normalizedCategory == "cardio") {
        return ExerciseTracking(repetitions = false, externalResistance = false, duration = true, distance = true)
    }
    if (normalizedCategory == "stretching" || normalizedMechanic == "static") {
        return ExerciseTracking(repetitions = false, externalResistance = false, duration = true, distance = false)
    }

    val resistanceCategory = normalizedCategory in setOf("strength", "powerlifting", "olympic weightlifting", "strongman")
    val hasExternalEquipment = normalizedEquipment.isNotBlank() && normalizedEquipment != "body only"
    return ExerciseTracking(
        repetitions = true,
        externalResistance = resistanceCategory && hasExternalEquipment,
        duration = false,
        distance = false
    )
}