package com.sami.setra.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sami.setra.data.local.database.entity.ExerciseEntity
import com.sami.setra.data.local.database.entity.ExerciseImageEntity
import com.sami.setra.data.local.database.entity.ExerciseMuscleEntity
import com.sami.setra.data.local.database.relation.ExerciseWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT COUNT(*) FROM exercise")
    suspend fun getExercisesCount(): Int

    @Transaction
    @Query("""
        SELECT DISTINCT e.* FROM exercise e
        LEFT JOIN exercise_muscle em ON e.id = em.exerciseId
        WHERE (:query IS NULL OR :query = '' OR LOWER(e.name) LIKE '%' || LOWER(:query) || '%')
          AND (:equipment IS NULL OR :equipment = '' OR e.equipment = :equipment)
          AND (:category IS NULL OR :category = '' OR e.category = :category)
          AND (:level IS NULL OR :level = '' OR e.level = :level)
          AND (:muscleId IS NULL OR :muscleId = '' OR em.muscleId = :muscleId)
        ORDER BY e.name ASC
    """)
    fun searchExercises(
        query: String? = null,
        equipment: String? = null,
        muscleId: String? = null,
        category: String? = null,
        level: String? = null
    ): Flow<List<ExerciseWithDetails>>

    @Transaction
    @Query("SELECT * FROM exercise WHERE id = :exerciseId LIMIT 1")
    fun getExerciseById(exerciseId: String): Flow<ExerciseWithDetails?>

    @Query("SELECT * FROM exercise WHERE id = :exerciseId LIMIT 1")
    suspend fun getExerciseByIdSync(exerciseId: String): ExerciseEntity?

    @Query("SELECT * FROM exercise WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getExerciseByNameSync(name: String): ExerciseEntity?

    @Query("SELECT * FROM exercise ORDER BY name ASC")
    suspend fun getAllExercisesSync(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseImages(images: List<ExerciseImageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseMuscles(exerciseMuscles: List<ExerciseMuscleEntity>)

    @Query("SELECT DISTINCT equipment FROM exercise WHERE equipment IS NOT NULL ORDER BY equipment ASC")
    fun getAllEquipment(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM exercise WHERE category IS NOT NULL ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT level FROM exercise WHERE level IS NOT NULL ORDER BY level ASC")
    fun getAllLevels(): Flow<List<String>>
}
