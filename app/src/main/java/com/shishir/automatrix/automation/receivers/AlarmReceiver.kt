package com.shishir.automatrix.automation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shishir.automatrix.AutoMatrixApp
import com.shishir.automatrix.automation.engine.ConditionEvaluator
import com.shishir.automatrix.automation.scheduler.FlowScheduler
import com.shishir.automatrix.automation.scheduler.RoutineAlarmScheduler
import com.shishir.automatrix.domain.models.ConditionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? AutoMatrixApp ?: return
        val engine = app.automationEngine

        // 1. Exact per-routine alarm (time-based triggers fire precisely here)
        val routineId = intent?.getStringExtra(RoutineAlarmScheduler.EXTRA_ROUTINE_ID)
        val triggerName = intent?.getStringExtra(RoutineAlarmScheduler.EXTRA_TRIGGER)

        if (routineId != null && triggerName == "TIME_RANGE_END") {
            // The time window closed — revert actions flagged Restore-on-Exit
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val restored = engine.restoreRoutineState(routineId)
                    val routine = runCatching {
                        app.repository.getRoutineById(routineId)
                    }.getOrNull()
                    if (restored) {
                        app.repository.recordExecutionLog(
                            com.shishir.automatrix.domain.models.ExecutionLog(
                                routineId = routineId,
                                routineName = routine?.name ?: "Routine",
                                triggerName = "Time Window Ended",
                                timestamp = System.currentTimeMillis(),
                                isSuccess = true,
                                conditionsSummary = "Time window closed",
                                actionsSummary = "Restored pre-routine device state",
                                resultSummary = "Window ended — automatic revert applied"
                            )
                        )
                    }
                    RoutineAlarmScheduler.rescheduleAll(context)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (routineId != null && triggerName != null) {
            val triggerType = try {
                ConditionType.valueOf(triggerName)
            } catch (_: Exception) {
                null
            }
            if (triggerType != null) {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = triggerType,
                        extraData = mapOf("routine_id" to routineId)
                    )
                )
            }
        } else {
            // 2. Periodic evaluation sweeps
            engine.triggerAutomations(
                ConditionEvaluator.TriggerContext(
                    triggerType = ConditionType.TIME_RANGE
                )
            )
            engine.triggerAutomations(
                ConditionEvaluator.TriggerContext(
                    triggerType = ConditionType.CALENDAR_EVENT
                )
            )
        }

        // 3. Re-arm the periodic sweep (routine alarms re-arm inside the
        // TIME_RANGE_END branch above; the sweep path only needs the sweep)
        app.flowScheduler.scheduleNextEvaluation()
    }
}
