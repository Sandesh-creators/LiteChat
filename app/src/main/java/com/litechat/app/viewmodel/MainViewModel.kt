package com.litechat.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.litechat.app.core.di.ServiceLocator
import com.litechat.app.data.db.entity.ConversationEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationRepo = ServiceLocator.provideConversationRepository(application)
    private val mqttClient = ServiceLocator.provideMqttClient(application)

    val conversations: StateFlow<List<ConversationEntity>> = conversationRepo.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun connectMqtt(userId: String) {
        mqttClient.connect(userId)
    }

    fun disconnectMqtt() {
        mqttClient.disconnect()
    }
}
