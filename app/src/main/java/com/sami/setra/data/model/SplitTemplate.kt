package com.sami.setra.data.model

data class SplitTemplateDay(
    val dayOfWeek: Int, // 1 = Monday, ..., 7 = Sunday
    val dayName: String, // "Monday", "Tuesday", etc.
    val isRestDay: Boolean,
    val workoutName: String? = null,
    val workoutDescription: String? = null,
    val sampleExercises: List<SplitExerciseTemplate> = emptyList()
)

data class SplitExerciseTemplate(
    val exerciseId: String,
    val displayName: String,
    val sets: String,
    val repRange: String
)

data class SplitTemplate(
    val id: String,
    val name: String,
    val description: String,
    val targetFrequency: String, // e.g. "4 Days / Week"
    val category: String, // e.g. "Hypertrophy & Strength"
    val imageKey: String = "custom",
    val days: List<SplitTemplateDay>
) {
    val activeDaysCount: Int
        get() = days.count { !it.isRestDay }

    val restDaysCount: Int
        get() = 7 - activeDaysCount
}
