package com.shishir.automatrix.util

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manual update check against the public GitHub releases API.
 * This is the app's only network call and it never runs automatically.
 */
object UpdateChecker {

    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/Shishir-ip/AutoMatrix/releases/latest"

    sealed class Result {
        data class UpdateAvailable(
            val latestVersion: String,
            val releaseUrl: String,
            val apkUrl: String,
            val whatsNew: String
        ) : Result()
        data object UpToDate : Result()
        data class Failed(val message: String) : Result()
    }

    suspend fun check(currentVersion: String): Result {
        return try {
            val json = withContext(Dispatchers.IO) {
                fetchLatestReleaseJson()
            } ?: return Result.Failed("Could not reach GitHub")

            val tagName = json.optString("tag_name", "").removePrefix("v")
            val htmlUrl = json.optString("html_url", LATEST_RELEASE_URL)
            val body = json.optString("body", "")
            val apkUrl = run {
                val assets = json.optJSONArray("assets")
                var found = ""
                for (i in 0 until (assets?.length() ?: 0)) {
                    val asset = assets!!.optJSONObject(i) ?: continue
                    val name = asset.optString("name", "")
                    val url = asset.optString("browser_download_url", "")
                    if (name.endsWith(".apk") && url.isNotBlank()) {
                        found = url
                        break
                    }
                }
                found
            }

            if (tagName.isEmpty()) {
                Result.Failed("No release information found")
            } else if (isNewerVersion(tagName, currentVersion)) {
                Result.UpdateAvailable(
                    latestVersion = tagName,
                    releaseUrl = htmlUrl,
                    apkUrl = apkUrl,
                    whatsNew = body.ifBlank { "Bug fixes and improvements." }
                )
            } else {
                Result.UpToDate
            }
        } catch (e: Exception) {
            Result.Failed(e.message ?: "Update check failed")
        }
    }

    /**
     * Streams the APK into the app's private storage while reporting progress
     * (0–100). Returns the downloaded file, or null on failure.
     */
    suspend fun downloadApk(
        context: Context,
        url: String,
        onProgress: (Int) -> Unit
    ): File? {
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, "updates").apply { mkdirs() }
                val dest = File(dir, "automatrix-update.apk")

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 20000
                connection.instanceFollowRedirects = true

                val total = connection.contentLengthLong.let { if (it <= 0) -1L else it }
                var done = 0L
                var lastReported = -1

                connection.inputStream.use { input ->
                    dest.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            done += read
                            if (total > 0) {
                                val percent = (done * 100 / total).toInt().coerceIn(0, 100)
                                if (percent != lastReported) {
                                    lastReported = percent
                                    onProgress(percent)
                                }
                            }
                        }
                    }
                }
                connection.disconnect()

                if (dest.length() > 0) {
                    onProgress(100)
                    dest
                } else {
                    dest.delete()
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun fetchLatestReleaseJson(): JSONObject? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "AutoMatrix/${Build.VERSION.SDK_INT}")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            connection.inputStream.bufferedReader().use { it.readText() }.let { JSONObject(it) }
        } finally {
            connection?.disconnect()
        }
    }

    /** Compares dotted versions like "1.10.0" > "1.9.2". */
    private fun isNewerVersion(candidate: String, current: String): Boolean {
        val cand = candidate.split('.').map { it.trim().toIntOrNull() ?: 0 }
        val curr = current.split('.').map { it.trim().toIntOrNull() ?: 0 }
        val size = maxOf(cand.size, curr.size)
        for (i in 0 until size) {
            val c = cand.getOrElse(i) { 0 }
            val k = curr.getOrElse(i) { 0 }
            if (c != k) return c > k
        }
        return false
    }
}
