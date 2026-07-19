package com.litechat.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [Index("name")]
)
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val memberCount: Int = 1,
    val avatarUri: String? = null,
    val maxMembers: Int = 20
)
