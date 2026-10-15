package com.sami.setra.ui.screens.routines

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sami.setra.data.importexport.RoutineExportManager
import com.sami.setra.data.importexport.RoutineImportManager
import com.sami.setra.data.importexport.model.PortableRoutine
import com.sami.setra.data.importexport.resolver.ExerciseResolutionSummary
import com.sami.setra.data.local.database.relation.RoutineWithDays
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.repository.RoutineRepository
import com.sami.setra.data.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImportPreviewData(
    val routine: PortableRoutine,
    val resolutionSummary: ExerciseResolutionSummary
)

class RoutinesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = com.sami.setra.data.local.database.SetraDatabase.getDatabase(application)
    private val repository = RoutineRepository(application, database)
    private val sessionRepository = WorkoutSessionRepository(application, database)
    private val importManager = RoutineImportManager(application, repository, database)
    private val exportManager = RoutineExportManager(application, repository)

    val routines: StateFlow<List<RoutineWithDays>> = repository.getAllActiveRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<WorkoutSessionWithDetails?> = sessionRepository.getActiveWorkoutSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allExercises: StateFlow<List<com.sami.setra.data.local.database.relation.ExerciseWithDetails>> =
        database.exerciseDao().searchExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importPreviewData = MutableStateFlow<ImportPreviewData?>(null)
    val importPreviewData: StateFlow<ImportPreviewData?> = _importPreviewData.asStateFlow()

    private val _importErrorMessage = MutableStateFlow<String?>(null)
    val importErrorMessage: StateFlow<String?> = _importErrorMessage.asStateFlow()

    private val _isProcessingImport = MutableStateFlow(false)
    val isProcessingImport: StateFlow<Boolean> = _isProcessingImport.asStateFlow()

    fun createNewRoutine(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newRoutineId = repository.createNewRoutine()
            onCreated(newRoutineId)
        }
    }

    fun duplicateRoutine(routineId: Long, onDuplicated: (Long) -> Unit) {
        viewModelScope.launch {
            val newRoutineId = repository.duplicateRoutine(routineId)
            if (newRoutineId > 0) {
                onDuplicated(newRoutineId)
            }
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            repository.deleteRoutine(routineId)
        }
    }

    fun loadImportFile(uri: Uri) {
        viewModelScope.launch {
            _isProcessingImport.value = true
            val result = importManager.readAndValidate(uri)
            result.onSuccess { portableRoutine ->
                val summary = importManager.resolveExercises(portableRoutine)
                _importPreviewData.value = ImportPreviewData(portableRoutine, summary)
            }.onFailure { error ->
                _importErrorMessage.value = error.message ?: "Failed to read or validate routine file."
            }
            _isProcessingImport.value = false
        }
    }

    fun clearImportPreview() {
        _importPreviewData.value = null
    }

    fun clearImportError() {
        _importErrorMessage.value = null
    }

    fun mapExercise(originalId: String, mappedExerciseId: String) {
        val current = _importPreviewData.value ?: return
        val updatedItems = current.resolutionSummary.items.map { item ->
            if (item.originalExerciseId == originalId) {
                item.copy(mappedExerciseId = mappedExerciseId, isSkipped = false)
            } else item
        }
        val updatedSummary = ExerciseResolutionSummary(updatedItems)
        _importPreviewData.value = current.copy(resolutionSummary = updatedSummary)
    }

    fun confirmImport(customName: String, summary: ExerciseResolutionSummary, onImported: (Long) -> Unit) {
        val current = _importPreviewData.value ?: return
        viewModelScope.launch {
            _isProcessingImport.value = true
            val result = importManager.importRoutine(
                routine = current.routine,
                resolutionSummary = summary,
                customName = customName
            )
            _isProcessingImport.value = false
            result.onSuccess { importedId ->
                clearImportPreview()
                onImported(importedId)
            }.onFailure { error ->
                _importErrorMessage.value = error.message ?: "Failed to import routine."
            }
        }
    }

    fun prepareRoutineShare(routineId: Long, onReady: (Uri) -> Unit) {
        viewModelScope.launch {
            val result = exportManager.prepareShareUri(routineId)
            result.onSuccess { uri ->
                onReady(uri)
            }.onFailure { error ->
                _importErrorMessage.value = error.message ?: "Failed to export routine for sharing."
            }
        }
    }

    fun exportRoutineToUri(routineId: Long, targetUri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = exportManager.exportToUri(routineId, targetUri)
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _importErrorMessage.value = error.message ?: "Failed to save routine file."
            }
        }
    }

    suspend fun getExportFilename(routineId: Long): String {
        return exportManager.getExportFilename(routineId)
    }
}
