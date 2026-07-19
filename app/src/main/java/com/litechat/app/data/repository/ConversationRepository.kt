package com.litechat.app.data.repository

import com.litechat.app.data.db.dao.ConversationDao
import com.litechat.app.data.db.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ConversationRepository(private val conversationDao: ConversationDao) {

    fun getAllConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    suspend fun getConversationById(id: String): ConversationEntity? =
        conversationDao.getConversationById(id)

    suspend fun getOrCreateConversation(peerId: String, peerName: String): ConversationEntity {
        val existing = conversationDao.getConversationByPeerId(peerId)
        if (existing != null) return existing

        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            peerId = peerId,
            peerName = peerName
        )
        conversationDao.insertConversation(conversation)
        return conversation
    }

    suspend fun updateLastMessage(conversationId: String, preview: String, timestamp: Long) {
        conversationDao.updateLastMessage(conversationId, preview, timestamp)
    }

    suspend fun incrementUnread(conversationId: String) {
        conversationDao.incrementUnread(conversationId)
    }

    suspend fun clearUnread(conversationId: String) {
        conversationDao.clearUnread(conversationId)
    }

    suspend fun deleteConversation(conversation: ConversationEntity) {
        conversationDao.deleteConversation(conversation)
    }
}
