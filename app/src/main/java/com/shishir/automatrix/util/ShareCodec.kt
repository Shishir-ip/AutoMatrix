package com.shishir.automatrix.util

import android.util.Base64
import com.shishir.automatrix.data.backup.BackupManager
import com.shishir.automatrix.domain.models.Routine

/**
 * Encodes/decodes a single routine into a portable share code:
 * `automatrix://import/<base64url of backup JSON>`.
 *
 * The payload uses the exact backup JSON schema, so a pasted share code works
 * in the existing Import Backup dialog too.
 */
object ShareCodec {

    const val PREFIX = "automatrix://import/"

    fun encode(routine: Routine): String {
        val json = BackupManager.exportRoutinesToJson(listOf(routine))
        val encoded = Base64.encodeToString(
            json.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        return PREFIX + encoded
    }

    /** Returns the routine JSON payload, or null when the code is not a valid share code. */
    fun decode(code: String): String? {
        val trimmed = code.trim()
        val payload = if (trimmed.startsWith(PREFIX)) {
            trimmed.removePrefix(PREFIX)
        } else {
            // Allow pasting a bare code without the scheme
            trimmed
        }
        if (payload.isBlank()) return null
        return try {
            val bytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val json = String(bytes, Charsets.UTF_8)
            if (json.contains("\"routines\"")) json else null
        } catch (_: Exception) {
            null
        }
    }

    /** Rough scanability limit: QR codes comfortably hold ~2.3 KB at low error correction. */
    fun isQrFriendly(code: String): Boolean = code.length <= 2400
}
