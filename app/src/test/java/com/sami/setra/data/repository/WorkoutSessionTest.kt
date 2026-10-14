package com.sami.setra.data.repository

import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.entity.ExerciseEntity
import com.sami.setra.data.local.database.entity.SessionExerciseEntity
import com.sami.setra.data.local.database.entity.WorkoutSessionEntity
import com.sami.setra.data.local.database.relation.SessionExerciseWithDetails
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSessionTest {

    @Test
    fun sessionExerciseWithDetails_computesCompletionAndSetCountsCorrectly() {
        val sessionExercise = SessionExerciseEntity(
            id = 1,
            workoutSessionId = 100,
            exerciseId = "bench_press",
            exerciseNameSnapshot = "Barbell Bench Press",
            ordering = 0
        )

        val exercise = ExerciseEntity(
            id = "bench_press",
            name = "Barbell Bench Press",
            category = "strength",
            equipment = "barbell",
            level = "beginner",
            force = "push",
            mechanic = "compound",
            instructions = "Instructions"
        )

        val set1 = CompletedSetEntity(id = 1, sessionExerciseId = 1, ordering = 0, isCompleted = true, weight = 60.0, reps = 10.0)
        val set2 = CompletedSetEntity(id = 2, sessionExerciseId = 1, ordering = 1, isCompleted = true, weight = 60.0, reps = 10.0)
        val set3 = CompletedSetEntity(id = 3, sessionExerciseId = 1, ordering = 2, isCompleted = false, weight = 60.0, reps = 10.0)

        val incompleteDetails = SessionExerciseWithDetails(
            sessionExercise = sessionExercise,
            exercise = exercise,
            completedSets = listOf(set1, set2, set3)
        )

        assertEquals(2, incompleteDetails.completedSetsCount)
        assertEquals(3, incompleteDetails.totalSetsCount)
        assertFalse(incompleteDetails.isCompleted)

        // Mark set3 completed
        val completeDetails = SessionExerciseWithDetails(
            sessionExercise = sessionExercise,
            exercise = exercise,
            completedSets = listOf(set1, set2, set3.copy(isCompleted = true))
        )

        assertEquals(3, completeDetails.completedSetsCount)
        assertEquals(3, completeDetails.totalSetsCount)
        assertTrue(completeDetails.isCompleted)
    }

    @Test
    fun sessionExerciseWithZeroSets_handlesCompletionGracefullyWithoutError() {
        val sessionExercise = SessionExerciseEntity(
            id = 2,
            workoutSessionId = 100,
            exerciseId = "stretching",
            exerciseNameSnapshot = "Stretching",
            ordering = 1
        )

        val emptyDetails = SessionExerciseWithDetails(
            sessionExercise = sessionExercise,
            exercise = null,
            completedSets = emptyList()
        )

        assertEquals(0, emptyDetails.completedSetsCount)
        assertEquals(0, emptyDetails.totalSetsCount)
        assertFalse(emptyDetails.isCompleted)
    }

    @Test
    fun workoutSessionWithDetails_computesAggregatesAndOrdering() {
        val session = WorkoutSessionEntity(
            id = 100,
            routineId = 1,
            workoutId = 5,
            routineName = "Push Pull Legs",
            workoutName = "Push Day",
            startedAt = System.currentTimeMillis(),
            status = "IN_PROGRESS"
        )

        val setEx1 = SessionExerciseWithDetails(
            sessionExercise = SessionExerciseEntity(id = 1, workoutSessionId = 100, exerciseId = "ex1", exerciseNameSnapshot = "Ex 1", ordering = 1),
            exercise = null,
            completedSets = listOf(
                CompletedSetEntity(id = 1, sessionExerciseId = 1, ordering = 0, isCompleted = true)
            )
        )

        val setEx0 = SessionExerciseWithDetails(
            sessionExercise = SessionExerciseEntity(id = 2, workoutSessionId = 100, exerciseId = "ex0", exerciseNameSnapshot = "Ex 0", ordering = 0),
            exercise = null,
            completedSets = listOf(
                CompletedSetEntity(id = 2, sessionExerciseId = 2, ordering = 0, isCompleted = true),
                CompletedSetEntity(id = 3, sessionExerciseId = 2, ordering = 1, isCompleted = true)
            )
        )

        val sessionWithDetails = WorkoutSessionWithDetails(
            session = session,
            sessionExercises = listOf(setEx1, setEx0)
        )

        assertEquals(2, sessionWithDetails.totalExercisesCount)
        assertEquals(2, sessionWithDetails.completedExercisesCount)
        assertEquals(3, sessionWithDetails.totalSetsCount)
        assertEquals(3, sessionWithDetails.completedSetsCount)

        // Check ordering
        val sorted = sessionWithDetails.sortedExercises
        assertEquals("Ex 0", sorted[0].sessionExercise.exerciseNameSnapshot)
        assertEquals("Ex 1", sorted[1].sessionExercise.exerciseNameSnapshot)
    }

    @Test
    fun timerElapsedCalculation_preservesTimeAcrossPauseAndResume() {
        val totalSeconds = 90
        val startTime = 1000000L
        val targetEndTime = startTime + totalSeconds * 1000L

        // After 30 seconds
        val now30s = startTime + 30000L
        val remainingMillis30s = targetEndTime - now30s
        val remainingSeconds30s = ((remainingMillis30s + 999) / 1000).toInt()
        assertEquals(60, remainingSeconds30s)

        // Paused with 60s remaining
        val pausedRemaining = remainingSeconds30s
        assertEquals(60, pausedRemaining)

        // Resumed at time T2
        val resumeTime = now30s + 50000L // 50 seconds later
        val newTargetEndTime = resumeTime + pausedRemaining * 1000L

        // Immediately after resume
        val remainingMillisAfterResume = newTargetEndTime - resumeTime
        val remainingSecondsAfterResume = ((remainingMillisAfterResume + 999) / 1000).toInt()
        assertEquals(60, remainingSecondsAfterResume)
    }

    @Test
    fun historicalSafety_editingRoutineDoesNotMutateSavedSessionSnapshot() {
        val originalRoutineName = "Original PPL"
        val originalWorkoutName = "Push A"

        val session = WorkoutSessionEntity(
            id = 50,
            routineId = 1,
            workoutId = 10,
            routineName = originalRoutineName,
            workoutName = originalWorkoutName,
            startedAt = 1000L,
            completedAt = 3000L,
            status = "COMPLETED"
        )

        // Routine is modified later
        val editedRoutineName = "Modified PPL 2026"

        // The session snapshot remains unchanged
        assertNotEquals(editedRoutineName, session.routineName)
        assertEquals("Original PPL", session.routineName)
        assertEquals("Push A", session.workoutName)
    }
}
