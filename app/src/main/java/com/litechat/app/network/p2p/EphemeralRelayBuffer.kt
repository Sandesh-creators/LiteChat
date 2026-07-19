package com.litechat.app.network.p2p

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

class EphemeralRelayBuffer {

    companion object {
        private const val TAG = "EphemeralRelay"
        private const val BUFFER_TIMEOUT_MS = 30_000L
    }

    data class BufferedChunk(
        val chunkId: String,
        val data: ByteArray,
        val transferId: String,
        val sequenceNumber: Int,
        val createdAt: Long = System.currentTimeMillis()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BufferedChunk) return false
            return chunkId == other.chunkId
        }
        override fun hashCode(): Int = chunkId.hashCode()
    }

    data class TransferState(
        val transferId: String,
        val totalChunks: Int,
        val receivedChunks: MutableSet<Int> = mutableSetOf(),
        val completed: Boolean = false
    )

    private val buffer = ConcurrentHashMap<String, BufferedChunk>()
    private val transfers = ConcurrentHashMap<String, TransferState>()

    fun bufferChunk(chunk: BufferedChunk): Boolean {
        buffer[chunk.chunkId] = chunk

        val state = transfers.getOrPut(chunk.transferId) {
            TransferState(chunk.transferId, 0)
        }
        state.receivedChunks.add(chunk.sequenceNumber)

        cleanupExpired()
        return state.receivedChunks.size >= state.totalChunks && state.totalChunks > 0
    }

    fun getChunk(chunkId: String): BufferedChunk? {
        return buffer.remove(chunkId)
    }

    fun markComplete(transferId: String, totalChunks: Int) {
        transfers[transferId]?.let {
            val completed = it.copy(totalChunks = totalChunks, completed = true)
            transfers[transferId] = completed
        } ?: run {
            transfers[transferId] = TransferState(transferId, totalChunks, completed = true)
        }
    }

    fun wipeTransfer(transferId: String) {
        buffer.entries.removeAll { it.value.transferId == transferId }
        transfers.remove(transferId)
        Log.d(TAG, "Wiped relay buffer for transfer: $transferId")
    }

    fun wipeAll() {
        buffer.clear()
        transfers.clear()
        Log.d(TAG, "Wiped all relay buffers")
    }

    private fun cleanupExpired() {
        val now = System.currentTimeMillis()
        buffer.entries.removeIf { (_, chunk) ->
            now - chunk.createdAt > BUFFER_TIMEOUT_MS
        }
    }

    fun bufferedBytes(): Long = buffer.values.sumOf { it.data.size.toLong() }
}
