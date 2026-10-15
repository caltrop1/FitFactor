package com.sami.setra.data.importexport.validator

import com.sami.setra.data.importexport.model.PortableRoutine

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reason: String) : ValidationResult
}

object RoutineSchemaValidator {

    private const val MAX_DAYS = 14
    private const val MAX_WORKOUTS_PER_DAY = 10
    private const val MAX_EXERCISES_PER_WORKOUT = 50
    private const val MAX_SETS_PER_EXERCISE = 30

    fun validate(routine: PortableRoutine): ValidationResult {
        if (routine.format != PortableRoutine.FORMAT_IDENTIFIER) {
            return ValidationResult.Invalid("Invalid format identifier: '${routine.format}'. Expected '${PortableRoutine.FORMAT_IDENTIFIER}'.")
        }

        if (routine.formatVersion > PortableRoutine.CURRENT_SCHEMA_VERSION) {
            return ValidationResult.Invalid("Unsupported format version: ${routine.formatVersion}.")
        }

        if (routine.metadata.name.isBlank()) {
            return ValidationResult.Invalid("Routine name cannot be blank.")
        }

        if (routine.days.size > MAX_DAYS) {
            return ValidationResult.Invalid("Exceeded maximum allowed routine days (${routine.days.size} > $MAX_DAYS).")
        }

        for (day in routine.days) {
            if (day.dayOfWeek !in 1..7) {
                return ValidationResult.Invalid("Invalid day of week '${day.dayOfWeek}'. Day of week must be between 1 and 7.")
            }

            if (day.workouts.size > MAX_WORKOUTS_PER_DAY) {
                return ValidationResult.Invalid("Exceeded maximum workouts per day (${day.workouts.size} > $MAX_WORKOUTS_PER_DAY).")
            }

            for (workout in day.workouts) {
                if (workout.exercises.size > MAX_EXERCISES_PER_WORKOUT) {
                    return ValidationResult.Invalid("Exceeded maximum exercises per workout (${workout.exercises.size} > $MAX_EXERCISES_PER_WORKOUT).")
                }

                for (exercise in workout.exercises) {
                    if (exercise.sets.size > MAX_SETS_PER_EXERCISE) {
                        return ValidationResult.Invalid("Exceeded maximum sets per exercise (${exercise.sets.size} > $MAX_SETS_PER_EXERCISE).")
                    }
                }
            }
        }

        return ValidationResult.Valid
    }
}
