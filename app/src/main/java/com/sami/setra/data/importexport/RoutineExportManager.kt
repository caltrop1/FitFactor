package com.sami.setra.data.importexport

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.sami.setra.data.importexport.serializer.RoutineSerializer
import com.sami.setra.data.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RoutineExportManager(
    private val context: Context,
    private val routineRepository: RoutineRepository = RoutineRepository(context)
) {

    suspend fun getExportFilename(routineId: Long): String {
        val portableRoutine = routineRepository.getPortableRoutine(routineId)
        val name = portableRoutine?.metadata?.name ?: "Routine"
        val sanitized = name.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim('_')
        return if (sanitized.isNotBlank()) "$sanitized.fitfactor" else "routine.fitfactor"
    }

    suspend fun exportToUri(routineId: Long, targetUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val portableRoutine = routineRepository.getPortableRoutine(routineId)
                ?: return@withContext Result.failure(IllegalArgumentException("Routine not found"))

            val jsonString = RoutineSerializer.serialize(portableRoutine)

            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: return@withContext Result.failure(IllegalStateException("Could not open destination URI for writing"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun prepareShareUri(routineId: Long): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val portableRoutine = routineRepository.getPortableRoutine(routineId)
                ?: return@withContext Result.failure(IllegalArgumentException("Routine not found"))

            val jsonString = RoutineSerializer.serialize(portableRoutine)

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val filename = getExportFilename(routineId)
            val exportFile = File(exportDir, filename)

            exportFile.writeText(jsonString, Charsets.UTF_8)

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, exportFile)

            Result.success(contentUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
