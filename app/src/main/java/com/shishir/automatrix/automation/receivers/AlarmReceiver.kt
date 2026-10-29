package com.shishir.automatrix.automation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shishir.automatrix.AutoMatrixApp
import com.shishir.automatrix.automation.engine.ConditionEvaluator
import com.shishir.automatrix.domain.models.ConditionType

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? AutoMatrixApp ?: return
        val routineId = intent?.getStringExtra("routine_id")

        app.automationEngine.triggerAutomations(
            ConditionEvaluator.TriggerContext(
                triggerType = ConditionType.TIME_RANGE,
                extraData = if (routineId != null) mapOf("routine_id" to routineId) else emptyMap()
            )
        )

        app.automationEngine.triggerAutomations(
            ConditionEvaluator.TriggerContext(
                triggerType = ConditionType.CALENDAR_EVENT
            )
        )

        // Reschedule next check
        app.flowScheduler.scheduleNextEvaluation()
    }
}
