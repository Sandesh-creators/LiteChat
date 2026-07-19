package com.litechat.app.data.repository

import com.litechat.app.data.db.dao.ContactDao
import com.litechat.app.data.db.dao.UserProfileDao
import com.litechat.app.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val contactDao: ContactDao
) {

    fun getProfile(): Flow<UserProfileEntity?> = userProfileDao.getProfile()

    suspend fun getProfileSync(): UserProfileEntity? = userProfileDao.getProfileSync()

    suspend fun initializeProfile(username: String, displayName: String): UserProfileEntity {
        val existing = userProfileDao.getProfileSync()
        if (existing != null) return existing

        val profile = UserProfileEntity(
            id = UUID.randomUUID().toString(),
            username = username,
            displayName = displayName
        )
        userProfileDao.upsertProfile(profile)
        return profile
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        val existing = contactDao.getContactByUsername(username)
        if (existing != null) return true

        val profile = userProfileDao.getProfileSync()
        return profile?.username?.lowercase() == username.lowercase()
    }

    suspend fun isUsernameTakenByOther(username: String, myUserId: String): Boolean {
        val existing = contactDao.getContactByUsernameExcluding(username, myUserId)
        if (existing != null) return true

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
        return true
    }

    suspend fun changeDisplayName(newName: String) {
        val profile = userProfileDao.getProfileSync() ?: return
        userProfileDao.updateDisplayName(profile.id, newName)
    }

    suspend fun changeStatus(newStatus: String) {
        val profile = userProfileDao.getProfileSync() ?: return
        userProfileDao.updateStatus(profile.id, newStatus)
    }

    suspend fun searchUsers(query: String): List<UserProfileEntity> {
        return emptyList()
    }
}
