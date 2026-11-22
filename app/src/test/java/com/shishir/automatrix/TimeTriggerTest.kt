package com.shishir.automatrix

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shishir.automatrix.automation.engine.ConditionEvaluator
import com.shishir.automatrix.automation.engine.AutomationEngine
import com.shishir.automatrix.data.database.FlowDatabase
import com.shishir.automatrix.domain.models.ActionType
import com.shishir.automatrix.domain.models.AutomationAction
import com.shishir.automatrix.domain.models.AutomationCondition
import com.shishir.automatrix.domain.models.ConditionLogic
import com.shishir.automatrix.domain.models.ConditionType
import com.shishir.automatrix.domain.models.Routine
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Calendar

/**
 * Reproduces the reported failure: a routine with a Time Window condition and
 * a Battery Saver action must fire when a TIME_RANGE sweep arrives.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimeTriggerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun nowWindow(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        val start = cal.clone() as Calendar
        start.add(Calendar.MINUTE, -5)
        val end = cal.clone() as Calendar
        end.add(Calendar.MINUTE, 30)
        return Triple(start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), 0)
    }

    private fun batterySaverRoutine(windowJson: String): Routine {
        val id = "test_routine_${System.nanoTime()}"
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
                    configJson = windowJson
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "${id}_a1",
                    routineId = id,
                    type = ActionType.SET_BATTERY_SAVER,
                    title = "Battery Saver",
                    summary = "Battery Saver ON",
                    configJson = """{"enable":true}"""
                )
            )
        )
    }

    private fun grantWriteSecureSettings() {
        shadowOf(context as android.app.Application).grantPermissions(
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        )
    }

    private fun batterySaverState(): Int =
        android.provider.Settings.Global.getInt(
            context.contentResolver, "low_power", -1
        )

    @Test
    fun `evaluator matches an active time window`() {
        val (sh, sm, _) = nowWindow()
        val config = JSONObject().apply {
            put("startHour", sh); put("startMinute", sm)
            put("endHour", (sh + 1) % 24); put("endMinute", sm)
        }.toString()
        val condition = AutomationCondition(
            id = "c", routineId = "r", type = ConditionType.TIME_RANGE,
            title = "Time Window", summary = "", configJson = config
        )
        val (matched, desc) = ConditionEvaluator.evaluateRoutineConditions(
            context, listOf(condition), ConditionLogic.ALL,
            ConditionEvaluator.TriggerContext(triggerType = ConditionType.TIME_RANGE)
        )
        assertTrue("Active window should match: $desc", matched)
    }

    @Test
    fun `evaluator matches specific time when alarm carries routine id`() {
        val cal = Calendar.getInstance()
        val condition = AutomationCondition(
            id = "c", routineId = "r", type = ConditionType.TIME_SPECIFIC,
            title = "Specific Time", summary = "",
            configJson = JSONObject().apply {
                put("hour", cal.get(Calendar.HOUR_OF_DAY))
                put("minute", cal.get(Calendar.MINUTE))
            }.toString()
        )
        val (matched, _) = ConditionEvaluator.evaluateRoutineConditions(
            context, listOf(condition), ConditionLogic.ALL,
            ConditionEvaluator.TriggerContext(
                triggerType = ConditionType.TIME_SPECIFIC,
                extraData = mapOf("routine_id" to "r")
            )
        )
        assertTrue("Exact-alarm firing should match its own routine", matched)
    }

    @Test
    fun `evaluator matches today for days of week`() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val condition = AutomationCondition(
            id = "c", routineId = "r", type = ConditionType.DAYS_OF_WEEK,
            title = "Days", summary = "",
            configJson = JSONObject().apply { put("days", org.json.JSONArray().put(today)) }.toString()
        )
        val (matched, _) = ConditionEvaluator.evaluateRoutineConditions(
            context, listOf(condition), ConditionLogic.ALL,
            ConditionEvaluator.TriggerContext(triggerType = ConditionType.DAYS_OF_WEEK)
        )
        assertTrue("Today should match the days condition", matched)
    }

    @Test
    fun `engine executes battery saver for an in-window time range sweep`() {
        grantWriteSecureSettings()
        val database = FlowDatabase.getInstance(context)
        val repository = com.shishir.automatrix.data.repository.AutomationRepository(database)
        val engine = AutomationEngine(context, repository)

        val (sh, sm, _) = nowWindow()
        val windowJson = JSONObject().apply {
            put("startHour", sh); put("startMinute", sm)
            put("endHour", (sh + 1) % 24); put("endMinute", sm)
        }.toString()
        val routine = batterySaverRoutine(windowJson)
        kotlinx.coroutines.runBlocking {
            repository.upsertRoutine(routine)
        }

        assertEquals(-1, batterySaverState())

        engine.triggerAutomations(
            ConditionEvaluator.TriggerContext(triggerType = ConditionType.TIME_RANGE)
        )

        // The engine is fire-and-forget; poll briefly for the side effect.
        var waited = 0L
        while (batterySaverState() != 1 && waited < 10_000) {
            Thread.sleep(200); waited += 200
        }
        assertEquals("Battery saver should have been turned ON by the sweep", 1, batterySaverState())
    }
}
