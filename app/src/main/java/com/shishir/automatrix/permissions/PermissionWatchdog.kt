package com.shishir.automatrix.permissions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.shishir.automatrix.MainActivity
import com.shishir.automatrix.R
import com.shishir.automatrix.domain.models.ActionType
import com.shishir.automatrix.domain.models.ConditionType
import com.shishir.automatrix.domain.models.Routine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Watches the permissions that *enabled* automations actually depend on and
 * notifies the user when one is missing — most importantly when accessibility
 * gets turned off (some manufacturers disable it on reboot, which silently
 * breaks app-launch triggers and navigation actions).
 *
 * Only relevant checks run: a routine that never uses app detection will not
 * nag about accessibility. Each missing permission is reported at most once
 * per day so the watchdog stays quiet but persistent.
 */
object PermissionWatchdog {

    private const val CHANNEL_ID = "automatrix_watchdog"
    private const val PREFS = "automatrix_watchdog"
    private const val REPEAT_INTERVAL_MS = 24 * 60 * 60 * 1000L

    private val scope = CoroutineScope(Dispatchers.Default)
    private val running = Mutex()

    fun checkAndNotify(context: Context, routines: List<Routine>) {
        scope.launch {
            running.withLock {
                runCatching { check(context, routines) }
            }
        }
    }

    /** Boot-time variant: the DB may still be opening, so fetch routines in the watchdog's own scope. */
    fun checkAfterBoot(context: Context, repository: com.shishir.automatrix.data.repository.AutomationRepository) {
        scope.launch {
            running.withLock {
                runCatching {
                    check(context, repository.getActiveRoutines())
                }
            }
        }
    }

    private suspend fun check(context: Context, routines: List<Routine>) {
        val enabled = routines.filter { it.isEnabled }
        if (enabled.isEmpty()) return

        val usesAppDetection = enabled.any { routine ->
            routine.conditions.any { it.type == ConditionType.APP_OPENED } ||
                    routine.actions.any { it.type == ActionType.ACCESSIBILITY_NAV }
        }
        val usesNotifications = enabled.any { routine ->
            routine.conditions.any {
                it.type == ConditionType.NOTIFICATION_RECEIVED ||
                        it.type == ConditionType.DND_ON ||
                        it.type == ConditionType.DND_OFF
            }
        }
        val usesCalendar = enabled.any { it.conditions.any { c -> c.type == ConditionType.CALENDAR_EVENT } }
        val usesWriteSettings = enabled.any { it.actions.any { a -> a.type == ActionType.SET_BRIGHTNESS } }
        val usesDndControl = enabled.any { it.actions.any { a -> a.type == ActionType.SET_DND_MODE } }

        val missing = buildList {
            if (usesAppDetection && !isAccessibilityEnabled(context)) {
                add("Accessibility Access is OFF — app-launch triggers and Back/Home actions cannot run")
            }
            if (usesNotifications && !isNotificationListenerEnabled(context)) {
                add("Notification Access is OFF — notification triggers cannot read notifications")
            }
            if (usesCalendar && !isCalendarGranted(context)) {
                add("Calendar permission is OFF — calendar triggers cannot read events")
            }
            if (usesWriteSettings && !Settings.System.canWrite(context)) {
                add("Modify System Settings is OFF — brightness actions cannot run")
            }
            if (usesDndControl && !isDndAccessGranted(context)) {
                add("Do Not Disturb access is OFF — DND actions cannot run")
            }
        }

        if (missing.isEmpty()) return
        notifyMissing(context, missing)
    }

    private fun notifyMissing(context: Context, problems: List<String>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val key = "last_notify_" + problems.hashCode()
        val last = prefs.getLong(key, 0L)
        if (now - last < REPEAT_INTERVAL_MS) return
        prefs.edit().putLong(key, now).apply()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Permission Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Warns when automations cannot run because a permission is off"
                }
            )
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (problems.size == 1) "AutoMatrix needs a permission" else "AutoMatrix permissions need attention"
        val text = problems.joinToString("\n")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_flow_tile)
            .setContentTitle(title)
            .setContentText(problems.first())
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(2001, notification)
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        return try {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            enabled.contains("${context.packageName}/", ignoreCase = true)
        } catch (_: Exception) {
            true // don't nag on check failures
        }
    }

    private fun isNotificationListenerEnabled(context: Context): Boolean {
        return try {
            androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        } catch (_: Exception) {
            true
        }
    }

    private fun isCalendarGranted(context: Context): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun isDndAccessGranted(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return nm?.isNotificationPolicyAccessGranted ?: true
    }
}
