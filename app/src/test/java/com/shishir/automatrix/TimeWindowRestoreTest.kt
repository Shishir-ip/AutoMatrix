package com.shishir.automatrix

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shishir.automatrix.automation.engine.ConditionEvaluator
import android.app.AlarmManager
import com.shishir.automatrix.automation.engine.StateRestorationManager
import com.shishir.automatrix.automation.scheduler.RoutineAlarmScheduler
import com.shishir.automatrix.data.database.FlowDatabase
import com.shishir.automatrix.data.repository.AutomationRepository
import com.shishir.automatrix.domain.models.ActionType
import com.shishir.automatrix.domain.models.AutomationAction
import com.shishir.automatrix.domain.models.AutomationCondition
import com.shishir.automatrix.domain.models.ConditionLogic
import com.shishir.automatrix.domain.models.ConditionType
import com.shishir.automatrix.domain.models.Routine
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Covers the reported scenario: a Time Window routine turns Battery Saver ON
 * at the window start and must turn it back OFF when the window closes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimeWindowRestoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun batterySaverState(): Int =
        android.provider.Settings.Global.getInt(context.contentResolver, "low_power", -1)

    private fun grantWriteSecureSettings() {
        shadowOf(context as android.app.Application).grantPermissions(
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        )
    }

    private fun windowJson(startMinOffset: Int, endMinOffset: Int): String {
        val cal = Calendar.getInstance()
        val start = cal.clone() as Calendar
        start.add(Calendar.MINUTE, startMinOffset)
        val end = cal.clone() as Calendar
        end.add(Calendar.MINUTE, endMinOffset)
        return JSONObject().apply {
            put("startHour", start.get(Calendar.HOUR_OF_DAY))
            put("startMinute", start.get(Calendar.MINUTE))
            put("endHour", end.get(Calendar.HOUR_OF_DAY))
            put("endMinute", end.get(Calendar.MINUTE))
        }.toString()
    }

    private fun restoreRoutine(): Routine {
        val id = "restore_test_${System.nanoTime()}"
        return Routine(
            id = id,
            name = "Battery Saver on",
            isEnabled = true,
            conditionLogic = ConditionLogic.ALL,
            conditions = listOf(
                AutomationCondition(
                    id = "${id}_c1",
                    routineId = id,
                    type = ConditionType.TIME_RANGE,
                    title = "Time Window",
                    summary = "test window",
                    configJson = windowJson(-5, 30)
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "${id}_a1",
                    routineId = id,
                    type = ActionType.SET_BATTERY_SAVER,
                    title = "Battery Saver",
                    summary = "Battery Saver ON",
                    configJson = """{"enable":true}""",
                    restoreOnExit = true
                )
            )
        )
    }

    @Test
    fun `battery saver captures and restores the original state`() {
        grantWriteSecureSettings()
        // Original state: saver OFF
        android.provider.Settings.Global.putInt(context.contentResolver, "low_power", 0)

        StateRestorationManager.captureStateBeforeExecution(
            context, "bs_test", restoreRoutine().actions
        )

        // Routine turns it ON
        android.provider.Settings.Global.putInt(context.contentResolver, "low_power", 1)
        assertEquals(1, batterySaverState())

        // Window ends → restore
        val restored = StateRestorationManager.restoreState(context, "bs_test")
        assertTrue("Restore should find the captured snapshot", restored)
        assertEquals("Battery saver should return to its original state", 0, batterySaverState())
    }

    @Test
    fun `time range with reversible actions schedules start and end alarms`() {
        val database = FlowDatabase.getInstance(context)
        val repository = AutomationRepository(database)
        val routine = restoreRoutine().copy(
            id = "alarm_test_${System.nanoTime()}",
            conditions = listOf(
                AutomationCondition(
                    id = "c1",
                    routineId = "alarm_test",
                    type = ConditionType.TIME_RANGE,
                    title = "Time Window",
                    summary = "",
                    configJson = windowJson(30, 90) // future window
                )
            )
        )
        runBlocking { repository.upsertRoutine(routine) }
        runBlocking {
            repository.getActiveRoutines().forEach { existing ->
                if (existing.id != routine.id) repository.setRoutineEnabled(existing.id, false)
            }
        }

        runBlocking { RoutineAlarmScheduler.rescheduleAll(context) }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarms = shadowOf(alarmManager).scheduledAlarms

        // Other tests share the DB sandbox — count only this routine's alarms
        val ourRoutine = routine.id
        val intents = alarms.mapNotNull { shadowOf(it.operation as android.app.PendingIntent).savedIntent }
        val ours = intents.filter {
            it.getStringExtra(com.shishir.automatrix.automation.scheduler.RoutineAlarmScheduler.EXTRA_ROUTINE_ID) == ourRoutine
        }
        val triggers = ours.mapNotNull { it.getStringExtra(com.shishir.automatrix.automation.scheduler.RoutineAlarmScheduler.EXTRA_TRIGGER) }
        assertTrue(
            "Both START and END alarms should be scheduled (got $triggers)",
            triggers.contains("TIME_RANGE") && triggers.contains("TIME_RANGE_END")
        )
    }
}
