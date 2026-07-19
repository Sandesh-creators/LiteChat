package com.litechat.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.litechat.app.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET username = :username, lastUpdated = :timestamp WHERE id = :userId")
    suspend fun updateUsername(userId: String, username: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET displayName = :name, lastUpdated = :timestamp WHERE id = :userId")
    suspend fun updateDisplayName(userId: String, name: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET statusMessage = :status, lastUpdated = :timestamp WHERE id = :userId")
    suspend fun updateStatus(userId: String, status: String, timestamp: Long = System.currentTimeMillis())
}
