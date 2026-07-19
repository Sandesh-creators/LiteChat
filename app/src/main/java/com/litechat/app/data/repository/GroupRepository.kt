package com.litechat.app.data.repository

import com.litechat.app.data.db.dao.GroupDao
import com.litechat.app.data.db.entity.GroupEntity
import com.litechat.app.data.db.entity.GroupMemberEntity
import com.litechat.app.data.db.entity.GroupRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GroupRepository(private val groupDao: GroupDao) {

    companion object {
        const val MAX_GROUP_MEMBERS = 20
    }

    fun getAllGroups(): Flow<List<GroupEntity>> = groupDao.getAllGroups()

    fun searchGroups(query: String): Flow<List<GroupEntity>> = groupDao.searchGroups(query)

    suspend fun getGroupById(id: String): GroupEntity? = groupDao.getGroupById(id)

    suspend fun createGroup(
        name: String,
        description: String,
        createdByUserId: String,
        createdByUserName: String,
        createdByUsername: String
    ): GroupEntity {
        val group = GroupEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            createdBy = createdByUserId
        )
        groupDao.insertGroup(group)

        val owner = GroupMemberEntity(
            id = UUID.randomUUID().toString(),
            groupId = group.id,
            userId = createdByUserId,
            displayName = createdByUserName,
            username = createdByUsername,
            role = GroupRole.OWNER
        )
        groupDao.insertMember(owner)

        return group
    }

    suspend fun addMember(
        groupId: String,
        userId: String,
        displayName: String,
        username: String,
        role: GroupRole = GroupRole.MEMBER
    ): Boolean {
        val memberCount = groupDao.getMemberCount(groupId)
        if (memberCount >= MAX_GROUP_MEMBERS) return false

        val existing = groupDao.getMember(groupId, userId)
        if (existing != null) return false

        val member = GroupMemberEntity(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            userId = userId,
            displayName = displayName,
            username = username,
            role = role
        )
        groupDao.insertMember(member)

        val group = groupDao.getGroupById(groupId)
        group?.let {
            groupDao.updateGroup(it.copy(memberCount = memberCount + 1))
        }
        return true
    }

    suspend fun removeMember(groupId: String, userId: String) {
        groupDao.removeMemberByUserId(groupId, userId)
        val memberCount = groupDao.getMemberCount(groupId)
        val group = groupDao.getGroupById(groupId)
        group?.let {
            groupDao.updateGroup(it.copy(memberCount = memberCount))
        }
    }

    fun getMembers(groupId: String): Flow<List<GroupMemberEntity>> =
        groupDao.getGroupMembers(groupId)

    suspend fun getMemberCount(groupId: String): Int = groupDao.getMemberCount(groupId)

    suspend fun isMember(groupId: String, userId: String): Boolean =
        groupDao.getMember(groupId, userId) != null

    suspend fun deleteGroup(group: GroupEntity) {
        groupDao.deleteGroup(group)
    }
}
