package com.shishir.automatrix.automation.engine

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

/**
 * Process-wide TextToSpeech wrapper so routine actions can speak without
 * re-initializing the engine on every execution. Initialization is async;
 * a pending utterance is replayed once the engine becomes ready.
 */
object TtsSpeaker {

    private val ttsRef = AtomicReference<TextToSpeech?>(null)
    private var pendingText: String? = null
    private var pendingAlarmStream = false

    /** @return true when the utterance was queued with the TTS engine. */
    fun speak(context: Context, text: String, alarmStream: Boolean): Boolean {
        if (text.isBlank()) return false

        if (ttsRef.get() == null) {
            synchronized(this) {
                if (ttsRef.get() == null) {
                    pendingText = text
                    pendingAlarmStream = alarmStream
                    val created = TextToSpeech(context.applicationContext) { status ->
                        val engine = ttsRef.get() ?: return@TextToSpeech
                        if (status == TextToSpeech.SUCCESS) {
                            try {
                                engine.language = Locale.getDefault()
                            } catch (_: Exception) {
                                // Language selection is best-effort
                            }
                            val queued = synchronized(this) {
                                val p = pendingText
                                pendingText = null
                                p
                            }
                            if (queued != null) {
                                dispatch(engine, queued, pendingAlarmStream)
                            }
                        } else {
                            shutdown()
                        }
                    }
                    ttsRef.set(created)
                }
            }
        }

        val engine = ttsRef.get() ?: return false
        return dispatch(engine, text, alarmStream)
    }

    private fun dispatch(engine: TextToSpeech, text: String, alarmStream: Boolean): Boolean {
        return try {
            if (alarmStream) {
                // Playing on the alarm stream keeps the voice audible even
                // while the ringer is set to silent.
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_STREAM] =
                    AudioManager.STREAM_ALARM.toString()
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, params) == TextToSpeech.SUCCESS
            } else {
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "automatrix_tts") ==
                        TextToSpeech.SUCCESS
            }
        } catch (_: Exception) {
            false
        }
    }

    fun shutdown() {
        synchronized(this) {
            try {
                ttsRef.get()?.stop()
                ttsRef.get()?.shutdown()
            } catch (_: Exception) {
                // Shutdown failures are harmless
            }
            ttsRef.set(null)
            pendingText = null
        }
    }
}
