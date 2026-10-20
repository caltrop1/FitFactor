package com.sami.setra.debug

import android.content.Context
import androidx.room.withTransaction
import com.sami.setra.data.local.database.SetraDatabase
import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.entity.RoutineDayEntity
import com.sami.setra.data.local.database.entity.RoutineEntity
import com.sami.setra.data.local.database.entity.RoutineExerciseEntity
import com.sami.setra.data.local.database.entity.SessionExerciseEntity
import com.sami.setra.data.local.database.entity.SetTemplateEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity
import com.sami.setra.data.local.database.entity.WorkoutSessionEntity
import com.sami.setra.data.local.database.seed.ExerciseDatabaseImporter
import com.sami.setra.data.model.SplitTemplate
import com.sami.setra.data.repository.SplitTemplateRepository
import java.util.concurrent.TimeUnit

/**
 * Synthetic showcase data for local development only.
 * This source is compiled into debug APKs only and is never present in release APKs.
 */
internal object DemoDataSeeder {
    private const val PREFS = "debug_demo_data"
    private const val SEEDED_KEY = "com.sami.fitfactor.demo_seed_v1"
    private const val DEMO_DESCRIPTION = "Local debug/demo data. Safe to remove by clearing app data."

    private data class WorkoutRef(
        val routineId: Long,
        val routineName: String,
        val workoutId: Long,
        val workoutName: String,
        val exerciseIds: List<String>
    )

    suspend fun seed(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(SEEDED_KEY, false)) {
            return "Demo data was already seeded. Clear app data to seed again."
        }

        val database = SetraDatabase.getDatabase(context)
        ExerciseDatabaseImporter.seedIfNeeded(context, database)

        val workoutRefs = database.withTransaction {
            val routineDao = database.routineDao()
            val references = mutableListOf<WorkoutRef>()
            val templates = SplitTemplateRepository().builtInSplits
                .filter { it.id != "custom" }

            templates.forEach { template ->
                references += insertTemplateRoutine(template, routineDao)
            }
            references += insertConditioningRoutine(routineDao)

            val sessionDao = database.workoutSessionDao()
            val now = System.currentTimeMillis()
            val daysAgo = listOf(0, 1, 3, 5, 6, 9, 14, 21, 30, 42, 56, 75)

            daysAgo.forEachIndexed { index, ageInDays ->
                val workout = references[index % references.size]
                val completedAt = now - TimeUnit.DAYS.toMillis(ageInDays.toLong()) - TimeUnit.MINUTES.toMillis(20)
                val durationMinutes = 42L + (index % 4) * 7L
                val startedAt = completedAt - TimeUnit.MINUTES.toMillis(durationMinutes)
                val sessionId = sessionDao.insertWorkoutSession(
                    WorkoutSessionEntity(
                        routineId = workout.routineId,
                        workoutId = workout.workoutId,
                        routineName = workout.routineName,
                        workoutName = workout.workoutName,
                        startedAt = startedAt,
                        completedAt = completedAt,
                        status = "COMPLETED",
                        notes = if (index % 3 == 0) "Demo session — technique felt strong." else null
                    )
                )
                insertSessionDetails(
                    sessionId = sessionId,
                    workout = workout,
                    database = database,
                    sessionTime = completedAt,
                    progressionIndex = daysAgo.lastIndex - index,
                    completed = true
                )
            }

            val activeWorkout = references.first()
            val activeStartedAt = now - TimeUnit.MINUTES.toMillis(18)
            val activeSessionId = sessionDao.insertWorkoutSession(
                WorkoutSessionEntity(
                    routineId = activeWorkout.routineId,
                    workoutId = activeWorkout.workoutId,
                    routineName = activeWorkout.routineName,
                    workoutName = activeWorkout.workoutName,
                    startedAt = activeStartedAt,
                    status = "IN_PROGRESS",
                    notes = "Debug demo session in progress"
                )
            )
            insertSessionDetails(
                sessionId = activeSessionId,
                workout = activeWorkout,
                database = database,
                sessionTime = now,
                progressionIndex = daysAgo.lastIndex,
                completed = false,
                partiallyComplete = true
            )

            references
        }

