package com.shishir.automatrix.automation.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.shishir.automatrix.AutoMatrixApp
import com.shishir.automatrix.MainActivity
import com.shishir.automatrix.R
import com.shishir.automatrix.automation.engine.FlowModeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlowWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_MODE = "com.shishir.automatrix.action.WIDGET_TOGGLE_MODE"
        const val ACTION_DEACTIVATE_ALL = "com.shishir.automatrix.action.WIDGET_DEACTIVATE_ALL"
        const val EXTRA_MODE_ID = "extra_mode_id"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, FlowWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, FlowWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as? AutoMatrixApp ?: return
        CoroutineScope(Dispatchers.Default).launch {
            val modes = app.repository.allModes.first()
            val activeMode = modes.firstOrNull { it.isActive }

            withContext(Dispatchers.Main) {
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.flow_widget_layout)

                    // Click header to open App
                    val appIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val appPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        appIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_header, appPendingIntent)

                    // Bind Status Text & Subtext
                    if (activeMode != null) {
                        views.setTextViewText(R.id.widget_status_badge, "● ${activeMode.name} Active")
                        views.setTextViewText(R.id.widget_subtext, "Focus mode engaged • Tap below to switch")
                    } else {
                        views.setTextViewText(R.id.widget_status_badge, "○ Inactive")
                        views.setTextViewText(R.id.widget_subtext, "Tap any mode below to start focusing")
                    }

                    // Three dynamic slots showing the first modes in the library
                    val slotIds = listOf(
                        R.id.widget_btn_mode_1,
                        R.id.widget_btn_mode_2,
                        R.id.widget_btn_mode_3
                    )
                    val visibleModes = modes.take(3)
                    slotIds.forEachIndexed { index, slotId ->
                        val mode = visibleModes.getOrNull(index)
                        if (mode == null) {
                            views.setViewVisibility(slotId, android.view.View.GONE)
                        } else {
                            views.setViewVisibility(slotId, android.view.View.VISIBLE)
                            // Only universally-remotable ops here: text, color,
                            // visibility, click. setBackgroundResource is NOT
                            // remotable and crashes the host when applying.
                            views.setTextViewText(
                                slotId,
                                if (mode.isActive) "\u25cf " + mode.name else mode.name
                            )
                            views.setTextColor(
                                slotId,
                                if (mode.isActive) 0xFFFFFFFF.toInt() else 0xFFB8B8BD.toInt()
                            )
                            views.setOnClickPendingIntent(
                                slotId,
                                createModeToggleIntent(context, mode.id, mode.id.hashCode())
                            )
                        }
                    }

                    // Button 4: Turn Off
                    val turnOffIntent = Intent(context, FlowWidgetProvider::class.java).apply {
                        action = ACTION_DEACTIVATE_ALL
                    }
                    val turnOffPendingIntent = PendingIntent.getBroadcast(
                        context,
                        4,
                        turnOffIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_btn_off, turnOffPendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    private fun createModeToggleIntent(context: Context, modeId: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, FlowWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_MODE
            putExtra(EXTRA_MODE_ID, modeId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent == null) return

        when (intent.action) {
            ACTION_TOGGLE_MODE -> {
                val modeId = intent.getStringExtra(EXTRA_MODE_ID) ?: return
                FlowModeController.toggleMode(context, modeId)
            }
            ACTION_DEACTIVATE_ALL -> {
                CoroutineScope(Dispatchers.Default).launch {
                    FlowModeController.deactivateAllModes(context)
                }
            }
        }
    }
}
