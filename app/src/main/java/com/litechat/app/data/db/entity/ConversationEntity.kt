package com.litechat.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [Index("lastMessageTimestamp")]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val peerId: String,
    val peerName: String,
    val peerAvatarUri: String? = null,
    val lastMessagePreview: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isGroup: Boolean = false,
    val groupId: String? = null,
    val memberCount: Int = 2,
    val createdAt: Long = System.currentTimeMillis()
)
