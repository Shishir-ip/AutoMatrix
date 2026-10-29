package com.shishir.automatrix.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shishir.automatrix.AutoMatrixApp
import com.shishir.automatrix.automation.engine.ConditionEvaluator
import com.shishir.automatrix.data.backup.BackupManager
import com.shishir.automatrix.domain.models.ConditionType
import com.shishir.automatrix.domain.models.ExecutionLog
import com.shishir.automatrix.domain.models.Mode
import com.shishir.automatrix.domain.models.Routine
import com.shishir.automatrix.permissions.PermissionItem
import com.shishir.automatrix.permissions.PermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RoutineSortOrder {
    FAVORITES_FIRST,
    ALPHABETICAL,
    LAST_TRIGGERED,
    PRIORITY
}

class FlowViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AutoMatrixApp
    private val repository = app.repository
    private val prefRepo = app.preferenceRepository
    private val engine = app.automationEngine

    // Search and filter state
    val routineSearchQuery = MutableStateFlow("")
    val routineFilterFavorites = MutableStateFlow(false)
    val routineSortOrder = MutableStateFlow(RoutineSortOrder.FAVORITES_FIRST)

    // User Feedback Snackbars / Toast events
    private val _userFeedback = MutableSharedFlow<String>()
    val userFeedback: SharedFlow<String> = _userFeedback.asSharedFlow()

    // Permissions state
    private val _permissions = MutableStateFlow<List<PermissionItem>>(emptyList())
    val permissions: StateFlow<List<PermissionItem>> = _permissions.asStateFlow()

    // Theme and preferences
    val themeMode: StateFlow<String> = prefRepo.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val isDeveloperMode: StateFlow<Boolean> = prefRepo.isDeveloperMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isFlipToShhhEnabled: StateFlow<Boolean> = prefRepo.isFlipToShhhEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isShakeTriggerEnabled: StateFlow<Boolean> = prefRepo.isShakeTriggerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isCalendarTriggerEnabled: StateFlow<Boolean> = prefRepo.isCalendarTriggerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** null = still loading from disk; false = onboarding pending; true = seen */
    val isOnboardingDone: StateFlow<Boolean?> = prefRepo.isOnboardingDone
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val phoneOrientation: StateFlow<com.shishir.automatrix.automation.services.PhoneOrientation> = app.gestureSensorManager.currentOrientation

    // Raw sources from repository
    val rawRoutines: StateFlow<List<Routine>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val modes: StateFlow<List<Mode>> = repository.allModes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val executionLogs: StateFlow<List<ExecutionLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted Routines
    val filteredRoutines: StateFlow<List<Routine>> = combine(
        rawRoutines,
        routineSearchQuery,
        routineFilterFavorites,
        routineSortOrder
    ) { routines, query, onlyFavs, sortOrder ->
        var list = routines

        if (onlyFavs) {
            list = list.filter { it.isFavorite }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.conditions.any { c -> c.title.lowercase().contains(q) || c.summary.lowercase().contains(q) } ||
                        it.actions.any { a -> a.title.lowercase().contains(q) || a.summary.lowercase().contains(q) }
            }
        }

        when (sortOrder) {
            RoutineSortOrder.FAVORITES_FIRST -> list.sortedWith(compareByDescending<Routine> { it.isFavorite }.thenByDescending { it.createdAt })
            RoutineSortOrder.ALPHABETICAL -> list.sortedBy { it.name.lowercase() }
            RoutineSortOrder.LAST_TRIGGERED -> list.sortedByDescending { it.lastTriggeredTime ?: 0L }
            RoutineSortOrder.PRIORITY -> list.sortedByDescending { it.priority.level }
        }
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        _permissions.value = PermissionManager.getAllPermissions(getApplication())
    }

    fun toggleRoutineEnabled(routine: Routine) {
        viewModelScope.launch {
            val newState = !routine.isEnabled
            repository.setRoutineEnabled(routine.id, newState)
            _userFeedback.emit("${routine.name} ${if (newState) "enabled" else "disabled"}")
        }
    }

    fun toggleRoutineFavorite(routine: Routine) {
        viewModelScope.launch {
            val newState = !routine.isFavorite
            repository.setRoutineFavorite(routine.id, newState)
        }
    }

    fun duplicateRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.duplicateRoutine(routine)
            _userFeedback.emit("Duplicated \"${routine.name}\"")
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            repository.deleteRoutine(routineId)
            _userFeedback.emit("Routine deleted")
        }
    }

    fun runRoutineNow(routine: Routine) {
        viewModelScope.launch {
            val log = engine.executeRoutineDirectly(routine)
            if (log.isSuccess) {
                _userFeedback.emit("\u2713 ${routine.name} executed successfully")
            } else {
                _userFeedback.emit(log.errorMessage ?: "Routine execution completed with notes")
            }
        }
    }

    fun toggleModeActive(mode: Mode) {
        viewModelScope.launch {
            val targetState = !mode.isActive
            repository.setModeActive(mode.id, targetState)

            if (targetState) {
                // Execute actions assigned to this mode
                for (action in mode.actions) {
                    com.shishir.automatrix.automation.engine.ActionExecutor.executeAction(getApplication(), action)
                }
                repository.recordExecutionLog(
                    ExecutionLog(
                        routineId = mode.id,
                        routineName = "${mode.name} Mode",
                        triggerName = "Manual Mode Activation",
                        timestamp = System.currentTimeMillis(),
                        isSuccess = true,
                        conditionsSummary = "Mode switched to ACTIVE",
                        actionsSummary = mode.actions.joinToString("\n") { "\u2713 ${it.title}" },
                        resultSummary = "Mode profile activated"
                    )
                )
                _userFeedback.emit("${mode.name} Mode Activated")
            } else {
                _userFeedback.emit("${mode.name} Mode Deactivated")
            }
        }
    }

    fun saveRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.upsertRoutine(routine)
            _userFeedback.emit("Routine saved successfully")
        }
    }

    fun exportBackup(): String {
        return BackupManager.exportRoutinesToJson(rawRoutines.value)
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            when (val result = BackupManager.importRoutinesFromJson(jsonString)) {
                is BackupManager.ImportResult.Success -> {
                    result.routines.forEach { r ->
                        repository.upsertRoutine(r)
                    }
                    _userFeedback.emit("Imported ${result.routines.size} routines (disabled for your review)")
                }
                is BackupManager.ImportResult.Error -> {
                    _userFeedback.emit(result.message)
                }
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _userFeedback.emit("Execution logs cleared")
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefRepo.setThemeMode(mode)
        }
    }

    fun setDeveloperMode(enabled: Boolean) {
        viewModelScope.launch {
            prefRepo.setDeveloperMode(enabled)
        }
    }

    fun setFlipToShhhEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefRepo.setFlipToShhhEnabled(enabled)
            _userFeedback.emit("Flip-to-Shhh ${if (enabled) "enabled" else "disabled"}")
        }
    }

    fun setShakeTriggerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefRepo.setShakeTriggerEnabled(enabled)
            _userFeedback.emit("Shake trigger ${if (enabled) "enabled" else "disabled"}")
        }
    }

    fun setCalendarTriggerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefRepo.setCalendarTriggerEnabled(enabled)
            _userFeedback.emit("Calendar integration ${if (enabled) "enabled" else "disabled"}")
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            prefRepo.setOnboardingDone(true)
        }
    }

    fun handleNfcTagScanned(tagId: String?, payload: String?) {
        viewModelScope.launch {
            val actualPayload = payload ?: ""
            val matchedMode = when {
                actualPayload.contains("mode_work", ignoreCase = true) -> "mode_work"
                actualPayload.contains("mode_study", ignoreCase = true) -> "mode_study"
                actualPayload.contains("mode_sleep", ignoreCase = true) -> "mode_sleep"
                actualPayload.contains("mode_driving", ignoreCase = true) -> "mode_driving"
                else -> null
            }

            if (matchedMode != null) {
                com.shishir.automatrix.automation.engine.FlowModeController.activateMode(getApplication(), matchedMode)
                _userFeedback.emit("NFC tag scanned: mode activated")
            } else {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = ConditionType.NFC_TAG_SCANNED,
                        extraData = mapOf(
                            "tagId" to (tagId ?: "unknown"),
                            "payload" to actualPayload
                        )
                    )
                )
                _userFeedback.emit("NFC tag scanned (${tagId ?: "Tag"})")
            }
        }
    }

    fun simulateNfcTap(modeId: String) {
        viewModelScope.launch {
            com.shishir.automatrix.automation.engine.FlowModeController.activateMode(getApplication(), modeId)
            _userFeedback.emit("Simulated NFC tag tap: mode activated")
        }
    }

    fun testSimulatedTrigger(type: ConditionType) {
        viewModelScope.launch {
            engine.triggerAutomations(
                ConditionEvaluator.TriggerContext(
                    triggerType = type
                )
            )
            _userFeedback.emit("Broadcasted test trigger: ${type.displayName}")
        }
    }
}
