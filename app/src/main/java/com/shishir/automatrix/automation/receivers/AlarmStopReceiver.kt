package com.shishir.automatrix.automation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shishir.automatrix.automation.engine.ActionExecutor

/** Stops the emergency alarm when the user taps "Stop" on its notification. */
class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        ActionExecutor.stopEmergencyAlarm(context)
    }
}
