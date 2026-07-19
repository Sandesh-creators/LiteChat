package com.litechat.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.litechat.app.data.db.entity.MediaRefEntity
import com.litechat.app.data.db.entity.MediaRefStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaRefDao {

    @Query("SELECT * FROM media_refs WHERE messageId = :messageId")
    suspend fun getMediaRefByMessageId(messageId: String): MediaRefEntity?

    @Query("SELECT * FROM media_refs WHERE messageId = :messageId")
    fun observeMediaRef(messageId: String): Flow<MediaRefEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaRef(mediaRef: MediaRefEntity)

    @Update
    suspend fun updateMediaRef(mediaRef: MediaRefEntity)

    @Delete
    suspend fun deleteMediaRef(mediaRef: MediaRefEntity)

    @Query("UPDATE media_refs SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: MediaRefStatus)

    @Query("SELECT * FROM media_refs WHERE status = :status")
    suspend fun getMediaRefsByStatus(status: MediaRefStatus): List<MediaRefEntity>

    @Query("DELETE FROM media_refs WHERE status = 'PURGED'")
    suspend fun purgeRevokedRefs()
}
