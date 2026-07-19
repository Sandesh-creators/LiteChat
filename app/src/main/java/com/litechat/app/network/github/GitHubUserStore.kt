package com.litechat.app.network.github

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GitHubUserStore(
    private val owner: String = "Sandesh-creators",
    private val repo: String = "LiteChat",
    private val token: String = ""
) {
    companion object {
        private const val TAG = "GitHubUserStore"
        private const val USERS_PATH = "Chats/users.json"
        private const val RAW_BASE = "https://raw.githubusercontent.com"
        private const val API_BASE = "https://api.github.com"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    @Volatile
    private var cachedUsers: List<GitHubUserEntry> = emptyList()

    @Volatile
    private var cachedSha: String? = null

    suspend fun fetchUsers(): List<GitHubUserEntry> = withContext(Dispatchers.IO) {
        try {
            val url = "$RAW_BASE/$owner/$repo/main/$USERS_PATH"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3.raw")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "{\"users\":[]}"
                val parsed = json.decodeFromString<GitHubUsersFile>(body)
                cachedUsers = parsed.users
                Log.d(TAG, "Fetched ${parsed.users.size} users from GitHub")
                parsed.users
            } else {
                Log.w(TAG, "Failed to fetch users: ${response.code}")
                cachedUsers
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users", e)
            cachedUsers
        }
    }

    suspend fun searchUsers(query: String): List<GitHubUserEntry> {
        if (cachedUsers.isEmpty()) {
            fetchUsers()
        }
        if (query.isBlank()) return cachedUsers

        val lowerQuery = query.lowercase()
        return cachedUsers.filter { user ->
            user.username.lowercase().contains(lowerQuery) ||
                    user.displayName.lowercase().contains(lowerQuery)
        }
    }

    suspend fun pushUser(user: GitHubUserEntry): Boolean = withContext(Dispatchers.IO) {
        if (token.isBlank()) {
            Log.w(TAG, "No GitHub token available, cannot push user")
            return@withContext false
        }

        try {
            if (cachedSha == null) {
                fetchFileSha()
            }

            val currentUsers = cachedUsers.toMutableList()
            val existingIndex = currentUsers.indexOfFirst { it.id == user.id }
            if (existingIndex >= 0) {
                currentUsers[existingIndex] = user
            } else {
                currentUsers.add(user)
            }
            cachedUsers = currentUsers

            val fileContent = json.encodeToString(GitHubUsersFile(currentUsers))
            val encodedContent = Base64.encodeToString(fileContent.toByteArray(), Base64.NO_WRAP)

            val requestBody = JSONObject().apply {
                put("message", "Update user: ${user.username}")
                put("content", encodedContent)
                cachedSha?.let { put("sha", it) }
            }

            val url = "$API_BASE/repos/$owner/$repo/contents/$USERS_PATH"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .put(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    val jsonResp = JSONObject(it)
                    val sha = jsonResp.optString("sha", "")
                    cachedSha = sha.ifEmpty { null }
                }
                Log.d(TAG, "Pushed user ${user.username} to GitHub")
                true
            } else {
                Log.e(TAG, "Failed to push user: ${response.code} ${response.body?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing user", e)
            false
        }
    }

    private suspend fun fetchFileSha() = withContext(Dispatchers.IO) {
        try {
            val url = "$API_BASE/repos/$owner/$repo/contents/$USERS_PATH"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github+json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                body?.let {
                    val jsonResp = JSONObject(it)
                    val sha = jsonResp.optString("sha", "")
                    cachedSha = sha.ifEmpty { null }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching file SHA", e)
        }
    }

    fun getCachedUsers(): List<GitHubUserEntry> = cachedUsers
}
