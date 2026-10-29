package com.shishir.automatrix.automation.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.shishir.automatrix.AutoMatrixApp
import com.shishir.automatrix.automation.engine.ConditionEvaluator
import com.shishir.automatrix.domain.models.ConditionType

class FlowAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkgName = event.packageName?.toString() ?: return
            val app = applicationContext as? AutoMatrixApp ?: return
            app.automationEngine.triggerAutomations(
                ConditionEvaluator.TriggerContext(
                    triggerType = ConditionType.APP_OPENED,
                    extraData = mapOf("packageName" to pkgName)
                )
            )
        }
    }

    override fun onInterrupt() {
        // Required callback for accessibility interruptions
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        var instance: FlowAccessibilityService? = null
            private set
    }
}
