package com.shishir.automatrix.automation.engine

import android.content.SharedPreferences

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import com.shishir.automatrix.domain.models.ActionType
import com.shishir.automatrix.domain.models.AutomationAction
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

data class RoutineStateSnapshot(
    val routineId: String,
    val capturedAt: Long = System.currentTimeMillis(),
    val originalMediaVolume: Int? = null,
    val originalRingerMode: Int? = null,
    val originalBrightness: Int? = null,
    val originalFlashlight: Boolean? = null,
    val originalBatterySaver: Boolean? = null
)

object StateRestorationManager {
    // Snapshots persist in SharedPreferences so a restore still works after
    // a reboot or an OEM battery kill in the middle of a time window.
    private const val PREFS = "automatrix_routine_snapshots"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun snapshotToJson(snapshot: RoutineStateSnapshot): String {
        val obj = JSONObject()
        obj.put("routineId", snapshot.routineId)
        obj.put("capturedAt", snapshot.capturedAt)
        snapshot.originalMediaVolume?.let { obj.put("mediaVolume", it) }
        snapshot.originalRingerMode?.let { obj.put("ringerMode", it) }
        snapshot.originalBrightness?.let { obj.put("brightness", it) }
        snapshot.originalFlashlight?.let { obj.put("flashlight", it) }
        snapshot.originalBatterySaver?.let { obj.put("batterySaver", it) }
        return obj.toString()
    }

    private fun snapshotFromJson(json: String): RoutineStateSnapshot? {
        return try {
            val obj = JSONObject(json)
            RoutineStateSnapshot(
                routineId = obj.optString("routineId"),
                capturedAt = obj.optLong("capturedAt", System.currentTimeMillis()),
                originalMediaVolume = if (obj.has("mediaVolume")) obj.getInt("mediaVolume") else null,
                originalRingerMode = if (obj.has("ringerMode")) obj.getInt("ringerMode") else null,
                originalBrightness = if (obj.has("brightness")) obj.getInt("brightness") else null,
                originalFlashlight = if (obj.has("flashlight")) obj.getBoolean("flashlight") else null,
                originalBatterySaver = if (obj.has("batterySaver")) obj.getBoolean("batterySaver") else null
            )
        } catch (_: Exception) {
            null
        }
    }

    fun captureStateBeforeExecution(
        context: Context,
        routineId: String,
        actions: List<AutomationAction>
    ) {
        val reversibleActions = actions.filter { it.restoreOnExit }
        if (reversibleActions.isEmpty()) return

        // Keep the ORIGINAL pre-window capture; later runs inside the same
        // window (sweeps) must not overwrite it with the already-changed state.
        if (prefs(context).contains(routineId)) return

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        var mediaVolume: Int? = null
        var ringerMode: Int? = null
        var brightness: Int? = null
        var flashlight: Boolean? = null
        var batterySaver: Boolean? = null

        for (action in reversibleActions) {
            when (action.type) {
                ActionType.SET_MEDIA_VOLUME -> {
                    if (mediaVolume == null && audioManager != null) {
                        mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    }
                }
                ActionType.SET_RINGER_MODE, ActionType.SET_DND_MODE -> {
                    if (ringerMode == null && audioManager != null) {
                        ringerMode = audioManager.ringerMode
                    }
                }
                ActionType.SET_BRIGHTNESS -> {
                    if (brightness == null) {
                        brightness = try {
                            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
                ActionType.TOGGLE_FLASHLIGHT -> {
                    flashlight = false // Default assumption prior to turning on
                }
                ActionType.SET_BATTERY_SAVER -> {
                    if (batterySaver == null) {
                        batterySaver = try {
                            Settings.Global.getInt(context.contentResolver, "low_power", 0) == 1
                        } catch (_: Exception) {
                            false
                        }
                    }
                }
                else -> Unit
            }
        }

        val snapshot = RoutineStateSnapshot(
            routineId = routineId,
            originalMediaVolume = mediaVolume,
            originalRingerMode = ringerMode,
            originalBrightness = brightness,
            originalFlashlight = flashlight,
            originalBatterySaver = batterySaver
        )

        prefs(context).edit().putString(routineId, snapshotToJson(snapshot)).apply()
    }

    fun restoreState(context: Context, routineId: String): Boolean {
        val json = prefs(context).getString(routineId, null) ?: return false
        prefs(context).edit().remove(routineId).apply()
        val snapshot = snapshotFromJson(json) ?: return false
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        // 1. Restore Media Volume
        snapshot.originalMediaVolume?.let { volume ->
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        }

        // 2. Restore Ringer Mode
        snapshot.originalRingerMode?.let { mode ->
            audioManager?.let { am ->
                try {
                    am.ringerMode = mode
                } catch (_: Exception) {
                    // Ignored if DND policy blocks changing
                }
            }
        }

        // 3. Restore Brightness
        snapshot.originalBrightness?.let { b ->
            if (Settings.System.canWrite(context)) {
                try {
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        b
                    )
                } catch (_: Exception) {}
            }
        }

        // 4. Restore Torch
        snapshot.originalFlashlight?.let { state ->
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, state)
                }
            } catch (_: Exception) {}
        }

        // 5. Restore Battery Saver (elevated setting — same grant as the action)
        snapshot.originalBatterySaver?.let { saverOn ->
            try {
                Settings.Global.putInt(context.contentResolver, "low_power", if (saverOn) 1 else 0)
            } catch (_: Exception) {}
        }

        return true
    }

    fun clearAll() {
        // Intentionally does not wipe persisted snapshots: a pending
        // end-of-window restore must survive app restarts.
    }
}
