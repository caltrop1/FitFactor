package com.sami.setra.data.repository

import android.content.Context
import com.sami.setra.data.local.database.SetraDatabase
import com.sami.setra.data.local.database.entity.RoutineDayEntity
import com.sami.setra.data.local.database.entity.RoutineEntity
import com.sami.setra.data.local.database.entity.RoutineExerciseEntity
import com.sami.setra.data.local.database.entity.SetTemplateEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity
import com.sami.setra.data.local.database.relation.RoutineWithDays
import com.sami.setra.data.model.SplitTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoutineRepository(
    private val context: Context,
    private val database: SetraDatabase = SetraDatabase.getDatabase(context)
) {
    private val routineDao = database.routineDao()

    fun getAllActiveRoutines(): Flow<List<RoutineWithDays>> {
        return routineDao.getAllActiveRoutines()
    }

    fun getRoutineById(routineId: Long): Flow<RoutineWithDays?> {
        return routineDao.getRoutineWithDaysById(routineId)
    }

    suspend fun createNewRoutine(
        name: String = "My Custom Routine",
        description: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val routineEntity = RoutineEntity(
            name = name.ifBlank { "My Custom Routine" },
            description = description,
            creatorMetadata = "User Created Routine",
            imageSource = "BUILT_IN",
            builtInImageId = "custom",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val routineId = routineDao.insertRoutine(routineEntity)

        // Create 7 days (Monday = 1, ..., Sunday = 7)
        val dayEntities = (1..7).map { dayOfWeek ->
            RoutineDayEntity(
                routineId = routineId,
                dayOfWeek = dayOfWeek,
                ordering = dayOfWeek - 1
            )
        }
        routineDao.insertRoutineDays(dayEntities)

        routineId
    }

    suspend fun createRoutineFromSplit(template: SplitTemplate): Long = withContext(Dispatchers.IO) {
        val routineEntity = RoutineEntity(
            name = "My ${template.name}",
            description = template.description,
            creatorMetadata = "Copied from Setra ${template.name} Template",
            imageSource = "BUILT_IN",
            builtInImageId = template.imageKey,
            userImageUri = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val routineId = routineDao.insertRoutine(routineEntity)

        template.days.forEachIndexed { index, day ->
            val dayEntity = RoutineDayEntity(
                routineId = routineId,
                dayOfWeek = day.dayOfWeek,
                ordering = index
            )
            val dayIdList = routineDao.insertRoutineDays(listOf(dayEntity))
            val dayId = dayIdList.first()

            if (!day.isRestDay && !day.workoutName.isNullOrBlank()) {
                val workoutEntity = WorkoutEntity(
                    routineDayId = dayId,
                    name = day.workoutName,
                    description = day.workoutDescription ?: "",
                    ordering = 0
                )
                val workoutIdList = routineDao.insertWorkouts(listOf(workoutEntity))
                val workoutId = workoutIdList.first()

                day.sampleExercises.forEachIndexed { exIndex, exerciseId ->
                    val routineExerciseEntity = RoutineExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = exerciseId,
                        ordering = exIndex
                    )
                    val routineExerciseIdList = routineDao.insertRoutineExercises(listOf(routineExerciseEntity))
                    val routineExerciseId = routineExerciseIdList.first()

                    val setTemplates = listOf(
                        SetTemplateEntity(routineExerciseId = routineExerciseId, ordering = 0, targetReps = 10.0, restSeconds = 90),
                        SetTemplateEntity(routineExerciseId = routineExerciseId, ordering = 1, targetReps = 10.0, restSeconds = 90),
                        SetTemplateEntity(routineExerciseId = routineExerciseId, ordering = 2, targetReps = 10.0, restSeconds = 90)
                    )
                    routineDao.insertSetTemplates(setTemplates)
                }
            }
        }

        routineId
    }

    suspend fun updateRoutineHeader(
        routineId: Long,
        name: String,
        description: String
    ) = withContext(Dispatchers.IO) {
        val existing = routineDao.getRoutineWithDaysByIdSync(routineId)?.routine ?: return@withContext
        val updated = existing.copy(
            name = name.ifBlank { "Untitled Routine" },
            description = description,
            updatedAt = System.currentTimeMillis()
        )
        routineDao.updateRoutine(updated)
    }

    suspend fun deleteRoutine(routineId: Long) = withContext(Dispatchers.IO) {
        routineDao.deleteRoutine(routineId)
    }

    suspend fun duplicateRoutine(routineId: Long): Long = withContext(Dispatchers.IO) {
        val oldRoutineWithDays = routineDao.getRoutineWithDaysByIdSync(routineId) ?: return@withContext -1L
        val oldRoutine = oldRoutineWithDays.routine

        val newRoutineEntity = RoutineEntity(
            name = "${oldRoutine.name} (Copy)",
            description = oldRoutine.description,
            creatorMetadata = oldRoutine.creatorMetadata,
            sharedByMetadata = oldRoutine.sharedByMetadata,
            imageSource = oldRoutine.imageSource,
            builtInImageId = oldRoutine.builtInImageId,
            userImageUri = oldRoutine.userImageUri,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newRoutineId = routineDao.insertRoutine(newRoutineEntity)

        oldRoutineWithDays.sortedDays.forEach { oldDayWithWorkouts ->
            val newDayEntity = RoutineDayEntity(
                routineId = newRoutineId,
                dayOfWeek = oldDayWithWorkouts.routineDay.dayOfWeek,
                ordering = oldDayWithWorkouts.routineDay.ordering
            )
            val newDayId = routineDao.insertRoutineDays(listOf(newDayEntity)).first()

            oldDayWithWorkouts.sortedWorkouts.forEach { oldWorkoutWithExercises ->
                val newWorkoutEntity = WorkoutEntity(
                    routineDayId = newDayId,
                    name = oldWorkoutWithExercises.workout.name,
                    description = oldWorkoutWithExercises.workout.description,
                    ordering = oldWorkoutWithExercises.workout.ordering
                )
                val newWorkoutId = routineDao.insertWorkout(newWorkoutEntity)

                oldWorkoutWithExercises.sortedExercises.forEach { oldExerciseWithDetails ->
                    val newExerciseEntity = RoutineExerciseEntity(
                        workoutId = newWorkoutId,
                        exerciseId = oldExerciseWithDetails.routineExercise.exerciseId,
                        ordering = oldExerciseWithDetails.routineExercise.ordering,
                        notes = oldExerciseWithDetails.routineExercise.notes
                    )
                    val newExerciseId = routineDao.insertRoutineExercise(newExerciseEntity)

                    val newSetTemplates = oldExerciseWithDetails.sortedSetTemplates.map { oldSet ->
                        SetTemplateEntity(
                            routineExerciseId = newExerciseId,
                            ordering = oldSet.ordering,
                            setType = oldSet.setType,
                            targetReps = oldSet.targetReps,
                            targetWeight = oldSet.targetWeight,
                            durationSeconds = oldSet.durationSeconds,
                            restSeconds = oldSet.restSeconds,
                            targetRpe = oldSet.targetRpe,
                            targetRir = oldSet.targetRir,
                            tempo = oldSet.tempo,
                            isWarmup = oldSet.isWarmup,
                            isDropSet = oldSet.isDropSet,
                            isFailureSet = oldSet.isFailureSet,
                            notes = oldSet.notes
                        )
                    }
                    if (newSetTemplates.isNotEmpty()) {
                        routineDao.insertSetTemplates(newSetTemplates)
                    }
                }
            }
        }

        newRoutineId
    }

    suspend fun addWorkout(
        routineDayId: Long,
        name: String = "New Workout",
        description: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val workout = WorkoutEntity(
            routineDayId = routineDayId,
            name = name,
            description = description,
            ordering = System.currentTimeMillis().toInt()
        )
        routineDao.insertWorkout(workout)
    }

    suspend fun updateWorkout(workout: WorkoutEntity) = withContext(Dispatchers.IO) {
        routineDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workoutId: Long) = withContext(Dispatchers.IO) {
        routineDao.deleteWorkout(workoutId)
    }

    suspend fun duplicateWorkout(workoutId: Long): Long = withContext(Dispatchers.IO) {
        val oldWorkoutWithExercises = routineDao.getWorkoutWithExercisesByIdSync(workoutId) ?: return@withContext -1L
        val oldWorkout = oldWorkoutWithExercises.workout

        val newWorkoutEntity = WorkoutEntity(
            routineDayId = oldWorkout.routineDayId,
            name = "${oldWorkout.name} (Copy)",
            description = oldWorkout.description,
            ordering = oldWorkout.ordering + 1
        )
        val newWorkoutId = routineDao.insertWorkout(newWorkoutEntity)

        oldWorkoutWithExercises.sortedExercises.forEach { oldExercise ->
            val newExerciseEntity = RoutineExerciseEntity(
                workoutId = newWorkoutId,
                exerciseId = oldExercise.routineExercise.exerciseId,
                ordering = oldExercise.routineExercise.ordering,
                notes = oldExercise.routineExercise.notes
            )
            val newExerciseId = routineDao.insertRoutineExercise(newExerciseEntity)

            val newSetTemplates = oldExercise.sortedSetTemplates.map { oldSet ->
                SetTemplateEntity(
                    routineExerciseId = newExerciseId,
                    ordering = oldSet.ordering,
                    setType = oldSet.setType,
                    targetReps = oldSet.targetReps,
                    targetWeight = oldSet.targetWeight,
                    durationSeconds = oldSet.durationSeconds,
                    restSeconds = oldSet.restSeconds,
                    targetRpe = oldSet.targetRpe,
                    targetRir = oldSet.targetRir,
                    tempo = oldSet.tempo,
                    isWarmup = oldSet.isWarmup,
                    isDropSet = oldSet.isDropSet,
                    isFailureSet = oldSet.isFailureSet,
                    notes = oldSet.notes
                )
            }
            if (newSetTemplates.isNotEmpty()) {
                routineDao.insertSetTemplates(newSetTemplates)
            }
        }

        newWorkoutId
    }

    suspend fun reorderWorkouts(workouts: List<WorkoutEntity>) = withContext(Dispatchers.IO) {
        val reordered = workouts.mapIndexed { index, workout ->
            workout.copy(ordering = index)
        }
        routineDao.updateWorkouts(reordered)
    }

    suspend fun addExerciseToWorkout(
        workoutId: Long,
        exerciseId: String
    ): Long = withContext(Dispatchers.IO) {
        val workoutWithExercises = routineDao.getWorkoutWithExercisesByIdSync(workoutId)
        val nextOrdering = workoutWithExercises?.routineExercises?.size ?: 0

        val routineExerciseEntity = RoutineExerciseEntity(
            workoutId = workoutId,
            exerciseId = exerciseId,
            ordering = nextOrdering
        )
        val routineExerciseId = routineDao.insertRoutineExercise(routineExerciseEntity)

        // Add 3 default set templates
        val setTemplates = listOf(
            SetTemplateEntity(routineExerciseId = routineExerciseId, ordering = 0, targetReps = 10.0, restSeconds = 90),
            SetTemplateEntity(routineExerciseId = routineExerciseId, ordering = 1, targetReps = 10.0, restSeconds = 90),
            SetTemplateEntity(routineExerciseId = routineExerciseId, ordering = 2, targetReps = 10.0, restSeconds = 90)
        )
        routineDao.insertSetTemplates(setTemplates)

        routineExerciseId
    }

    suspend fun deleteRoutineExercise(routineExerciseId: Long) = withContext(Dispatchers.IO) {
        routineDao.deleteRoutineExercise(routineExerciseId)
    }

    suspend fun duplicateRoutineExercise(routineExerciseId: Long): Long = withContext(Dispatchers.IO) {
        val oldDetails = routineDao.getRoutineExerciseWithDetailsByIdSync(routineExerciseId) ?: return@withContext -1L
        val oldEx = oldDetails.routineExercise

        val newExEntity = RoutineExerciseEntity(
            workoutId = oldEx.workoutId,
            exerciseId = oldEx.exerciseId,
            ordering = oldEx.ordering + 1,
            notes = oldEx.notes
        )
        val newExId = routineDao.insertRoutineExercise(newExEntity)

        val newSetTemplates = oldDetails.sortedSetTemplates.map { oldSet ->
            SetTemplateEntity(
                routineExerciseId = newExId,
                ordering = oldSet.ordering,
                setType = oldSet.setType,
                targetReps = oldSet.targetReps,
                targetWeight = oldSet.targetWeight,
                durationSeconds = oldSet.durationSeconds,
                restSeconds = oldSet.restSeconds,
                targetRpe = oldSet.targetRpe,
                targetRir = oldSet.targetRir,
                tempo = oldSet.tempo,
                isWarmup = oldSet.isWarmup,
                isDropSet = oldSet.isDropSet,
                isFailureSet = oldSet.isFailureSet,
                notes = oldSet.notes
            )
        }
        if (newSetTemplates.isNotEmpty()) {
            routineDao.insertSetTemplates(newSetTemplates)
        }

        newExId
    }

    suspend fun reorderRoutineExercises(exercises: List<RoutineExerciseEntity>) = withContext(Dispatchers.IO) {
        val reordered = exercises.mapIndexed { index, ex ->
            ex.copy(ordering = index)
        }
        routineDao.updateRoutineExercises(reordered)
    }

    suspend fun addSetTemplate(routineExerciseId: Long): Long = withContext(Dispatchers.IO) {
        val oldDetails = routineDao.getRoutineExerciseWithDetailsByIdSync(routineExerciseId)
        val existingSets = oldDetails?.sortedSetTemplates ?: emptyList()
        val lastSet = existingSets.lastOrNull()

        val nextOrdering = existingSets.size
        val newSet = SetTemplateEntity(
            routineExerciseId = routineExerciseId,
            ordering = nextOrdering,
            targetReps = lastSet?.targetReps ?: 10.0,
            targetWeight = lastSet?.targetWeight,
            restSeconds = lastSet?.restSeconds ?: 90,
            setType = "NORMAL"
        )
        routineDao.insertSetTemplate(newSet)
    }

    suspend fun updateSetTemplate(setTemplate: SetTemplateEntity) = withContext(Dispatchers.IO) {
        routineDao.updateSetTemplate(setTemplate)
    }

    suspend fun deleteSetTemplate(setTemplateId: Long) = withContext(Dispatchers.IO) {
        routineDao.deleteSetTemplate(setTemplateId)
    }

    suspend fun reorderSetTemplates(setTemplates: List<SetTemplateEntity>) = withContext(Dispatchers.IO) {
        val reordered = setTemplates.mapIndexed { index, set ->
            set.copy(ordering = index)
        }
        routineDao.updateSetTemplates(reordered)
    }
}
