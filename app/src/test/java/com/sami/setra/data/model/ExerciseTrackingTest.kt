package com.sami.setra.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseTrackingTest {
    @Test
    fun cardioUsesDistanceAndDurationWithoutStrengthMetrics() {
        assertEquals(
            ExerciseTracking(repetitions = false, externalResistance = false, duration = true, distance = true),
            exerciseTracking("cardio", null, null)
        )
    }

    @Test
    fun barbellStrengthUsesRepsAndExternalResistance() {
        assertEquals(
            ExerciseTracking(repetitions = true, externalResistance = true, duration = false, distance = false),
            exerciseTracking("strength", "barbell", "compound")
        )
    }

    @Test
    fun bodyweightStrengthUsesRepsButNoExternalResistance() {
        assertEquals(
            ExerciseTracking(repetitions = true, externalResistance = false, duration = false, distance = false),
            exerciseTracking("strength", "body only", "compound")
        )
    }

    @Test
    fun staticAndStretchingMovementsUseDuration() {
        assertEquals(
            ExerciseTracking(repetitions = false, externalResistance = false, duration = true, distance = false),
            exerciseTracking("strength", "body only", "static")
        )
        assertEquals(
            ExerciseTracking(repetitions = false, externalResistance = false, duration = true, distance = false),
            exerciseTracking("stretching", "other", "isolation")
        )
    }
}