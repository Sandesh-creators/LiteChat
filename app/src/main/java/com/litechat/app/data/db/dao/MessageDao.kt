package com.litechat.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.litechat.app.data.db.entity.MessageEntity
import com.litechat.app.data.db.entity.MessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversation(conversationId: String)

    @Query("UPDATE messages SET status = :newStatus WHERE status = :oldStatus")
    suspend fun bulkUpdateStatus(oldStatus: MessageStatus, newStatus: MessageStatus)

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND status != :status")
    suspend fun countUndelivered(conversationId: String, status: MessageStatus = MessageStatus.DELIVERED): Int

    @Query("""
        SELECT * FROM messages 
        WHERE conversationId = :conversationId 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    fun getLastMessage(conversationId: String): Flow<MessageEntity?>

    @Query("UPDATE messages SET status = :status WHERE id = :messageId AND status != 'PURGED'")
    suspend fun safeUpdateStatus(messageId: String, status: MessageStatus)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC LIMIT :limit")
    fun getRecentMessages(conversationId: String, limit: Int = 50): Flow<List<MessageEntity>>
}
