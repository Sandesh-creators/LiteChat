package com.litechat.app.core.memory

import java.util.concurrent.ConcurrentLinkedQueue

class ObjectPool<T : Any>(
    private val factory: () -> T,
    private val reset: (T) -> Unit = {},
    private val maxSize: Int = 32
) {
    private val pool = ConcurrentLinkedQueue<T>()

    fun acquire(): T {
        return pool.poll() ?: factory()
    }

    fun release(obj: T) {
        if (pool.size < maxSize) {
            reset(obj)
            pool.offer(obj)
        }
    }

    fun clear() {
        pool.clear()
    }

    fun size(): Int = pool.size
}

object PoolManager {
    val byteArrayPool = ObjectPool(
        factory = { ByteArray(8192) },
        reset = { it.fill(0) },
        maxSize = 16
    )
    val stringBuilderPool = ObjectPool(
        factory = { StringBuilder(256) },
        reset = { it.clear() },
        maxSize = 8
    )
    val messageBufferPool = ObjectPool(
        factory = { mutableListOf<String>() },
        reset = { it.clear() },
        maxSize = 8
    )

    fun releaseAll() {
        byteArrayPool.clear()
        stringBuilderPool.clear()
        messageBufferPool.clear()
    }
}
