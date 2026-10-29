package com.shishir.automatrix.util

import android.os.Build
import org.json.JSONObject
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
        data class UpdateAvailable(val latestVersion: String, val releaseUrl: String) : Result()
        data object UpToDate : Result()
        data class Failed(val message: String) : Result()
    }

    suspend fun check(currentVersion: String): Result {
        return try {
            val json = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                fetchLatestReleaseJson()
            } ?: return Result.Failed("Could not reach GitHub")

            val tagName = json.optString("tag_name", "").removePrefix("v")
            val htmlUrl = json.optString("html_url", LATEST_RELEASE_URL)

            if (tagName.isEmpty()) {
                Result.Failed("No release information found")
            } else if (isNewerVersion(tagName, currentVersion)) {
                Result.UpdateAvailable(latestVersion = tagName, releaseUrl = htmlUrl)
            } else {
                Result.UpToDate
            }
        } catch (e: Exception) {
            Result.Failed(e.message ?: "Update check failed")
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
