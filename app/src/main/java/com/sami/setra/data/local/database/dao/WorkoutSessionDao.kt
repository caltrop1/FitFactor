package com.sami.setra.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.entity.SessionExerciseEntity
import com.sami.setra.data.local.database.entity.WorkoutSessionEntity
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateWorkoutSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_session WHERE id = :sessionId")
    suspend fun deleteWorkoutSession(sessionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercises(exercises: List<SessionExerciseEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedSets(sets: List<CompletedSetEntity>): List<Long>

    @Update
    suspend fun updateCompletedSet(completedSet: CompletedSetEntity)

    @Update
    suspend fun updateCompletedSets(completedSets: List<CompletedSetEntity>)

    @Query("DELETE FROM completed_set WHERE id = :setId")
    suspend fun deleteCompletedSet(setId: Long)

    @Transaction
    @Query("SELECT * FROM workout_session WHERE id = :sessionId LIMIT 1")
    fun getWorkoutSessionById(sessionId: Long): Flow<WorkoutSessionWithDetails?>

    @Transaction
    @Query("SELECT * FROM workout_session WHERE id = :sessionId LIMIT 1")
    suspend fun getWorkoutSessionByIdSync(sessionId: Long): WorkoutSessionWithDetails?

    @Transaction
    @Query("SELECT * FROM workout_session WHERE status = 'IN_PROGRESS' ORDER BY startedAt DESC LIMIT 1")
    fun getActiveWorkoutSession(): Flow<WorkoutSessionWithDetails?>

    @Transaction
    @Query("SELECT * FROM workout_session WHERE status = 'IN_PROGRESS' ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveWorkoutSessionSync(): WorkoutSessionWithDetails?

    @Transaction
    @Query("SELECT * FROM workout_session WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getAllCompletedSessions(): Flow<List<WorkoutSessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM workout_session WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    suspend fun getAllCompletedSessionsSync(): List<WorkoutSessionWithDetails>

    @Transaction
    @Query("""
        SELECT DISTINCT ws.* FROM workout_session ws
        INNER JOIN session_exercise se ON se.workoutSessionId = ws.id
        WHERE se.exerciseId = :exerciseId AND ws.status = 'COMPLETED'
        ORDER BY ws.completedAt DESC
    """)
    fun getCompletedSessionsForExercise(exerciseId: String): Flow<List<WorkoutSessionWithDetails>>

    @Query("""
        SELECT cs.* FROM completed_set cs
        INNER JOIN session_exercise se ON cs.sessionExerciseId = se.id
        INNER JOIN workout_session ws ON se.workoutSessionId = ws.id
        WHERE se.exerciseId = :exerciseId
          AND ws.status = 'COMPLETED'
          AND ws.id = (
              SELECT ws2.id FROM workout_session ws2
              INNER JOIN session_exercise se2 ON se2.workoutSessionId = ws2.id
              WHERE se2.exerciseId = :exerciseId
                AND ws2.status = 'COMPLETED'
              ORDER BY ws2.completedAt DESC
              LIMIT 1
          )
        ORDER BY cs.ordering ASC
    """)
    suspend fun getPreviousPerformanceForExercise(exerciseId: String): List<CompletedSetEntity>
}
