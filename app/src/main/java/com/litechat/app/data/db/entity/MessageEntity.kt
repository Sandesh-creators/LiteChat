package com.litechat.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MessageStatus {
    PENDING,
    SENT_TO_QUEUE,
    STREAMING,
    DELIVERED,
    PURGED
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    VOICE_CALL,
    VIDEO_CALL
}

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conversationId"),
        Index("status"),
        Index("timestamp"),
        Index("conversationId", "timestamp")
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val type: MessageType,
    val status: MessageStatus = MessageStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUri: String? = null,
    val thumbnailUri: String? = null,
    val mimeType: String? = null,
    val fileSize: Long = 0L,
    val encryptedKey: String? = null,
    val replyToId: String? = null,
    val isOutgoing: Boolean = true
)
