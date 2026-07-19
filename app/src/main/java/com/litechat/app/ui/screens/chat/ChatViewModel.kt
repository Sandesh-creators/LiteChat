package com.litechat.app.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.litechat.app.core.di.ServiceLocator
import com.litechat.app.data.db.entity.MessageEntity
import com.litechat.app.data.db.entity.MessageStatus
import com.litechat.app.data.db.entity.MessageType
import com.litechat.app.network.signaling.MqttSignalingClient
import com.litechat.app.network.signaling.SignalingMessage
import com.litechat.app.network.signaling.SignalingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    application: Application,
    private val conversationId: String
) : AndroidViewModel(application) {

    private val messageRepo = ServiceLocator.provideMessageRepository(application)
    private val conversationRepo = ServiceLocator.provideConversationRepository(application)
    private val mqttClient = ServiceLocator.provideMqttClient(application)

    val messages: StateFlow<List<MessageEntity>> = messageRepo.getMessages(conversationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        viewModelScope.launch {
            conversationRepo.clearUnread(conversationId)
        }
        observeIncomingMessages()
    }

    fun updateInput(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            val message = messageRepo.sendMessage(
                conversationId = conversationId,
                senderId = "local_user",
                content = text
            )

            _inputText.value = ""

            if (mqttClient.isConnected()) {
                mqttClient.sendMessage(
                    SignalingMessage(
                        type = SignalingType.TEXT,
                        senderId = "local_user",
                        receiverId = "",
                        conversationId = conversationId,
                        payload = text,
                        messageId = message.id
                    )
                )
                messageRepo.updateStatus(message.id, MessageStatus.SENT_TO_QUEUE)
            }

            val preview = if (text.length > 50) text.take(50) + "..." else text
            conversationRepo.updateLastMessage(conversationId, preview, System.currentTimeMillis())
        }
    }

    fun sendImage(uri: String) {
        viewModelScope.launch {
            messageRepo.sendMessage(
                conversationId = conversationId,
                senderId = "local_user",
                content = "Image",
                type = MessageType.IMAGE,
                mediaUri = uri
            )
        }
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch {
            messageRepo.deleteMessage(message)
        }
    }

    private fun observeIncomingMessages() {
        viewModelScope.launch {
            mqttClient.incomingMessages.collect { signaling ->
                if (signaling.conversationId == conversationId &&
                    signaling.type == SignalingType.TEXT
                ) {
                    val message = MessageEntity(
                        id = signaling.messageId.ifEmpty { "msg_${System.currentTimeMillis()}" },
                        conversationId = conversationId,
                        senderId = signaling.senderId,
                        content = signaling.payload,
                        type = MessageType.TEXT,
                        status = MessageStatus.DELIVERED,
                        isOutgoing = false
                    )
                    messageRepo.receiveMessage(message)
                    conversationRepo.incrementUnread(conversationId)
                    messageRepo.updateStatus(signaling.messageId, MessageStatus.DELIVERED)
                }

                if (signaling.type == SignalingType.TYPING) {
                    _isTyping.value = true
                    kotlinx.coroutines.delay(3000)
                    _isTyping.value = false
                }
            }
        }
    }
}
