package com.sami.setra.data.importexport

import android.content.Context
import android.net.Uri
import com.sami.setra.data.importexport.migrator.RoutineSchemaMigrator
import com.sami.setra.data.importexport.model.PortableRoutine
import com.sami.setra.data.importexport.resolver.ExerciseResolutionSummary
import com.sami.setra.data.importexport.resolver.ExerciseResolver
import com.sami.setra.data.importexport.serializer.RoutineSerializer
import com.sami.setra.data.importexport.validator.RoutineSchemaValidator
import com.sami.setra.data.importexport.validator.ValidationResult
import com.sami.setra.data.local.database.SetraDatabase
import com.sami.setra.data.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutineImportManager(
    private val context: Context,
    private val routineRepository: RoutineRepository = RoutineRepository(context),
    private val database: SetraDatabase = SetraDatabase.getDatabase(context)
) {

    private val exerciseResolver = ExerciseResolver(database.exerciseDao())

    suspend fun readAndValidate(uri: Uri): Result<PortableRoutine> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IllegalStateException("Unable to open file stream"))

            val bytes = inputStream.use { it.readBytes() }
            if (bytes.size > 5 * 1024 * 1024) { // 5MB limit
                return@withContext Result.failure(IllegalArgumentException("File size exceeds maximum allowed limit (5MB)."))
            }

            val rawJson = String(bytes, Charsets.UTF_8)
            val migratedJson = RoutineSchemaMigrator.migrateIfNeeded(rawJson)
            val portableRoutine = RoutineSerializer.deserialize(migratedJson)

            when (val validation = RoutineSchemaValidator.validate(portableRoutine)) {
                is ValidationResult.Valid -> Result.success(portableRoutine)
                is ValidationResult.Invalid -> Result.failure(IllegalArgumentException(validation.reason))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveExercises(routine: PortableRoutine): ExerciseResolutionSummary = withContext(Dispatchers.IO) {
        exerciseResolver.resolveExercises(routine)
    }

    suspend fun importRoutine(
        routine: PortableRoutine,
        resolutionSummary: ExerciseResolutionSummary,
        customName: String? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val resolvedMap = mutableMapOf<String, String>()
            val skippedSet = mutableSetOf<String>()

            resolutionSummary.items.forEach { item ->
                if (item.isSkipped) {
                    skippedSet.add(item.originalExerciseId)
                } else if (item.mappedExerciseId != null) {
                    resolvedMap[item.originalExerciseId] = item.mappedExerciseId
                }
            }

            val importedId = routineRepository.importRoutine(
                portableRoutine = routine,
                resolvedExerciseMap = resolvedMap,
                skippedExerciseIds = skippedSet,
                customName = customName
            )

            Result.success(importedId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
