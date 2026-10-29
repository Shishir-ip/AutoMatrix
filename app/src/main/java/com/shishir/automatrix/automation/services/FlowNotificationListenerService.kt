package com.shishir.automatrix.automation.services

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.shishir.automatrix.AutoMatrixApp
import com.shishir.automatrix.automation.engine.ConditionEvaluator
import com.shishir.automatrix.domain.models.ConditionType
import com.shishir.automatrix.domain.models.InterceptedNotification
import java.util.UUID

class FlowNotificationListenerService : NotificationListenerService() {

    /**
     * Chat apps (WhatsApp, Telegram, etc.) do not post a new notification per
     * message — they update ONE notification, so onNotificationPosted fires
     * repeatedly for the same key. Identical re-posts within this window are
     * ignored to avoid duplicate routine executions, while genuinely new
     * message content still matches.
     */
    private val recentFingerprints = HashMap<String, Long>()
    private val fingerprintWindowMs = 2500L

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: ""
        // Ignore internal notifications from AutoMatrix itself to prevent feedback loops
        if (pkg == packageName) return

        // Group summaries duplicate the content of their children and would
        // cause double evaluation — the child notification is always posted too.
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val (title, text, subText) = extractNotificationText(sbn)
        val combined = "$title $text $subText".trim()

        // Skip system re-posts of byte-identical content within a short window
        val fingerprint = "${sbn.key}|$combined"
        val now = System.currentTimeMillis()
        synchronized(recentFingerprints) {
            recentFingerprints.entries.removeAll { now - it.value > fingerprintWindowMs }
            if (recentFingerprints.containsKey(fingerprint)) return
            recentFingerprints[fingerprint] = now
        }

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
            timestamp = now
        )

        // Record into the in-memory hub for quick inspection
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
        if (sbn != null) {
            synchronized(recentFingerprints) {
                recentFingerprints.keys.removeAll { it.startsWith("${sbn.key}|") }
            }
        }
    }

    /**
     * Extracts (title, text, subText) across every notification style:
     * plain text, BigTextStyle, MessagingStyle (chat apps) and ticker text.
     */
    private fun extractNotificationText(sbn: StatusBarNotification): Triple<String, String, String> {
        val extras = sbn.notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""

        var subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            ?: ""

        // 1. MessagingStyle — chat apps put messages here and leave EXTRA_TEXT
        //    empty once the notification has more than one message.
        val messages = extractMessageTexts(sbn.notification, extras)
        if (messages.isNotEmpty()) {
            val senderNames = extractSenderNames(sbn.notification, extras)
            if (senderNames.isNotBlank() && subText.isBlank()) {
                subText = senderNames
            }
            return Triple(title, messages.joinToString("\n"), subText)
        }

        // 2. CharSequence or CharSequence[] in EXTRA_TEXT
        val text = when (val raw = extras.get(Notification.EXTRA_TEXT)) {
            is CharSequence -> raw.toString()
            is Array<*> -> raw.filterIsInstance<CharSequence>().joinToString("\n")
            else -> ""
        }
        if (text.isNotBlank()) {
            return Triple(title, text, subText)
        }

        // 3. BigTextStyle and ticker fallbacks
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val ticker = sbn.notification.tickerText?.toString() ?: ""
        return Triple(title, if (bigText.isNotBlank()) bigText else ticker, subText)
    }

    /** Newest-last list of message bodies from MessagingStyle notifications. */
    private fun extractMessageTexts(
        notification: Notification,
        extras: android.os.Bundle
    ): List<String> {
        val texts = mutableListOf<String>()

        // EXTRA_MESSAGES (public since API 28; the same key is written by the
        // platform on older versions too). Elements are MessagingStyle.Message.
        val parcelables = try {
            extras.getParcelableArray(Notification.EXTRA_MESSAGES)
                ?: (extras.get("android.messages") as? Array<*>)
        } catch (_: Exception) {
            null
        }
        parcelables?.forEach { parcelable ->
            val text = when (parcelable) {
                is Notification.MessagingStyle.Message -> parcelable.text?.toString()
                is android.os.Bundle -> parcelable.getString("text")
                else -> null
            }
            if (!text.isNullOrBlank()) texts.add(text)
        }
        return texts
    }

    /** Sender names from MessagingStyle messages, used as sub text context. */
    private fun extractSenderNames(
        notification: Notification,
        extras: android.os.Bundle
    ): String {
        val names = mutableListOf<String>()
        try {
            val parcelables = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
                ?: (extras.get("android.messages") as? Array<*>)
            parcelables?.forEach { parcelable ->
                val name = when (parcelable) {
                    is Notification.MessagingStyle.Message -> parcelable.sender?.toString()
                    is android.os.Bundle -> parcelable.getString("sender")
                    else -> null
                }
                if (!name.isNullOrBlank() && !names.contains(name)) names.add(name)
            }
        } catch (_: Exception) {
            // Sender names are cosmetic; ignore extraction failures
        }
        return names.joinToString(", ")
    }
}
