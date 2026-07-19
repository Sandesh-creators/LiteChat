package com.litechat.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.litechat.app.data.db.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE peerId = :peerId LIMIT 1")
    suspend fun getConversationByPeerId(peerId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessagePreview = :preview, lastMessageTimestamp = :timestamp WHERE id = :conversationId")
    suspend fun updateLastMessage(conversationId: String, preview: String, timestamp: Long)

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :conversationId")
    suspend fun incrementUnread(conversationId: String)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun clearUnread(conversationId: String)
}
