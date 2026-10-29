package com.shishir.automatrix.automation.services

import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.shishir.automatrix.AutoMatrixApp
import com.shishir.automatrix.automation.engine.ConditionEvaluator
import com.shishir.automatrix.domain.models.ConditionType
import com.shishir.automatrix.domain.models.InterceptedNotification
import java.util.UUID

class FlowNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: ""
        // Ignore internal notifications from AutoMatrix itself to prevent feedback loops
        if (pkg == packageName) return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val subText = extras.getCharSequence("android.subText")?.toString() ?: ""
        val combined = "$title $text $subText".trim()

        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkg
        }

        val intercepted = InterceptedNotification(
            id = UUID.randomUUID().toString(),
            packageName = pkg,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            timestamp = System.currentTimeMillis()
        )

        // Record into the in-memory hub for debugger and quick-creation
        NotificationEventHub.recordNotification(intercepted)

        val app = applicationContext as? AutoMatrixApp ?: return
        app.automationEngine.triggerAutomations(
            ConditionEvaluator.TriggerContext(
                triggerType = ConditionType.NOTIFICATION_RECEIVED,
                extraData = mapOf(
                    "notification" to intercepted,
                    "packageName" to pkg,
                    "appName" to appName,
                    "title" to title,
                    "text" to text,
                    "subText" to subText,
                    "notification_text" to combined
                )
            )
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}

