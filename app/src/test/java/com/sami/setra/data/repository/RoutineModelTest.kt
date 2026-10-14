package com.sami.setra.data.repository

import com.sami.setra.data.local.database.entity.ExerciseEntity
import com.sami.setra.data.local.database.entity.RoutineDayEntity
import com.sami.setra.data.local.database.entity.RoutineEntity
import com.sami.setra.data.local.database.entity.RoutineExerciseEntity
import com.sami.setra.data.local.database.entity.SetTemplateEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity
import com.sami.setra.data.local.database.relation.RoutineDayWithWorkouts
import com.sami.setra.data.local.database.relation.RoutineExerciseWithDetails
import com.sami.setra.data.local.database.relation.RoutineWithDays
import com.sami.setra.data.local.database.relation.WorkoutWithExercises
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineModelTest {

    @Test
    fun routineWithDays_computesActiveAndRestDaysAndSortsDays() {
        val routine = RoutineEntity(id = 1, name = "Test Routine", description = "Test Desc")

        val day3Rest = RoutineDayWithWorkouts(
            routineDay = RoutineDayEntity(id = 3, routineId = 1, dayOfWeek = 3, ordering = 2),
            workouts = emptyList()
        )

        val workout1 = WorkoutWithExercises(
            workout = WorkoutEntity(id = 10, routineDayId = 1, name = "Upper Body", ordering = 0)
        )

        val day1Active = RoutineDayWithWorkouts(
            routineDay = RoutineDayEntity(id = 1, routineId = 1, dayOfWeek = 1, ordering = 0),
            workouts = listOf(workout1)
        )

        val day2Rest = RoutineDayWithWorkouts(
            routineDay = RoutineDayEntity(id = 2, routineId = 1, dayOfWeek = 2, ordering = 1),
            workouts = emptyList()
        )

        val routineWithDays = RoutineWithDays(
            routine = routine,
            routineDays = listOf(day3Rest, day1Active, day2Rest)
        )

        assertEquals(1, routineWithDays.activeDaysCount)
        assertEquals(6, routineWithDays.restDaysCount)

        val sortedDays = routineWithDays.sortedDays
        assertEquals(3, sortedDays.size)
        assertEquals(1, sortedDays[0].routineDay.dayOfWeek)
        assertEquals(2, sortedDays[1].routineDay.dayOfWeek)
        assertEquals(3, sortedDays[2].routineDay.dayOfWeek)
    }

    @Test
    fun multipleWorkoutsOnSameDay_areSupportedAndSortedByOrdering() {
        val workoutB = WorkoutWithExercises(
            workout = WorkoutEntity(id = 101, routineDayId = 5, name = "Evening Cardio", ordering = 1)
        )
        val workoutA = WorkoutWithExercises(
            workout = WorkoutEntity(id = 100, routineDayId = 5, name = "Morning Strength", ordering = 0)
        )

        val dayWithWorkouts = RoutineDayWithWorkouts(
            routineDay = RoutineDayEntity(id = 5, routineId = 1, dayOfWeek = 1, ordering = 0),
            workouts = listOf(workoutB, workoutA)
        )

        assertEquals(2, dayWithWorkouts.workouts.size)
        val sortedWorkouts = dayWithWorkouts.sortedWorkouts
        assertEquals("Morning Strength", sortedWorkouts[0].workout.name)
        assertEquals("Evening Cardio", sortedWorkouts[1].workout.name)
    }

    @Test
    fun workoutWithExercises_sortsExercisesAndSetTemplates() {
        val set2 = SetTemplateEntity(id = 2, routineExerciseId = 20, ordering = 1, targetReps = 8.0, targetWeight = 65.0)
        val set1 = SetTemplateEntity(id = 1, routineExerciseId = 20, ordering = 0, targetReps = 10.0, targetWeight = 60.0)

        val exerciseDetails = RoutineExerciseWithDetails(
            routineExercise = RoutineExerciseEntity(id = 20, workoutId = 10, exerciseId = "bench_press", ordering = 0),
            exercise = ExerciseEntity(id = "bench_press", name = "Bench Press", category = "chest", equipment = "barbell", level = "beginner", force = "push", mechanic = "compound", instructions = ""),
            setTemplates = listOf(set2, set1)
        )

        val sortedSets = exerciseDetails.sortedSetTemplates
        assertEquals(2, sortedSets.size)
        assertEquals(1, sortedSets[0].id)
        assertEquals(10.0, sortedSets[0].targetReps!!, 0.001)
        assertEquals(2, sortedSets[1].id)
        assertEquals(8.0, sortedSets[1].targetReps!!, 0.001)
    }

    @Test
    fun duplication_createsIndependentCopies() {
        val originalRoutine = RoutineEntity(id = 10, name = "PPL Routine", description = "Original")
        val duplicatedRoutine = originalRoutine.copy(
            id = 20,
            name = "${originalRoutine.name} (Copy)",
            updatedAt = System.currentTimeMillis()
        )

        assertNotEquals(originalRoutine.id, duplicatedRoutine.id)
        assertEquals("PPL Routine", originalRoutine.name)
        assertEquals("PPL Routine (Copy)", duplicatedRoutine.name)

        val originalWorkout = WorkoutEntity(id = 100, routineDayId = 1, name = "Push A", ordering = 0)
        val duplicatedWorkout = originalWorkout.copy(
            id = 200,
            name = "${originalWorkout.name} (Copy)",
            ordering = 1
        )

        assertNotEquals(originalWorkout.id, duplicatedWorkout.id)
        assertEquals("Push A", originalWorkout.name)
        assertEquals("Push A (Copy)", duplicatedWorkout.name)
    }

    @Test
    fun splitTemplateRepository_integration_providesEditableTemplates() {
        val repo = SplitTemplateRepository()
        val ppl = repo.getSplitById("push_pull_legs")
        assertNotNull(ppl)
        assertEquals("Push / Pull / Legs", ppl!!.name)
        assertEquals(7, ppl.days.size)

        // Verify custom template exists
        val custom = repo.getSplitById("custom")
        assertNotNull(custom)
        assertEquals("Custom Split", custom!!.name)
    }
}
