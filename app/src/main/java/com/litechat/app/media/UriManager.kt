package com.litechat.app.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.util.UUID

class UriManager(private val context: Context) {

    data class ManagedUri(
        val id: String,
        val uri: Uri,
        val displayName: String,
        val mimeType: String,
        val fileSize: Long
    )

    fun obtainPersistableUri(uri: Uri, mimeType: String): ManagedUri {
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)

        val (name, size) = queryUriMetadata(uri, mimeType)

        return ManagedUri(
            id = UUID.randomUUID().toString(),
            uri = uri,
            displayName = name,
            mimeType = mimeType,
            fileSize = size
        )
    }

    fun releasePersistableUri(uri: Uri) {
        try {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            android.util.Log.w("UriManager", "Failed to release URI permission: ${e.message}")
        }
    }

    fun isUriAccessible(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (e: SecurityException) {
            false
        }
    }

    fun createShareableUri(originalUri: Uri, mimeType: String): Uri {
        return if (mimeType.startsWith("image/")) {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            context.contentResolver.query(originalUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(0)
                    MediaStore.Images.Media.getContentUri("external", id)
                } else {
                    originalUri
                }
            } ?: originalUri
        } else if (mimeType.startsWith("video/")) {
            val projection = arrayOf(MediaStore.Video.Media._ID)
            context.contentResolver.query(originalUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(0)
                    MediaStore.Video.Media.getContentUri("external", id)
                } else {
                    originalUri
                }
            } ?: originalUri
        } else {
            originalUri
        }
    }

    private fun queryUriMetadata(uri: Uri, mimeType: String): Pair<String, Long> {
        var name = "unknown"
        var size = 0L
        try {
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_SIZE
            )
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0) ?: "unknown"
                    size = cursor.getLong(1)
                }
            }
        } catch (e: Exception) {
            if (mimeType.startsWith("image/")) {
                val projection = arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE
                )
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        name = cursor.getString(0) ?: "unknown"
                        size = cursor.getLong(1)
                    }
                }
            }
        }
        return Pair(name, size)
    }

    fun getMediaUris(): List<ManagedUri> {
        val uris = mutableListOf<ManagedUri>()
        val collection = MediaStore.Images.Media.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE
        )
        context.contentResolver.query(collection, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol) ?: "unknown"
                val mime = cursor.getString(mimeCol) ?: "image/*"
                val size = cursor.getLong(sizeCol)
                uris.add(
                    ManagedUri(
                        id = id.toString(),
                        uri = MediaStore.Images.Media.getContentUri("external", id),
                        displayName = name,
                        mimeType = mime,
                        fileSize = size
                    )
                )
            }
        }
        return uris
    }
}
