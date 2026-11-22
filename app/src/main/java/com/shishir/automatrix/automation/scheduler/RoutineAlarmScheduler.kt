package com.shishir.automatrix.automation.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shishir.automatrix.AutoMatrixApp
import com.shishir.automatrix.automation.receivers.AlarmReceiver
import com.shishir.automatrix.domain.models.ConditionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar

/**
 * Schedules exact per-routine alarms for time-based triggers so they fire at
 * the precise moment instead of waiting for the 15-minute evaluation sweep:
 *
 *  - TIME_SPECIFIC conditions  → alarm at the target clock time
 *  - TIME_RANGE conditions     → alarm at the window's start time
 *
 * The periodic sweep still runs as a safety net (it re-arms everything and
 * covers calendar + ongoing windows). Call [rescheduleAll] whenever routines
 * change, on boot, and after any of these alarms fires (rolls to the next day).
 */
object RoutineAlarmScheduler {

    private const val PREFS = "automatrix_routine_alarms"
    private const val SCHEDULED_KEY = "scheduled"
    const val EXTRA_ROUTINE_ID = "routine_id"
    const val EXTRA_TRIGGER = "trigger_type"

    suspend fun rescheduleAll(context: Context) = withContext(Dispatchers.Default) {
        val app = context.applicationContext as? AutoMatrixApp ?: return@withContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val scheduled = prefs.getStringSet(SCHEDULED_KEY, emptySet()) ?: emptySet()

        // Cancel everything previously scheduled under the managed keys
        scheduled.forEach { key ->
            val parts = key.split("|")
            if (parts.size == 3) {
                val pi = buildPendingIntent(context, parts[0], parts[1])
                try {
                    alarmManager?.cancel(pi)
                } catch (_: Exception) {
                }
            }
        }

        val newKeys = mutableSetOf<String>()
        try {
            val routines = app.repository.getActiveRoutines()
            val now = System.currentTimeMillis()

            fun nextOccurrence(hour: Int, minute: Int): Long {
                val cal = Calendar.getInstance()
                cal.timeInMillis = now
                cal.set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
                cal.set(Calendar.MINUTE, minute.coerceIn(0, 59))
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
                return cal.timeInMillis
            }

            fun currentMinutes(): Int {
                val cal = Calendar.getInstance()
                return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            }

            routines.forEach { routine ->
                routine.conditions.forEach { condition ->
                    when (condition.type) {
                        ConditionType.TIME_SPECIFIC, ConditionType.TIME_RANGE -> {
                            val config = try {
                                JSONObject(condition.configJson.ifEmpty { "{}" })
                            } catch (_: Exception) {
                                JSONObject()
                            }

                            if (condition.type == ConditionType.TIME_SPECIFIC) {
                                val triggerAt = nextOccurrence(
                                    config.optInt("hour", 8),
                                    config.optInt("minute", 0)
                                )
                                val key = "${routine.id}|TIME_SPECIFIC|$triggerAt"
                                scheduleExact(context, key, routine.id, "TIME_SPECIFIC", triggerAt)
                                newKeys.add(key)
                            } else {
                                val startTotal = config.optInt("startHour", 0) * 60 +
                                        config.optInt("startMinute", 0)
                                val endTotal = config.optInt("endHour", 23) * 60 +
                                        config.optInt("endMinute", 59)

                                // Skip the start alarm while the window is already
                                // open — the periodic sweep keeps it covered.
                                val nowMins = currentMinutes()
                                val inWindow = if (startTotal <= endTotal) {
                                    nowMins in startTotal..endTotal
                                } else {
                                    nowMins >= startTotal || nowMins <= endTotal
                                }

                                if (!inWindow) {
                                    val triggerAt = nextOccurrence(
                                        config.optInt("startHour", 0),
                                        config.optInt("startMinute", 0)
                                    )
                                    val key = "${routine.id}|TIME_RANGE|$triggerAt"
                                    scheduleExact(context, key, routine.id, "TIME_RANGE", triggerAt)
                                    newKeys.add(key)
                                }

                                // Restore alarm at window end — only when the
                                // routine asked for its state to be reverted
                                if (routine.actions.any { it.restoreOnExit }) {
                                    val endAt = nextOccurrence(
                                        config.optInt("endHour", 23),
                                        config.optInt("endMinute", 59)
                                    )
                                    val endKey = "${routine.id}|TIME_RANGE_END|$endAt"
                                    scheduleExact(context, endKey, routine.id, "TIME_RANGE_END", endAt)
                                    newKeys.add(endKey)
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        } catch (_: Exception) {
            // Scheduling must never crash the caller; the periodic sweep is the fallback
        }

        prefs.edit().putStringSet(SCHEDULED_KEY, newKeys).apply()
    }

    private fun scheduleExact(
        context: Context,
        key: String,
        routineId: String,
        triggerName: String,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pi = buildPendingIntent(context, routineId, triggerName)
        try {
            val canBeExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager?.canScheduleExactAlarms() ?: false
            } else {
                true
            }
            if (canBeExact) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pi
                )
            } else {
                alarmManager?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pi
                )
            }
        } catch (_: SecurityException) {
            try {
                alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } catch (_: Exception) {
            }
        }
    }

    private fun buildPendingIntent(
        context: Context,
        routineId: String,
        triggerName: String
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ROUTINE_ID, routineId)
            putExtra(EXTRA_TRIGGER, triggerName)
        }
        return PendingIntent.getBroadcast(
            context,
            (routineId + triggerName).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
