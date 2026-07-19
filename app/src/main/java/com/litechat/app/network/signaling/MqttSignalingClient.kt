package com.litechat.app.network.signaling

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

@Serializable
data class SignalingMessage(
    val type: SignalingType,
    val senderId: String,
    val receiverId: String,
    val conversationId: String = "",
    val payload: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageId: String = ""
)

@Serializable
enum class SignalingType {
    TEXT,
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    CALL_ACCEPT,
    CALL_REJECT,
    CALL_END,
    MEDIA_REQUEST,
    MEDIA_OFFER,
    FILE_CHUNK,
    DELIVERY_ACK,
    TYPING,
    PRESENCE
}

class MqttSignalingClient(private val context: Context) {

    companion object {
        private const val TAG = "SignalingClient"
        private const val WS_URL = "wss://signal.litechat.internal/ws"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _incomingMessages = MutableSharedFlow<SignalingMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<SignalingMessage> = _incomingMessages.asSharedFlow()

    private var onConnectedCallback: (() -> Unit)? = null
    private var onDisconnectedCallback: (() -> Unit)? = null
    private var userId: String = ""

    fun connect(uId: String, onConnected: () -> Unit = {}, onDisconnected: () -> Unit = {}) {
        userId = uId
        onConnectedCallback = onConnected
        onDisconnectedCallback = onDisconnected

        val request = Request.Builder()
            .url("$WS_URL?userId=$userId")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                onConnectedCallback?.invoke()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = json.decodeFromString<SignalingMessage>(text)
                    scope.launch {
                        _incomingMessages.emit(message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse message: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code $reason")
                onDisconnectedCallback?.invoke()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}")
                onDisconnectedCallback?.invoke()
            }
        })
    }

    fun sendMessage(message: SignalingMessage) {
        sendRaw(message)
    }

    fun sendSignaling(message: SignalingMessage) {
        sendRaw(message)
    }

    private fun sendRaw(message: SignalingMessage) {
        try {
            val payload = json.encodeToString(message)
            webSocket?.send(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Send error: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Client disconnect")
            webSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        }
    }

    fun isConnected(): Boolean = webSocket != null
}
