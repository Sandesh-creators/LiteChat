package com.litechat.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.litechat.app.data.db.entity.VoiceParticipantEntity
import com.litechat.app.data.db.entity.VoiceRoomEntity
import com.litechat.app.data.db.entity.VoiceRoomStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceRoomDao {

    @Query("SELECT * FROM voice_rooms WHERE groupId = :groupId AND status != 'ENDED' ORDER BY startedAt DESC")
    fun getActiveRoomsForGroup(groupId: String): Flow<List<VoiceRoomEntity>>

    @Query("SELECT * FROM voice_rooms WHERE status = 'ACTIVE'")
    fun getAllActiveRooms(): Flow<List<VoiceRoomEntity>>

    @Query("SELECT * FROM voice_rooms WHERE id = :roomId")
    suspend fun getRoomById(roomId: String): VoiceRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: VoiceRoomEntity)

    @Update
    suspend fun updateRoom(room: VoiceRoomEntity)

    @Query("UPDATE voice_rooms SET status = :status, endedAt = :endedAt WHERE id = :roomId")
    suspend fun updateRoomStatus(roomId: String, status: VoiceRoomStatus, endedAt: Long = 0L)

    @Query("SELECT * FROM voice_participants WHERE roomId = :roomId ORDER BY joinedAt ASC")
    fun getParticipants(roomId: String): Flow<List<VoiceParticipantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: VoiceParticipantEntity)

    @Query("DELETE FROM voice_participants WHERE roomId = :roomId AND userId = :userId")
    suspend fun removeParticipant(roomId: String, userId: String)

    @Query("UPDATE voice_participants SET isMuted = :isMuted WHERE roomId = :roomId AND userId = :userId")
    suspend fun setMuted(roomId: String, userId: String, isMuted: Boolean)

    @Query("UPDATE voice_participants SET isSpeaking = :isSpeaking WHERE roomId = :roomId AND userId = :userId")
    suspend fun setSpeaking(roomId: String, userId: String, isSpeaking: Boolean)

    @Query("SELECT COUNT(*) FROM voice_participants WHERE roomId = :roomId")
    suspend fun getParticipantCount(roomId: String): Int
}
