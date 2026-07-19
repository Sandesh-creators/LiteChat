package com.litechat.app.core.di

import android.content.Context
import com.litechat.app.data.db.AppDatabase
import com.litechat.app.data.repository.ContactRepository
import com.litechat.app.data.repository.ConversationRepository
import com.litechat.app.data.repository.MessageRepository
import com.litechat.app.media.UriManager
import com.litechat.app.network.signaling.MqttSignalingClient
import com.litechat.app.network.webrtc.CallManager

object ServiceLocator {

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var mqttClient: MqttSignalingClient? = null

    @Volatile
    private var callManager: CallManager? = null

    @Volatile
    private var uriManager: UriManager? = null

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            AppDatabase.getInstance(context).also { database = it }
        }
    }

    fun provideMqttClient(context: Context): MqttSignalingClient {
        return mqttClient ?: synchronized(this) {
            MqttSignalingClient(context).also { mqttClient = it }
        }
    }

    fun provideCallManager(context: Context): CallManager {
        return callManager ?: synchronized(this) {
            CallManager(context).also { callManager = it }
        }
    }

    fun provideUriManager(context: Context): UriManager {
        return uriManager ?: synchronized(this) {
            UriManager(context).also { uriManager = it }
        }
    }

    fun provideMessageRepository(context: Context): MessageRepository {
        return MessageRepository(provideDatabase(context).messageDao())
    }

    fun provideConversationRepository(context: Context): ConversationRepository {
        return ConversationRepository(provideDatabase(context).conversationDao())
    }

    fun provideContactRepository(context: Context): ContactRepository {
        return ContactRepository(provideDatabase(context).contactDao())
    }
}
