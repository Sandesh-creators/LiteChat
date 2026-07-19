package com.litechat.app.core.di

import android.content.Context
import com.litechat.app.BuildConfig
import com.litechat.app.data.db.AppDatabase
import com.litechat.app.data.repository.ContactRepository
import com.litechat.app.data.repository.ConversationRepository
import com.litechat.app.data.repository.GroupRepository
import com.litechat.app.data.repository.MessageRepository
import com.litechat.app.data.repository.UserProfileRepository
import com.litechat.app.data.repository.VoiceRoomRepository
import com.litechat.app.media.UriManager
import com.litechat.app.network.github.GitHubUserStore
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

    @Volatile
    private var gitHubUserStore: GitHubUserStore? = null

    @Volatile
    private var messageRepository: MessageRepository? = null

    @Volatile
    private var conversationRepository: ConversationRepository? = null

    @Volatile
    private var contactRepository: ContactRepository? = null

    @Volatile
    private var groupRepository: GroupRepository? = null

    @Volatile
    private var voiceRoomRepository: VoiceRoomRepository? = null

    @Volatile
    private var userProfileRepository: UserProfileRepository? = null

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

    fun provideGitHubUserStore(): GitHubUserStore {
        return gitHubUserStore ?: synchronized(this) {
            GitHubUserStore(
                token = BuildConfig.GITHUB_TOKEN
            ).also { gitHubUserStore = it }
        }
    }

    fun provideMessageRepository(context: Context): MessageRepository {
        return messageRepository ?: synchronized(this) {
            MessageRepository(provideDatabase(context).messageDao()).also { messageRepository = it }
        }
    }

    fun provideConversationRepository(context: Context): ConversationRepository {
        return conversationRepository ?: synchronized(this) {
            ConversationRepository(provideDatabase(context).conversationDao()).also { conversationRepository = it }
        }
    }

    fun provideContactRepository(context: Context): ContactRepository {
        return contactRepository ?: synchronized(this) {
            ContactRepository(provideDatabase(context).contactDao()).also { contactRepository = it }
        }
    }

    fun provideGroupRepository(context: Context): GroupRepository {
        return groupRepository ?: synchronized(this) {
            GroupRepository(provideDatabase(context).groupDao()).also { groupRepository = it }
        }
    }

    fun provideVoiceRoomRepository(context: Context): VoiceRoomRepository {
        return voiceRoomRepository ?: synchronized(this) {
            VoiceRoomRepository(provideDatabase(context).voiceRoomDao()).also { voiceRoomRepository = it }
        }
    }

    fun provideUserProfileRepository(context: Context): UserProfileRepository {
        return userProfileRepository ?: synchronized(this) {
            UserProfileRepository(
                provideDatabase(context).userProfileDao(),
                provideDatabase(context).contactDao(),
                provideGitHubUserStore()
            ).also { userProfileRepository = it }
        }
    }
}