        prefs.edit().putBoolean(SEEDED_KEY, true).apply()
        return "Seeded ${workoutRefs.size} demo routines, 12 completed workouts, one active workout, history, volume, progress and PR data."
    }

    private suspend fun insertTemplateRoutine(
        template: SplitTemplate,
        routineDao: com.sami.setra.data.local.database.dao.RoutineDao
    ): List<WorkoutRef> {
        val now = System.currentTimeMillis()
        val routineName = "Demo ${template.name}"
        val routineId = routineDao.insertRoutine(
            RoutineEntity(
                name = routineName,
                description = DEMO_DESCRIPTION,
                creatorMetadata = "User Created Routine",
                imageSource = "BUILT_IN",
                builtInImageId = template.imageKey,
                createdAt = now,
                updatedAt = now
            )
        )
        val dayIds = routineDao.insertRoutineDays(
            template.days.mapIndexed { index, day ->
                RoutineDayEntity(
                    routineId = routineId,
                    dayOfWeek = day.dayOfWeek,
                    ordering = index
                )
            }
        )

        val workoutRefs = mutableListOf<WorkoutRef>()
        template.days.forEachIndexed { dayIndex, day ->
            if (day.isRestDay || day.workoutName.isNullOrBlank()) return@forEachIndexed
            val workoutId = routineDao.insertWorkout(
                WorkoutEntity(
                    routineDayId = dayIds[dayIndex],
                    name = day.workoutName,
                    description = day.workoutDescription.orEmpty(),
                    ordering = 0
                )
            )
            day.sampleExercises.forEachIndexed { exerciseIndex, exercise ->
                val routineExerciseId = routineDao.insertRoutineExercise(
                    RoutineExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = exercise.exerciseId,
                        ordering = exerciseIndex,
                        notes = "${exercise.sets} sets × ${exercise.repRange}"
                    )
                )
                val numberOfSets = exercise.sets.substringAfterLast('-').toIntOrNull() ?: 3
                val targetReps = exercise.repRange.substringBefore('-').toDoubleOrNull()
                routineDao.insertSetTemplates(
                    (0 until numberOfSets).map { setIndex ->
                        SetTemplateEntity(
                            routineExerciseId = routineExerciseId,
                            ordering = setIndex,
                            targetReps = targetReps,
                            durationSeconds = if (exercise.repRange == "sets") 45 else null,
                            restSeconds = 90,
                            notes = "Reference prescription: ${exercise.sets} sets × ${exercise.repRange}"
                        )
                    }
                )
            }
            workoutRefs += WorkoutRef(
                routineId = routineId,
                routineName = routineName,
                workoutId = workoutId,
                workoutName = day.workoutName,
                exerciseIds = day.sampleExercises.map { it.exerciseId }
            )
        }
        return workoutRefs
    }

    private suspend fun insertConditioningRoutine(
        routineDao: com.sami.setra.data.local.database.dao.RoutineDao
    ): List<WorkoutRef> {
        val now = System.currentTimeMillis()
        val name = "Demo Conditioning & Core"
        val routineId = routineDao.insertRoutine(
            RoutineEntity(
                name = name,
                description = DEMO_DESCRIPTION,
                creatorMetadata = "User Created Routine",
                imageSource = "BUILT_IN",
                builtInImageId = "custom",
                createdAt = now,
                updatedAt = now
            )
        )
        val dayIds = routineDao.insertRoutineDays(
            (1..7).map { day -> RoutineDayEntity(routineId = routineId, dayOfWeek = day, ordering = day - 1) }
        )
        val workouts = listOf(
            Triple(1, "Upper Body + Core", listOf("Barbell_Bench_Press_-_Medium_Grip", "Dumbbell_Shoulder_Press", "Plank")),
            Triple(3, "Treadmill Intervals", listOf("Running_Treadmill", "Plank")),
            Triple(5, "Full Body Circuit", listOf("Barbell_Squat", "Wide-Grip_Lat_Pulldown", "Plank"))
        )
        return workouts.mapIndexed { index, (dayOfWeek, workoutName, exerciseIds) ->
            val workoutId = routineDao.insertWorkout(
                WorkoutEntity(
                    routineDayId = dayIds[dayOfWeek - 1],
                    name = workoutName,
                    description = "Demo strength, core and conditioning session",
                    ordering = index
                )
            )
            exerciseIds.forEachIndexed { exerciseIndex, exerciseId ->
                val routineExerciseId = routineDao.insertRoutineExercise(
                    RoutineExerciseEntity(workoutId = workoutId, exerciseId = exerciseId, ordering = exerciseIndex)
                )
                val timed = exerciseId == "Plank" || exerciseId == "Running_Treadmill"
                routineDao.insertSetTemplates(
                    (0..2).map { setIndex ->
                        SetTemplateEntity(
                            routineExerciseId = routineExerciseId,
                            ordering = setIndex,
                            targetReps = if (timed) null else 10.0,
                            durationSeconds = if (exerciseId == "Plank") 45 + setIndex * 10 else if (exerciseId == "Running_Treadmill") 300 else null,
                            targetDistanceMeters = if (exerciseId == "Running_Treadmill") 800.0 + setIndex * 100 else null,
                            restSeconds = 60
                        )
                    }
                )
            }
            WorkoutRef(routineId, name, workoutId, workoutName, exerciseIds)
        }
    }

    private suspend fun insertSessionDetails(
        sessionId: Long,
        workout: WorkoutRef,
        database: SetraDatabase,
        sessionTime: Long,
        progressionIndex: Int,
        completed: Boolean,
        partiallyComplete: Boolean = false
    ) {
        val sessionDao = database.workoutSessionDao()
        val exerciseIds = workout.exerciseIds.take(4).ifEmpty {
            listOf("Barbell_Bench_Press_-_Medium_Grip", "Barbell_Squat", "Plank")
        }
        exerciseIds.forEachIndexed { exerciseIndex, exerciseId ->
            val exerciseName = database.exerciseDao().getExerciseByIdSync(exerciseId)?.name
                ?: exerciseId.replace('_', ' ')
            val sessionExerciseId = sessionDao.insertSessionExercises(
                listOf(
                    SessionExerciseEntity(
                        workoutSessionId = sessionId,
                        exerciseId = exerciseId,
                        exerciseNameSnapshot = exerciseName,
                        notesSnapshot = if (exerciseIndex == 0) "Demo data" else null,
                        ordering = exerciseIndex
                    )
                )
            ).first()

            val isCardio = exerciseId == "Running_Treadmill"
            val isTimedCore = exerciseId == "Plank"
            val baseWeight = when (exerciseIndex) {
                0 -> 60.0
                1 -> 40.0
                else -> 20.0
            }
            val load = baseWeight + progressionIndex.coerceAtLeast(0) * 2.5
            val numberOfSets = if (isCardio) 1 else 3
            sessionDao.insertCompletedSets(
                (0 until numberOfSets).map { setIndex ->
                    val setDone = completed || (partiallyComplete && exerciseIndex == 0 && setIndex == 0)
                    CompletedSetEntity(
                        sessionExerciseId = sessionExerciseId,
                        ordering = setIndex,
                        setType = if (setIndex == 0 && exerciseIndex == 0) "WARMUP" else "NORMAL",
                        targetWeight = if (isCardio || isTimedCore) null else load,
                        targetReps = if (isCardio || isTimedCore) null else 8.0 + (setIndex % 3) * 2,
                        targetDurationSeconds = if (isCardio) 300 else if (isTimedCore) 45 + setIndex * 10 else null,
                        targetDistanceMeters = if (isCardio) 800.0 + setIndex * 100 else null,
                        restSeconds = 75,
                        weight = if (isCardio || isTimedCore) null else load,
                        reps = if (isCardio || isTimedCore) null else 8.0 + (setIndex % 3) * 2,
                        durationSeconds = if (isCardio) 300 else if (isTimedCore) 45 + setIndex * 10 else null,
                        distanceMeters = if (isCardio) 800.0 + setIndex * 100 else null,
                        rpe = 7.0 + setIndex,
                        rir = 3 - setIndex,
                        tempo = if (isCardio) null else "2-0-2",
                        isWarmup = setIndex == 0 && exerciseIndex == 0,
                        isCompleted = setDone,
                        completedAt = if (setDone) sessionTime - TimeUnit.MINUTES.toMillis((numberOfSets - setIndex).toLong()) else null,
                        notes = if (setIndex == 0) "Seeded showcase set" else null
                    )
                }
            )
        }
    }
}
