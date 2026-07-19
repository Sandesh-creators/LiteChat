package com.litechat.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.litechat.app.data.db.dao.ContactDao
import com.litechat.app.data.db.dao.ConversationDao
import com.litechat.app.data.db.dao.GroupDao
import com.litechat.app.data.db.dao.MediaRefDao
import com.litechat.app.data.db.dao.MessageDao
import com.litechat.app.data.db.dao.UserProfileDao
import com.litechat.app.data.db.dao.VoiceRoomDao
import com.litechat.app.data.db.entity.ContactEntity
import com.litechat.app.data.db.entity.ConversationEntity
import com.litechat.app.data.db.entity.GroupEntity
import com.litechat.app.data.db.entity.GroupMemberEntity
import com.litechat.app.data.db.entity.MediaRefEntity
import com.litechat.app.data.db.entity.MessageEntity
import com.litechat.app.data.db.entity.UserProfileEntity
import com.litechat.app.data.db.entity.VoiceParticipantEntity
import com.litechat.app.data.db.entity.VoiceRoomEntity

@Database(
    entities = [
        MessageEntity::class,
        ConversationEntity::class,
        ContactEntity::class,
        MediaRefEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        VoiceRoomEntity::class,
        VoiceParticipantEntity::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun contactDao(): ContactDao
    abstract fun mediaRefDao(): MediaRefDao
    abstract fun groupDao(): GroupDao
    abstract fun voiceRoomDao(): VoiceRoomDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "litechat.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
