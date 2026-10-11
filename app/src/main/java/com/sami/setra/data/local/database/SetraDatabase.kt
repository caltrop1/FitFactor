package com.sami.setra.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sami.setra.data.local.database.dao.AppMetaDao
import com.sami.setra.data.local.database.dao.ExerciseDao
import com.sami.setra.data.local.database.dao.MuscleDao
import com.sami.setra.data.local.database.dao.RoutineDao
import com.sami.setra.data.local.database.dao.WorkoutSessionDao
import com.sami.setra.data.local.database.entity.AppMetaEntity
import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.entity.ExerciseEntity
import com.sami.setra.data.local.database.entity.ExerciseImageEntity
import com.sami.setra.data.local.database.entity.ExerciseMuscleEntity
import com.sami.setra.data.local.database.entity.MuscleEntity
import com.sami.setra.data.local.database.entity.RoutineDayEntity
import com.sami.setra.data.local.database.entity.RoutineEntity
import com.sami.setra.data.local.database.entity.RoutineExerciseEntity
import com.sami.setra.data.local.database.entity.SessionExerciseEntity
import com.sami.setra.data.local.database.entity.SetTemplateEntity
import com.sami.setra.data.local.database.entity.SupersetGroupEntity
import com.sami.setra.data.local.database.entity.WorkoutEntity
import com.sami.setra.data.local.database.entity.WorkoutSessionEntity

@Database(
    entities = [
        AppMetaEntity::class,
        ExerciseEntity::class,
        ExerciseImageEntity::class,
        MuscleEntity::class,
        ExerciseMuscleEntity::class,
        RoutineEntity::class,
        RoutineDayEntity::class,
        WorkoutEntity::class,
        SupersetGroupEntity::class,
        RoutineExerciseEntity::class,
        SetTemplateEntity::class,
        WorkoutSessionEntity::class,
        SessionExerciseEntity::class,
        CompletedSetEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class SetraDatabase : RoomDatabase() {

    abstract fun appMetaDao(): AppMetaDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun muscleDao(): MuscleDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutSessionDao(): WorkoutSessionDao

    companion object {
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workout_session` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `routineId` INTEGER,
                        `workoutId` INTEGER,
                        `routineName` TEXT NOT NULL,
                        `workoutName` TEXT NOT NULL,
                        `startedAt` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        `status` TEXT NOT NULL,
                        `notes` TEXT,
                        FOREIGN KEY(`routineId`) REFERENCES `routine`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(`workoutId`) REFERENCES `workout`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_session_routineId` ON `workout_session` (`routineId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_session_workoutId` ON `workout_session` (`workoutId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_session_status` ON `workout_session` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_session_startedAt` ON `workout_session` (`startedAt`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `session_exercise` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `workoutSessionId` INTEGER NOT NULL,
                        `exerciseId` TEXT,
                        `exerciseNameSnapshot` TEXT NOT NULL,
                        `notesSnapshot` TEXT,
                        `ordering` INTEGER NOT NULL,
                        FOREIGN KEY(`workoutSessionId`) REFERENCES `workout_session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`exerciseId`) REFERENCES `exercise`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercise_workoutSessionId` ON `session_exercise` (`workoutSessionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercise_exerciseId` ON `session_exercise` (`exerciseId`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `completed_set` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionExerciseId` INTEGER NOT NULL,
                        `ordering` INTEGER NOT NULL,
                        `setType` TEXT NOT NULL,
                        `targetWeight` REAL,
                        `targetReps` REAL,
                        `targetDurationSeconds` INTEGER,
                        `restSeconds` INTEGER,
                        `weight` REAL,
                        `reps` REAL,
                        `durationSeconds` INTEGER,
                        `rpe` REAL,
                        `rir` INTEGER,
                        `tempo` TEXT,
                        `isWarmup` INTEGER NOT NULL,
                        `isDropSet` INTEGER NOT NULL,
                        `isFailureSet` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        `notes` TEXT,
                        FOREIGN KEY(`sessionExerciseId`) REFERENCES `session_exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_completed_set_sessionExerciseId` ON `completed_set` (`sessionExerciseId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_completed_set_isCompleted` ON `completed_set` (`isCompleted`)")
            }
        }

        @Volatile
        private var INSTANCE: SetraDatabase? = null

        fun getDatabase(context: Context): SetraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SetraDatabase::class.java,
                    "setra_database"
                )
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
