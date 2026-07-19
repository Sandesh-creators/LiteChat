package com.litechat.app.network.p2p

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.litechat.app.core.security.CryptoManager
import com.litechat.app.media.UriManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID

class P2PTransferManager(
    private val context: Context,
    private val uriManager: UriManager
) {

    companion object {
        private const val TAG = "P2PTransfer"
        private const val TRANSFER_PORT = 18900
        private const val CHUNK_SIZE = 8192
    }

    enum class TransferState {
        IDLE, CONNECTING, TRANSFERRING, COMPLETED, FAILED
    }

    data class TransferProgress(
        val transferId: String,
        val state: TransferState,
        val bytesTransferred: Long = 0,
        val totalBytes: Long = 0,
        val speed: Long = 0
    )

    private val _transferState = MutableStateFlow(TransferProgress("", TransferState.IDLE))
    val transferState: StateFlow<TransferProgress> = _transferState.asStateFlow()

    private var serverSocket: ServerSocket? = null

    suspend fun sendFile(
        peerIp: String,
        peerPort: Int,
        fileUri: Uri,
        transferId: String = UUID.randomUUID().toString()
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            _transferState.value = TransferProgress(transferId, TransferState.CONNECTING)

            val socket = Socket(peerIp, peerPort)
            val outputStream = socket.getOutputStream()

            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                val fileSize = getFileSize(fileUri)
                _transferState.value = TransferProgress(transferId, TransferState.TRANSFERRING, 0, fileSize)

                sendHeader(outputStream, fileSize, fileUri)
                transferData(inputStream, outputStream, transferId, fileSize)

                outputStream.flush()
                socket.close()

                _transferState.value = TransferProgress(transferId, TransferState.COMPLETED, fileSize, fileSize)
                true
            } ?: run {
                _transferState.value = TransferProgress(transferId, TransferState.FAILED)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Send failed: ${e.message}")
            _transferState.value = TransferProgress(transferId, TransferState.FAILED)
            false
        }
    }

    private fun sendHeader(outputStream: OutputStream, fileSize: Long, uri: Uri) {
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val header = "$fileSize|$mimeType\n"
        outputStream.write(header.toByteArray())
    }

    private fun transferData(
        inputStream: InputStream,
        outputStream: OutputStream,
        transferId: String,
        totalBytes: Long
    ) {
        val buffer = ByteArray(CHUNK_SIZE)
        var bytesTransferred = 0L
        val startTime = System.currentTimeMillis()

        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
            bytesTransferred += bytesRead

            val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            val speed = (bytesTransferred * 1000) / elapsed
            _transferState.value = TransferProgress(
                transferId, TransferState.TRANSFERRING, bytesTransferred, totalBytes, speed
            )
        }
    }

    suspend fun receiveFile(outputFileUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            serverSocket = ServerSocket(TRANSFER_PORT)
            _transferState.value = TransferProgress("", TransferState.CONNECTING)

            val clientSocket = serverSocket?.accept() ?: return@withContext false
            val inputStream = clientSocket.getInputStream()

            val header = readHeader(inputStream)
            val fileSize = header.first

            _transferState.value = TransferProgress("", TransferState.TRANSFERRING, 0, fileSize)

            context.contentResolver.openOutputStream(outputFileUri)?.use { outputStream ->
                transferData(inputStream, outputStream, "", fileSize)
            }

            clientSocket.close()
            _transferState.value = TransferProgress("", TransferState.COMPLETED, fileSize, fileSize)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Receive failed: ${e.message}")
            _transferState.value = TransferProgress("", TransferState.FAILED)
            false
        } finally {
            serverSocket?.close()
        }
    }

    private fun readHeader(inputStream: InputStream): Pair<Long, String> {
        val headerBuilder = StringBuilder()
        var byte: Int
        while (inputStream.read().also { byte = it } != -1) {
            if (byte == '\n'.code) break
            headerBuilder.append(byte.toChar())
        }
        val parts = headerBuilder.toString().split("|")
        val size = parts.getOrElse(0) { "0" }.toLongOrNull() ?: 0L
        val mimeType = parts.getOrElse(1) { "application/octet-stream" }
        return Pair(size, mimeType)
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Stop server error: ${e.message}")
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
            val size = pfd?.statSize ?: 0L
            pfd?.close()
            if (size > 0) size else 0L
        } catch (e: Exception) {
            Log.w(TAG, "Could not get file size via ParcelFileDescriptor, falling back to available()")
            try {
                context.contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: 0L
            } catch (e2: Exception) {
                0L
            }
        }
    }
}
