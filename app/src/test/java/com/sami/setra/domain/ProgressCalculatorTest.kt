package com.sami.setra.domain

import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.entity.SessionExerciseEntity
import com.sami.setra.data.local.database.entity.WorkoutSessionEntity
import com.sami.setra.data.local.database.relation.SessionExerciseWithDetails
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.model.RecordType
import com.sami.setra.data.model.TimeFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun calculatePersonalRecords_correctlyIdentifiesHeaviestAndMaxRepsAndExcludesWarmups() {
        val now = System.currentTimeMillis()

        // Warmup set 40kg x 10, Working set 1: 80kg x 10, Working set 2: 100kg x 5
        val warmupSet = CompletedSetEntity(id = 1, sessionExerciseId = 10, ordering = 0, isWarmup = true, setType = "WARMUP", weight = 40.0, reps = 10.0, isCompleted = true)
        val workingSet1 = CompletedSetEntity(id = 2, sessionExerciseId = 10, ordering = 1, isWarmup = false, setType = "NORMAL", weight = 80.0, reps = 10.0, isCompleted = true)
        val workingSet2 = CompletedSetEntity(id = 3, sessionExerciseId = 10, ordering = 2, isWarmup = false, setType = "NORMAL", weight = 100.0, reps = 5.0, isCompleted = true)
        val incompleteSet = CompletedSetEntity(id = 4, sessionExerciseId = 10, ordering = 3, isWarmup = false, setType = "NORMAL", weight = 120.0, reps = 5.0, isCompleted = false)

        val sessionEx = SessionExerciseWithDetails(
            sessionExercise = SessionExerciseEntity(id = 10, workoutSessionId = 1, exerciseId = "bench_press", exerciseNameSnapshot = "Bench Press", ordering = 0),
            exercise = null,
            completedSets = listOf(warmupSet, workingSet1, workingSet2, incompleteSet)
        )

        val completedSession = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 1, workoutName = "Chest Day", startedAt = now - 3600000, completedAt = now, status = "COMPLETED"),
            sessionExercises = listOf(sessionEx)
        )

        val records = ProgressCalculator.calculatePersonalRecords(listOf(completedSession))

        val heaviestRecord = records.find { it.recordType == RecordType.HEAVIEST_WEIGHT }
        assertNotNull(heaviestRecord)
        assertEquals(100.0, heaviestRecord!!.value, 0.001)

        val maxRepsRecord = records.find { it.recordType == RecordType.MAX_REPS }
        assertNotNull(maxRepsRecord)
        assertEquals(10.0, maxRepsRecord!!.value, 0.001)

        val e1rmRecord = records.find { it.recordType == RecordType.ESTIMATED_1RM }
        assertNotNull(e1rmRecord)
        // Working set 1: 80 * (36 / 27) = 106.67; Working set 2: 100 * (36 / 32) = 112.5
        assertEquals(112.5, e1rmRecord!!.value, 0.001)
    }

    @Test
    fun calculatePersonalRecords_excludesInProgressAndCancelledSessions() {
        val now = System.currentTimeMillis()
        val set = CompletedSetEntity(id = 1, sessionExerciseId = 10, ordering = 0, isCompleted = true, weight = 200.0, reps = 10.0)

        val sessionEx = SessionExerciseWithDetails(
            sessionExercise = SessionExerciseEntity(id = 10, workoutSessionId = 1, exerciseId = "deadlift", exerciseNameSnapshot = "Deadlift", ordering = 0),
            exercise = null,
            completedSets = listOf(set)
        )

        val inProgressSession = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 1, workoutName = "Legs", startedAt = now, status = "IN_PROGRESS"),
            sessionExercises = listOf(sessionEx)
        )

        val records = ProgressCalculator.calculatePersonalRecords(listOf(inProgressSession))
        assertTrue(records.isEmpty())
    }

    @Test
    fun calculateWorkoutFrequency_computesCorrectAggregates() {
        val now = System.currentTimeMillis()

        val set1 = CompletedSetEntity(id = 1, sessionExerciseId = 1, ordering = 0, isCompleted = true, weight = 50.0, reps = 10.0) // 500 kg
        val set2 = CompletedSetEntity(id = 2, sessionExerciseId = 1, ordering = 1, isCompleted = true, weight = 60.0, reps = 10.0) // 600 kg

        val sessionEx = SessionExerciseWithDetails(
            sessionExercise = SessionExerciseEntity(id = 1, workoutSessionId = 1, exerciseId = "ex1", exerciseNameSnapshot = "Ex 1", ordering = 0),
            exercise = null,
            completedSets = listOf(set1, set2)
        )

        val session1 = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 1, workoutName = "Push", startedAt = now - 86400000, completedAt = now - 82800000, status = "COMPLETED"),
            sessionExercises = listOf(sessionEx)
        )

        val frequencyStats = ProgressCalculator.calculateWorkoutFrequency(listOf(session1), nowMillis = now)

        assertEquals(1, frequencyStats.totalCompletedWorkouts)
        assertEquals(1, frequencyStats.workoutsThisWeek)
        assertEquals(1100.0, frequencyStats.totalVolumeKg, 0.001)
        assertEquals(2, frequencyStats.totalCompletedSets)
        assertEquals(3600L, frequencyStats.totalDurationSeconds)
    }

    @Test
    fun calculateExerciseProgression_ordersPointsChronologicallyAndSelectsBestSet() {
        val now = System.currentTimeMillis()

        // Session 1 (2 days ago): 60kg x 10
        val s1Set = CompletedSetEntity(id = 1, sessionExerciseId = 1, ordering = 0, isCompleted = true, weight = 60.0, reps = 10.0)
        val s1Ex = SessionExerciseWithDetails(
            sessionExercise = SessionExerciseEntity(id = 1, workoutSessionId = 1, exerciseId = "squat", exerciseNameSnapshot = "Squat", ordering = 0),
            exercise = null,
            completedSets = listOf(s1Set)
        )
        val session1 = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 1, workoutName = "Leg Day 1", startedAt = now - 172800000, completedAt = now - 169200000, status = "COMPLETED"),
            sessionExercises = listOf(s1Ex)
        )

        // Session 2 (1 day ago): 70kg x 8
        val s2Set = CompletedSetEntity(id = 2, sessionExerciseId = 2, ordering = 0, isCompleted = true, weight = 70.0, reps = 8.0)
        val s2Ex = SessionExerciseWithDetails(
            sessionExercise = SessionExerciseEntity(id = 2, workoutSessionId = 2, exerciseId = "squat", exerciseNameSnapshot = "Squat", ordering = 0),
            exercise = null,
            completedSets = listOf(s2Set)
        )
        val session2 = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 2, workoutName = "Leg Day 2", startedAt = now - 86400000, completedAt = now - 82800000, status = "COMPLETED"),
            sessionExercises = listOf(s2Ex)
        )

        val progression = ProgressCalculator.calculateExerciseProgression(listOf(session2, session1), exerciseId = "squat")

        assertEquals(2, progression.size)
        // Session 1 is oldest, so should appear first
        assertEquals(1L, progression[0].sessionId)
        assertEquals(60.0, progression[0].bestWeight, 0.001)

        assertEquals(2L, progression[1].sessionId)
        assertEquals(70.0, progression[1].bestWeight, 0.001)
    }
}
