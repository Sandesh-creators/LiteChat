package com.litechat.app.media

import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThumbnailLoader(private val mediaStore: MediaStoreAccessor) {

    companion object {
        const val THUMBNAIL_MAX_WIDTH = 150
        const val THUMBNAIL_MAX_HEIGHT = 150
        const val MAX_CACHE_SIZE = 30 * 1024 * 1024L
    }

    private object ThumbnailCache : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 16).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            if (evicted && !oldValue.isRecycled) {
                oldValue.recycle()
            }
        }
    }

    suspend fun loadThumbnail(uri: Uri): Bitmap? {
        val key = uri.toString()
        ThumbnailCache.get(key)?.let { return it }

        val bitmap = mediaStore.loadThumbnail(uri, THUMBNAIL_MAX_WIDTH, THUMBNAIL_MAX_HEIGHT)
        if (bitmap != null) {
            ThumbnailCache.put(key, bitmap)
        }
        return bitmap
    }

    fun evictAll() {
        ThumbnailCache.evictAll()
    }

    fun trimToSize(targetSize: Int) {
        ThumbnailCache.trimToSize(targetSize)
    }
}
