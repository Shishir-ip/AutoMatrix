package com.shishir.automatrix

import android.app.Application
import com.shishir.automatrix.automation.engine.AutomationEngine
import com.shishir.automatrix.automation.scheduler.FlowScheduler
import com.shishir.automatrix.data.database.FlowDatabase
import com.shishir.automatrix.data.repository.AutomationRepository
import com.shishir.automatrix.data.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AutoMatrixApp : Application() {

    lateinit var database: FlowDatabase
        private set
    lateinit var repository: AutomationRepository
        private set
    lateinit var preferenceRepository: PreferenceRepository
        private set
    lateinit var automationEngine: AutomationEngine
        private set
    lateinit var flowScheduler: FlowScheduler
        private set
    lateinit var gestureSensorManager: com.shishir.automatrix.automation.services.FlowGestureSensorManager
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        database = FlowDatabase.getInstance(this)
        repository = AutomationRepository(database)
        preferenceRepository = PreferenceRepository(this)
        automationEngine = AutomationEngine(this, repository)
        flowScheduler = FlowScheduler(this)
        gestureSensorManager = com.shishir.automatrix.automation.services.FlowGestureSensorManager(this)

        applicationScope.launch {
            val isFirstLaunchDone = preferenceRepository.isFirstLaunchDone.first()
            if (!isFirstLaunchDone) {
                repository.initializeDefaultDataIfEmpty()
                preferenceRepository.setFirstLaunchDone(true)
            }
            flowScheduler.scheduleNextEvaluation()
        }

        // Reactively synchronize active focus mode across Lock Screen Ongoing Notification, QS Tile, and Widget
        applicationScope.launch {
            repository.allModes.collect { modes ->
                val activeMode = modes.firstOrNull { it.isActive }
                com.shishir.automatrix.automation.engine.FlowModeController.syncSystemIntegrations(this@AutoMatrixApp, activeMode)
            }
        }

        // Reactively manage gesture sensor listener
        applicationScope.launch {
            kotlinx.coroutines.flow.combine(
                preferenceRepository.isFlipToShhhEnabled,
                preferenceRepository.isShakeTriggerEnabled
            ) { flipEnabled, shakeEnabled ->
                Pair(flipEnabled, shakeEnabled)
            }.collect { (flipEnabled, shakeEnabled) ->
                gestureSensorManager.isFlipToShhhEnabled = flipEnabled
                gestureSensorManager.isShakeEnabled = shakeEnabled
                if (flipEnabled || shakeEnabled) {
                    gestureSensorManager.start()
                } else {
                    gestureSensorManager.stop()
                }
            }
        }
    }
}
