package com.shishir.automatrix

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shishir.automatrix.automation.scheduler.RoutineAlarmScheduler
import com.shishir.automatrix.data.database.FlowDatabase
import com.shishir.automatrix.data.repository.AutomationRepository
import com.shishir.automatrix.domain.models.AutomationCondition
import com.shishir.automatrix.domain.models.ConditionLogic
import com.shishir.automatrix.domain.models.ConditionType
import com.shishir.automatrix.domain.models.Routine
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Verifies that time-based routines get exact per-routine alarms scheduled —
 * the mechanism that makes Specific Time / Time Window triggers fire on time
 * regardless of the periodic sweep.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoutineAlarmSchedulerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun timeSpecificRoutine(): Routine {
        val id = "alarm_test_${System.nanoTime()}"
        return Routine(
            id = id,
            name = "Scheduled at time",
            isEnabled = true,
            conditionLogic = ConditionLogic.ALL,
            conditions = listOf(
                AutomationCondition(
                    id = "${id}_c1",
                    routineId = id,
                    type = ConditionType.TIME_SPECIFIC,
                    title = "Specific Time",
                    summary = "08:00 AM",
                    configJson = JSONObject().apply {
                        put("hour", 8); put("minute", 30)
                    }.toString()
                )
            ),
            actions = emptyList()
        )
    }

    @Test
    fun `rescheduleAll schedules an exact alarm for a time specific routine`() {
        val database = FlowDatabase.getInstance(context)
        val repository = AutomationRepository(database)
        val routine = timeSpecificRoutine()

        runBlocking { repository.upsertRoutine(routine) }

        // Disable everything else the DB might hold from other tests
        runBlocking {
            repository.getActiveRoutines().forEach { existing ->
                if (existing.id != routine.id) repository.setRoutineEnabled(existing.id, false)
            }
        }

        runBlocking { RoutineAlarmScheduler.rescheduleAll(context) }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadow = shadowOf(alarmManager)
        val scheduled = shadow.nextScheduledAlarm
            ?: throw AssertionError("An alarm should have been scheduled")

        // The trigger time must be in the future and within ~24h (next 08:30)
        val now = System.currentTimeMillis()
        assertTrue(
            "Alarm should fire within the next 24h (next occurrence of 08:30)",
            scheduled.triggerAtTime in now..(now + 25 * 60 * 60 * 1000L)
        )
        assertEquals("Exactly one alarm should be scheduled", 1, shadow.scheduledAlarms.size)
    }
}

object RoutineAlarmSchedulerTestRunner {
    fun run(block: () -> Unit) = block()
}
