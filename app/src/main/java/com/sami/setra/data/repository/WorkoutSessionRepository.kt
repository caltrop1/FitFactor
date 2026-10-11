package com.sami.setra.data.repository

import android.content.Context
import com.sami.setra.data.local.database.SetraDatabase
import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.entity.SessionExerciseEntity
import com.sami.setra.data.local.database.entity.WorkoutSessionEntity
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WorkoutSessionRepository(
    private val context: Context,
    private val database: SetraDatabase = SetraDatabase.getDatabase(context)
) {
    private val workoutSessionDao = database.workoutSessionDao()
    private val routineDao = database.routineDao()

    fun getActiveWorkoutSession(): Flow<WorkoutSessionWithDetails?> {
        return workoutSessionDao.getActiveWorkoutSession()
    }

    fun getWorkoutSessionById(sessionId: Long): Flow<WorkoutSessionWithDetails?> {
        return workoutSessionDao.getWorkoutSessionById(sessionId)
    }

    suspend fun startWorkout(workoutId: Long): Long = withContext(Dispatchers.IO) {
        // Prevent duplicate creation if an active session already exists
        val existingActive = workoutSessionDao.getActiveWorkoutSessionSync()
        if (existingActive != null && existingActive.session.workoutId == workoutId) {
            return@withContext existingActive.session.id
        }

        val workoutWithDetails = routineDao.getWorkoutWithExercisesByIdSync(workoutId)
            ?: throw IllegalArgumentException("Workout not found: $workoutId")

        val workoutEntity = workoutWithDetails.workout
        val routineId = routineDao.getRoutineIdByDayId(workoutEntity.routineDayId)
        val routineName = routineDao.getRoutineNameByDayId(workoutEntity.routineDayId) ?: "Custom Routine"

        val sessionEntity = WorkoutSessionEntity(
            workoutId = workoutEntity.id,
            routineId = routineId,
            routineName = routineName,
            workoutName = workoutEntity.name.ifBlank { "Workout" },
            startedAt = System.currentTimeMillis(),
            status = "IN_PROGRESS"
        )

        val sessionId = workoutSessionDao.insertWorkoutSession(sessionEntity)

        workoutWithDetails.sortedExercises.forEachIndexed { exIndex, routineExDetails ->
            val exEntity = routineExDetails.exercise
            val routineEx = routineExDetails.routineExercise

            val sessionExEntity = SessionExerciseEntity(
                workoutSessionId = sessionId,
                exerciseId = exEntity?.id ?: routineEx.exerciseId,
                exerciseNameSnapshot = exEntity?.name ?: "Exercise",
                notesSnapshot = routineEx.notes,
                ordering = exIndex
            )

            val sessionExId = workoutSessionDao.insertSessionExercises(listOf(sessionExEntity)).first()

            val setTemplates = routineExDetails.sortedSetTemplates
            if (setTemplates.isNotEmpty()) {
                val completedSets = setTemplates.mapIndexed { setIndex, tmpl ->
                    CompletedSetEntity(
                        sessionExerciseId = sessionExId,
                        ordering = setIndex,
                        setType = tmpl.setType,
                        targetWeight = tmpl.targetWeight,
                        targetReps = tmpl.targetReps,
                        targetDurationSeconds = tmpl.durationSeconds,
                        restSeconds = tmpl.restSeconds ?: 90,
                        weight = tmpl.targetWeight,
                        reps = tmpl.targetReps,
                        durationSeconds = tmpl.durationSeconds,
                        rpe = tmpl.targetRpe,
                        rir = tmpl.targetRir,
                        tempo = tmpl.tempo,
                        isWarmup = tmpl.isWarmup,
                        isDropSet = tmpl.isDropSet,
                        isFailureSet = tmpl.isFailureSet,
                        isCompleted = false
                    )
                }
                workoutSessionDao.insertCompletedSets(completedSets)
            } else {
                // If no set templates exist, create a default 3 sets template for quick performance
                val defaultSets = (0..2).map { setIndex ->
                    CompletedSetEntity(
                        sessionExerciseId = sessionExId,
                        ordering = setIndex,
                        setType = "NORMAL",
                        targetWeight = null,
                        targetReps = 10.0,
                        targetDurationSeconds = null,
                        restSeconds = 90,
                        weight = null,
                        reps = 10.0,
                        durationSeconds = null,
                        isCompleted = false
                    )
                }
                workoutSessionDao.insertCompletedSets(defaultSets)
            }
        }

        sessionId
    }

    suspend fun updateCompletedSet(completedSet: CompletedSetEntity) = withContext(Dispatchers.IO) {
        workoutSessionDao.updateCompletedSet(completedSet)
    }

    suspend fun toggleSetCompletion(setId: Long, isCompleted: Boolean, currentSet: CompletedSetEntity) = withContext(Dispatchers.IO) {
        val updatedSet = currentSet.copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null
        )
        workoutSessionDao.updateCompletedSet(updatedSet)
    }

    suspend fun addSetToExercise(sessionExerciseId: Long, currentSetsCount: Int) = withContext(Dispatchers.IO) {
        val newSet = CompletedSetEntity(
            sessionExerciseId = sessionExerciseId,
            ordering = currentSetsCount,
            setType = "NORMAL",
            targetWeight = null,
            targetReps = 10.0,
            restSeconds = 90,
            weight = null,
            reps = 10.0,
            isCompleted = false
        )
        workoutSessionDao.insertCompletedSets(listOf(newSet))
    }

    suspend fun removeSetFromExercise(setId: Long) = withContext(Dispatchers.IO) {
        workoutSessionDao.deleteCompletedSet(setId)
    }

    suspend fun getPreviousPerformanceForExercise(exerciseId: String): List<CompletedSetEntity> = withContext(Dispatchers.IO) {
        workoutSessionDao.getPreviousPerformanceForExercise(exerciseId)
    }

    suspend fun finishWorkoutSession(sessionId: Long, notes: String? = null) = withContext(Dispatchers.IO) {
        val sessionWithDetails = workoutSessionDao.getWorkoutSessionByIdSync(sessionId) ?: return@withContext
        val updatedSession = sessionWithDetails.session.copy(
            status = "COMPLETED",
            completedAt = System.currentTimeMillis(),
            notes = notes ?: sessionWithDetails.session.notes
        )
        workoutSessionDao.updateWorkoutSession(updatedSession)
    }

    suspend fun cancelWorkoutSession(sessionId: Long) = withContext(Dispatchers.IO) {
        val sessionWithDetails = workoutSessionDao.getWorkoutSessionByIdSync(sessionId) ?: return@withContext
        val updatedSession = sessionWithDetails.session.copy(
            status = "CANCELLED",
            completedAt = System.currentTimeMillis()
        )
        workoutSessionDao.updateWorkoutSession(updatedSession)
    }
}
