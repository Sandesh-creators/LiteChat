package com.litechat.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val avatarUri: String? = null,
    val publicKey: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val isRegistered: Boolean = false
)
