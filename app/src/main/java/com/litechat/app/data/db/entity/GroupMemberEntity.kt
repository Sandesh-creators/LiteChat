package com.litechat.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class GroupRole {
    OWNER,
    ADMIN,
    MEMBER
}

@Entity(
    tableName = "group_members",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("groupId"),
        Index("userId"),
        Index("groupId", "userId", unique = true)
    ]
)
data class GroupMemberEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val userId: String,
    val displayName: String,
    val username: String = "",
    val role: GroupRole = GroupRole.MEMBER,
    val isMuted: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)
