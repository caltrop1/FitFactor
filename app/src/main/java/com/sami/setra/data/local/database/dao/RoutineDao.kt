package com.sami.setra.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sami.setra.data.local.database.entity.RoutineDayEntity
import com.sami.setra.data.local.database.entity.RoutineEntity
import com.sami.setra.data.local.database.entity.RoutineExerciseEntity
import com.sami.setra.data.local.database.entity.SetTemplateEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity
import com.sami.setra.data.local.database.relation.RoutineWithDays
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @androidx.room.Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineDays(routineDays: List<RoutineDayEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutEntity>): List<Long>

    @androidx.room.Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @androidx.room.Update
    suspend fun updateWorkouts(workouts: List<WorkoutEntity>)

    @Query("DELETE FROM workout WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(routineExercise: RoutineExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>): List<Long>

    @androidx.room.Update
    suspend fun updateRoutineExercise(routineExercise: RoutineExerciseEntity)

    @androidx.room.Update
    suspend fun updateRoutineExercises(routineExercises: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routine_exercise WHERE id = :routineExerciseId")
    suspend fun deleteRoutineExercise(routineExerciseId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetTemplate(setTemplate: SetTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetTemplates(setTemplates: List<SetTemplateEntity>): List<Long>

    @androidx.room.Update
    suspend fun updateSetTemplate(setTemplate: SetTemplateEntity)

    @androidx.room.Update
    suspend fun updateSetTemplates(setTemplates: List<SetTemplateEntity>)

    @Query("DELETE FROM set_template WHERE id = :setTemplateId")
    suspend fun deleteSetTemplate(setTemplateId: Long)

    @Transaction
    @Query("SELECT * FROM routine WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveRoutines(): Flow<List<RoutineWithDays>>

    @Transaction
    @Query("SELECT * FROM routine WHERE id = :routineId LIMIT 1")
    fun getRoutineWithDaysById(routineId: Long): Flow<RoutineWithDays?>

    @Transaction
    @Query("SELECT * FROM routine WHERE isArchived = 0 ORDER BY createdAt DESC")
    suspend fun getAllActiveRoutinesSync(): List<RoutineWithDays>

    @Transaction
    @Query("SELECT * FROM routine WHERE id = :routineId LIMIT 1")
    suspend fun getRoutineWithDaysByIdSync(routineId: Long): RoutineWithDays?

    @Transaction
    @Query("SELECT * FROM workout WHERE id = :workoutId LIMIT 1")
    suspend fun getWorkoutWithExercisesByIdSync(workoutId: Long): com.sami.setra.data.local.database.relation.WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM routine_exercise WHERE id = :routineExerciseId LIMIT 1")
    suspend fun getRoutineExerciseWithDetailsByIdSync(routineExerciseId: Long): com.sami.setra.data.local.database.relation.RoutineExerciseWithDetails?

    @Query("SELECT r.id FROM routine r INNER JOIN routine_day rd ON r.id = rd.routineId WHERE rd.id = :routineDayId LIMIT 1")
    suspend fun getRoutineIdByDayId(routineDayId: Long): Long?

    @Query("SELECT r.name FROM routine r INNER JOIN routine_day rd ON r.id = rd.routineId WHERE rd.id = :routineDayId LIMIT 1")
    suspend fun getRoutineNameByDayId(routineDayId: Long): String?

    @Query("DELETE FROM routine WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)
}
