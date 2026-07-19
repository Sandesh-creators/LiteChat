package com.litechat.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class VoiceRoomStatus {
    WAITING,
    ACTIVE,
    ENDED
}

@Entity(
    tableName = "voice_rooms",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class VoiceRoomEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val groupName: String,
    val createdBy: String,
    val status: VoiceRoomStatus = VoiceRoomStatus.WAITING,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long = 0L,
    val participantCount: Int = 0
)
