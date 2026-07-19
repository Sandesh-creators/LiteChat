package com.litechat.app.data.repository

import com.litechat.app.data.db.dao.MessageDao
import com.litechat.app.data.db.entity.MessageEntity
import com.litechat.app.data.db.entity.MessageStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MessageRepository(private val messageDao: MessageDao) {

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesByConversation(conversationId)

    fun getLastMessage(conversationId: String): Flow<MessageEntity?> =
        messageDao.getLastMessage(conversationId)

    suspend fun getMessageById(messageId: String): MessageEntity? =
        messageDao.getMessageById(messageId)

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String,
        type: com.litechat.app.data.db.entity.MessageType = com.litechat.app.data.db.entity.MessageType.TEXT,
        mediaUri: String? = null
    ): MessageEntity {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            type = type,
            status = MessageStatus.PENDING,
            mediaUri = mediaUri,
            isOutgoing = true
        )
        messageDao.insertMessage(message)
        return message
    }

    suspend fun updateStatus(messageId: String, status: MessageStatus) {
        messageDao.safeUpdateStatus(messageId, status)
    }

    suspend fun receiveMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun deleteMessage(message: MessageEntity) {
        messageDao.deleteMessage(message)
    }

    suspend fun purgeOldMessages(beforeTimestamp: Long) {
        messageDao.bulkUpdateOldMessages(beforeTimestamp, MessageStatus.DELIVERED, MessageStatus.PURGED)
    }
}
