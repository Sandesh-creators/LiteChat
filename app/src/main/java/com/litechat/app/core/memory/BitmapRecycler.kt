package com.litechat.app.core.memory

import android.graphics.Bitmap
import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap

object BitmapRecycler {

    private val trackedBitmaps = ConcurrentHashMap<String, SoftReference<Bitmap>>()

    fun track(key: String, bitmap: Bitmap) {
        trackedBitmaps[key] = SoftReference(bitmap)
    }

    fun get(key: String): Bitmap? {
        return trackedBitmaps[key]?.get()
    }

    fun recycle(key: String) {
        trackedBitmaps.remove(key)?.get()?.let { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    fun recycleAll() {
        trackedBitmaps.forEach { (_, ref) ->
            ref.get()?.let { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        }
        trackedBitmaps.clear()
    }

    fun recycleOldest(count: Int) {
        val keysToRemove = trackedBitmaps.keys.take(count)
        keysToRemove.forEach { recycle(it) }
    }

    fun currentMemoryBytes(): Long {
        return trackedBitmaps.values.sumOf { ref ->
            ref.get()?.let { it.width * it.height * 4L } ?: 0L
        }
    }
}
