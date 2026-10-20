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
    version = 9,
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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE completed_set ADD COLUMN targetDistanceMeters REAL")
                db.execSQL("ALTER TABLE completed_set ADD COLUMN distanceMeters REAL")
                db.execSQL("ALTER TABLE set_template ADD COLUMN targetDistanceMeters REAL")
            }
        }

        // Some development installs had already been marked version 8 before the distance columns
        // landed. Repair those databases in place rather than clearing a user's workout history.
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (hasColumn(db, "set_template", "targetDistance")) {
                    rebuildSetTemplates(db)
                } else if (!hasColumn(db, "set_template", "targetDistanceMeters")) {
                    db.execSQL("ALTER TABLE set_template ADD COLUMN targetDistanceMeters REAL")
                }

                val hasLegacySetDistance = hasColumn(db, "completed_set", "targetDistance") ||
                    hasColumn(db, "completed_set", "distance")
                if (hasLegacySetDistance) {
                    rebuildCompletedSets(db)
                } else if (!hasColumn(db, "completed_set", "targetDistanceMeters")) {
                    db.execSQL("ALTER TABLE completed_set ADD COLUMN targetDistanceMeters REAL")
                }
                if (!hasColumn(db, "completed_set", "distanceMeters")) {
                    db.execSQL("ALTER TABLE completed_set ADD COLUMN distanceMeters REAL")
                }
            }
        }

        private fun rebuildSetTemplates(db: SupportSQLiteDatabase) {
            val distanceColumn = distanceExpression(db, "set_template", "targetDistanceMeters", "targetDistance")
            db.execSQL("DROP TABLE IF EXISTS `set_template_repair`")
            db.execSQL("""
                CREATE TABLE `set_template_repair` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `routineExerciseId` INTEGER NOT NULL,
                    `ordering` INTEGER NOT NULL,
                    `setType` TEXT NOT NULL,
                    `targetReps` REAL,
                    `targetWeight` REAL,
                    `durationSeconds` INTEGER,
                    `targetDistanceMeters` REAL,
                    `restSeconds` INTEGER,
                    `targetRpe` REAL,
                    `targetRir` INTEGER,
                    `tempo` TEXT,
                    `isWarmup` INTEGER NOT NULL,
                    `isDropSet` INTEGER NOT NULL,
                    `isFailureSet` INTEGER NOT NULL,
                    `notes` TEXT,
                    FOREIGN KEY(`routineExerciseId`) REFERENCES `routine_exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO `set_template_repair` (
                    `id`, `routineExerciseId`, `ordering`, `setType`, `targetReps`, `targetWeight`,
                    `durationSeconds`, `targetDistanceMeters`, `restSeconds`, `targetRpe`, `targetRir`,
                    `tempo`, `isWarmup`, `isDropSet`, `isFailureSet`, `notes`
                ) SELECT `id`, `routineExerciseId`, `ordering`, `setType`, `targetReps`, `targetWeight`,
                    `durationSeconds`, $distanceColumn, `restSeconds`, `targetRpe`, `targetRir`,
                    `tempo`, `isWarmup`, `isDropSet`, `isFailureSet`, `notes` FROM `set_template`
            """.trimIndent())
            db.execSQL("DROP TABLE `set_template`")
            db.execSQL("ALTER TABLE `set_template_repair` RENAME TO `set_template`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_template_routineExerciseId` ON `set_template` (`routineExerciseId`)")
        }

        private fun rebuildCompletedSets(db: SupportSQLiteDatabase) {
            val targetDistanceColumn = distanceExpression(db, "completed_set", "targetDistanceMeters", "targetDistance")
            val distanceColumn = distanceExpression(db, "completed_set", "distanceMeters", "distance")
            db.execSQL("DROP TABLE IF EXISTS `completed_set_repair`")
            db.execSQL("""
                CREATE TABLE `completed_set_repair` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionExerciseId` INTEGER NOT NULL,
                    `ordering` INTEGER NOT NULL,
                    `setType` TEXT NOT NULL,
                    `targetWeight` REAL,
                    `targetReps` REAL,
                    `targetDurationSeconds` INTEGER,
                    `targetDistanceMeters` REAL,
                    `restSeconds` INTEGER,
                    `weight` REAL,
                    `reps` REAL,
                    `durationSeconds` INTEGER,
                    `distanceMeters` REAL,
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
            db.execSQL("""
                INSERT INTO `completed_set_repair` (
                    `id`, `sessionExerciseId`, `ordering`, `setType`, `targetWeight`, `targetReps`,
                    `targetDurationSeconds`, `targetDistanceMeters`, `restSeconds`, `weight`, `reps`,
                    `durationSeconds`, `distanceMeters`, `rpe`, `rir`, `tempo`, `isWarmup`, `isDropSet`,
                    `isFailureSet`, `isCompleted`, `completedAt`, `notes`
                ) SELECT `id`, `sessionExerciseId`, `ordering`, `setType`, `targetWeight`, `targetReps`,
                    `targetDurationSeconds`, $targetDistanceColumn, `restSeconds`, `weight`, `reps`,
                    `durationSeconds`, $distanceColumn, `rpe`, `rir`, `tempo`, `isWarmup`, `isDropSet`,
                    `isFailureSet`, `isCompleted`, `completedAt`, `notes` FROM `completed_set`
            """.trimIndent())
            db.execSQL("DROP TABLE `completed_set`")
            db.execSQL("ALTER TABLE `completed_set_repair` RENAME TO `completed_set`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_completed_set_sessionExerciseId` ON `completed_set` (`sessionExerciseId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_completed_set_isCompleted` ON `completed_set` (`isCompleted`)")
        }

        private fun hasColumn(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
            return db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                nameIndex >= 0 && generateSequence { if (cursor.moveToNext()) cursor.getString(nameIndex) else null }
                    .any { it == columnName }
            }
        }

        private fun distanceExpression(
            db: SupportSQLiteDatabase,
            tableName: String,
            currentColumn: String,
            legacyColumn: String
        ): String {
            val hasCurrent = hasColumn(db, tableName, currentColumn)
            val hasLegacy = hasColumn(db, tableName, legacyColumn)
            return when {
                hasCurrent && hasLegacy -> "COALESCE(`$currentColumn`, `$legacyColumn`)"
                hasCurrent -> "`$currentColumn`"
                hasLegacy -> "`$legacyColumn`"
                else -> "NULL"
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
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
