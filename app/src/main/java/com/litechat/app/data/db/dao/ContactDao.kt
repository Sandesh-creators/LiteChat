package com.litechat.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.litechat.app.data.db.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY displayName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE isRegistered = 1")
    fun getRegisteredContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isOnline = :isOnline, lastSeen = :timestamp WHERE id = :contactId")
    suspend fun updateOnlineStatus(contactId: String, isOnline: Boolean, timestamp: Long)

    @Query("SELECT * FROM contacts WHERE displayName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%'")
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE username = :username LIMIT 1")
    suspend fun getContactByUsername(username: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE username = :username AND id != :excludeId LIMIT 1")
    suspend fun getContactByUsernameExcluding(username: String, excludeId: String): ContactEntity?
}
