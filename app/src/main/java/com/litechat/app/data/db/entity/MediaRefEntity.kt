package com.litechat.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MediaRefStatus {
    GRANTED,
    REVOKED,
    PURGED
}

@Entity(
    tableName = "media_refs",
    indices = [Index("messageId")]
)
data class MediaRefEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val contentUri: String,
    val originalFileName: String,
    val mimeType: String,
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val durationMs: Long = 0L,
    val status: MediaRefStatus = MediaRefStatus.GRANTED,
    val cachedThumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
