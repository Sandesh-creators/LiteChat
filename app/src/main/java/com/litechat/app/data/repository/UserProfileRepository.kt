package com.litechat.app.data.repository

import com.litechat.app.data.db.dao.ContactDao
import com.litechat.app.data.db.dao.UserProfileDao
import com.litechat.app.data.db.entity.UserProfileEntity
import com.litechat.app.network.github.GitHubUserEntry
import com.litechat.app.network.github.GitHubUserStore
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val contactDao: ContactDao,
    private val gitHubUserStore: GitHubUserStore? = null
) {

    fun getProfile(): Flow<UserProfileEntity?> = userProfileDao.getProfile()

    suspend fun getProfileSync(): UserProfileEntity? = userProfileDao.getProfileSync()

    suspend fun initializeProfile(username: String, displayName: String): UserProfileEntity {
        val existing = userProfileDao.getProfileSync()
        if (existing != null) {
            syncProfileToGitHub(existing)
            return existing
        }

        val profile = UserProfileEntity(
            id = UUID.randomUUID().toString(),
            username = username,
            displayName = displayName
        )
        userProfileDao.upsertProfile(profile)
        syncProfileToGitHub(profile)
        return profile
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        val localTaken = contactDao.getContactByUsername(username)
        if (localTaken != null) return true

        val ghUsers = gitHubUserStore?.searchUsers(username) ?: emptyList()
        if (ghUsers.any { it.username.lowercase() == username.lowercase() }) return true

        val profile = userProfileDao.getProfileSync()
        return profile?.username?.lowercase() == username.lowercase()
    }

    suspend fun isUsernameTakenByOther(username: String, myUserId: String): Boolean {
        val existing = contactDao.getContactByUsernameExcluding(username, myUserId)
        if (existing != null) return true

        val ghUsers = gitHubUserStore?.searchUsers(username) ?: emptyList()
        if (ghUsers.any { it.id != myUserId && it.username.lowercase() == username.lowercase() }) return true

        val profile = userProfileDao.getProfileSync()
        if (profile?.id != myUserId && profile?.username?.lowercase() == username.lowercase()) {
            return true
        }
        return false
    }

    suspend fun changeUsername(newUsername: String): Boolean {
        val profile = userProfileDao.getProfileSync() ?: return false
        if (isUsernameTakenByOther(newUsername, profile.id)) return false
        userProfileDao.updateUsername(profile.id, newUsername)
        val updated = userProfileDao.getProfileSync()
        if (updated != null) syncProfileToGitHub(updated)
        return true
    }

    suspend fun changeDisplayName(newName: String) {
        val profile = userProfileDao.getProfileSync() ?: return
        userProfileDao.updateDisplayName(profile.id, newName)
        val updated = userProfileDao.getProfileSync()
        if (updated != null) syncProfileToGitHub(updated)
    }

    suspend fun changeStatus(newStatus: String) {
        val profile = userProfileDao.getProfileSync() ?: return
        userProfileDao.updateStatus(profile.id, newStatus)
        val updated = userProfileDao.getProfileSync()
        if (updated != null) syncProfileToGitHub(updated)
    }

    suspend fun searchUsers(query: String): List<GitHubUserEntry> {
        return gitHubUserStore?.searchUsers(query) ?: emptyList()
    }

    private suspend fun syncProfileToGitHub(profile: UserProfileEntity) {
        val entry = GitHubUserEntry(
            id = profile.id,
            username = profile.username,
            displayName = profile.displayName,
            statusMessage = profile.statusMessage,
            avatarUrl = profile.avatarUri,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                .format(java.util.Date(profile.createdAt)),
            lastUpdated = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                .format(java.util.Date(profile.lastUpdated))
        )
        gitHubUserStore?.pushUser(entry)
    }
}
