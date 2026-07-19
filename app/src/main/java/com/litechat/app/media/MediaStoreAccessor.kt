package com.litechat.app.media

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreAccessor(private val context: Context) {

    data class MediaInfo(
        val uri: Uri,
        val displayName: String,
        val mimeType: String,
        val size: Long,
        val width: Int,
        val height: Int,
        val dateAdded: Long
    )

    suspend fun getMediaInfo(uri: Uri): MediaInfo? = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.DATE_ADDED
            )
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    MediaInfo(
                        uri = uri,
                        displayName = cursor.getString(0) ?: "unknown",
                        mimeType = cursor.getString(1) ?: "image/*",
                        size = cursor.getLong(2),
                        width = cursor.getInt(3),
                        height = cursor.getInt(4),
                        dateAdded = cursor.getLong(5)
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadThumbnail(uri: Uri, maxWidth: Int = 150, maxHeight: Int = 150): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(uri, Size(maxWidth, maxHeight), null)
                } else {
                    loadThumbnailCompat(uri, maxWidth, maxHeight)
                }
            } catch (e: Exception) {
                loadThumbnailCompat(uri, maxWidth, maxHeight)
            }
        }

    private fun loadThumbnailCompat(uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
