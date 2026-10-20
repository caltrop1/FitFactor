package com.sami.setra.data.importexport

import com.sami.setra.data.importexport.model.PortableDay
import com.sami.setra.data.importexport.model.PortableExercise
import com.sami.setra.data.importexport.model.PortableMetadata
import com.sami.setra.data.importexport.model.PortableRoutine
import com.sami.setra.data.importexport.model.PortableSetTemplate
import com.sami.setra.data.importexport.model.PortableWorkout
import com.sami.setra.data.importexport.serializer.RoutineSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PortableRoutineSerializationTest {

    @Test
    fun `cardio distance and duration targets survive serialization`() {
        val routine = PortableRoutine(
            metadata = PortableMetadata(name = "Cardio"),
            days = listOf(
                PortableDay(
                    dayOfWeek = 1,
                    workouts = listOf(
                        PortableWorkout(
                            name = "Run",
                            exercises = listOf(
                                PortableExercise(
                                    exerciseId = "trail_running",
                                    exerciseName = "Trail Running",
                                    sets = listOf(PortableSetTemplate(durationSeconds = 1800, targetDistanceMeters = 5000.0))
                                )
                            )
                        )
                    )
                )
            )
        )

        val restored = RoutineSerializer.deserialize(RoutineSerializer.serialize(routine))
        val set = restored.days.single().workouts.single().exercises.single().sets.single()
        assertEquals(1800, set.durationSeconds)
        assertEquals(5000.0, set.targetDistanceMeters)
    }

    @Test
    fun `serialize and deserialize round trip preserves routine data`() {
        val originalRoutine = PortableRoutine(
            format = "fitfactor-routine",
            formatVersion = 1,
            metadata = PortableMetadata(
                name = "Caltrop's Upper / Lower",
                description = "4 day hypertrophy split",
                creator = "Caltrop",
                createdAt = "2026-10-14T00:00:00Z"
            ),
            days = listOf(
                PortableDay(
                    dayOfWeek = 1,
                    ordering = 0,
                    workouts = listOf(
                        PortableWorkout(
                            name = "Upper Body A",
                            description = "Chest & Back focused",
                            ordering = 0,
                            exercises = listOf(
                                PortableExercise(
                                    exerciseId = "barbell_bench_press",
                                    exerciseName = "Barbell Bench Press",
                                    ordering = 0,
                                    notes = "Focus on leg drive",
                                    sets = listOf(
                                        PortableSetTemplate(
                                            ordering = 0,
                                            setType = "WARMUP",
                                            targetReps = 12.0,
                                            targetWeight = 60.0,
                                            restSeconds = 60,
                                            isWarmup = true
                                        ),
                                        PortableSetTemplate(
                                            ordering = 1,
                                            setType = "NORMAL",
                                            targetReps = 8.0,
                                            targetWeight = 100.0,
                                            restSeconds = 120,
                                            targetRpe = 8.5
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        val jsonString = RoutineSerializer.serialize(originalRoutine)
        assertNotNull(jsonString)

        val deserializedRoutine = RoutineSerializer.deserialize(jsonString)

        assertEquals("fitfactor-routine", deserializedRoutine.format)
        assertEquals(1, deserializedRoutine.formatVersion)
        assertEquals("Caltrop's Upper / Lower", deserializedRoutine.metadata.name)
        assertEquals("4 day hypertrophy split", deserializedRoutine.metadata.description)
        assertEquals("Caltrop", deserializedRoutine.metadata.creator)

        assertEquals(1, deserializedRoutine.days.size)
        val day = deserializedRoutine.days[0]
        assertEquals(1, day.dayOfWeek)
        assertEquals(1, day.workouts.size)

        val workout = day.workouts[0]
        assertEquals("Upper Body A", workout.name)
        assertEquals(1, workout.exercises.size)

        val exercise = workout.exercises[0]
        assertEquals("barbell_bench_press", exercise.exerciseId)
        assertEquals("Barbell Bench Press", exercise.exerciseName)
        assertEquals("Focus on leg drive", exercise.notes)
        assertEquals(2, exercise.sets.size)

        val warmupSet = exercise.sets[0]
        assertEquals("WARMUP", warmupSet.setType)
        assertEquals(12.0, warmupSet.targetReps)
        assertEquals(60.0, warmupSet.targetWeight)

        val workingSet = exercise.sets[1]
        assertEquals("NORMAL", workingSet.setType)
        assertEquals(8.0, workingSet.targetReps)
        assertEquals(100.0, workingSet.targetWeight)
        assertEquals(8.5, workingSet.targetRpe)
    }

    @Test
    fun `deserialize JSON with optional fields omitted sets safe defaults`() {
        val minimalJson = """
            {
              "format": "fitfactor-routine",
              "formatVersion": 1,
              "metadata": {
                "name": "Minimal Routine"
              },
              "days": []
            }
        """.trimIndent()

        val deserialized = RoutineSerializer.deserialize(minimalJson)

        assertEquals("fitfactor-routine", deserialized.format)
        assertEquals(1, deserialized.formatVersion)
        assertEquals("Minimal Routine", deserialized.metadata.name)
        assertEquals("", deserialized.metadata.description)
        assertNull(deserialized.metadata.creator)
        assertEquals(0, deserialized.days.size)
    }
}
