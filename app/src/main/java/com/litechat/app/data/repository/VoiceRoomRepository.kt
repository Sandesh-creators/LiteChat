package com.litechat.app.data.repository

import com.litechat.app.data.db.dao.VoiceRoomDao
import com.litechat.app.data.db.entity.VoiceParticipantEntity
import com.litechat.app.data.db.entity.VoiceRoomEntity
import com.litechat.app.data.db.entity.VoiceRoomStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VoiceRoomRepository(private val voiceRoomDao: VoiceRoomDao) {

    fun getAllActiveRooms(): Flow<List<VoiceRoomEntity>> = voiceRoomDao.getAllActiveRooms()

    fun getActiveRoomsForGroup(groupId: String): Flow<List<VoiceRoomEntity>> =
        voiceRoomDao.getActiveRoomsForGroup(groupId)

    fun getParticipants(roomId: String): Flow<List<VoiceParticipantEntity>> =
        voiceRoomDao.getParticipants(roomId)

    suspend fun createRoom(
        groupId: String,
        groupName: String,
        createdBy: String
    ): VoiceRoomEntity {
        val room = VoiceRoomEntity(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            groupName = groupName,
            createdBy = createdBy,
            status = VoiceRoomStatus.ACTIVE,
            startedAt = System.currentTimeMillis()
        )
        voiceRoomDao.insertRoom(room)
        return room
    }

    suspend fun joinRoom(
        roomId: String,
        userId: String,
        displayName: String,
        username: String
    ): Boolean {
        val participant = VoiceParticipantEntity(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            userId = userId,
            displayName = displayName,
            username = username
        )
        voiceRoomDao.insertParticipant(participant)

        val room = voiceRoomDao.getRoomById(roomId)
        room?.let {
            val count = voiceRoomDao.getParticipantCount(roomId)
            voiceRoomDao.updateRoom(it.copy(participantCount = count))
        }
        return true
    }

    suspend fun leaveRoom(roomId: String, userId: String) {
        voiceRoomDao.removeParticipant(roomId, userId)
        val room = voiceRoomDao.getRoomById(roomId)
        room?.let {
            val count = voiceRoomDao.getParticipantCount(roomId)
            if (count <= 0) {
                voiceRoomDao.updateRoomStatus(roomId, VoiceRoomStatus.ENDED, System.currentTimeMillis())
                voiceRoomDao.updateRoom(it.copy(participantCount = 0, status = VoiceRoomStatus.ENDED))
            } else {
                voiceRoomDao.updateRoom(it.copy(participantCount = count))
            }
        }
    }

    suspend fun setMuted(roomId: String, userId: String, isMuted: Boolean) {
        voiceRoomDao.setMuted(roomId, userId, isMuted)
    }

    suspend fun endRoom(roomId: String) {
        voiceRoomDao.updateRoomStatus(roomId, VoiceRoomStatus.ENDED, System.currentTimeMillis())
    }

    suspend fun getActiveRoomForGroup(groupId: String): VoiceRoomEntity? {
        return voiceRoomDao.getAllActiveRooms().let { flow ->
            var room: VoiceRoomEntity? = null
            flow.collect { rooms ->
                room = rooms.firstOrNull { it.groupId == groupId }
            }
            room
        }
    }
}
