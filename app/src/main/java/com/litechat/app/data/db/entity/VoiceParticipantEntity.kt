package com.litechat.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "voice_participants",
    foreignKeys = [
        ForeignKey(
            entity = VoiceRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roomId"),
        Index("userId")
    ]
)
data class VoiceParticipantEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val userId: String,
    val displayName: String,
    val username: String = "",
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)
