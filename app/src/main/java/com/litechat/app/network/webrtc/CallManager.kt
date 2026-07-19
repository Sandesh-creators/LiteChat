package com.litechat.app.network.webrtc

import android.content.Context
import android.util.Log
import com.litechat.app.network.signaling.MqttSignalingClient
import com.litechat.app.network.signaling.SignalingMessage
import com.litechat.app.network.signaling.SignalingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.DataChannel
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

class CallManager(private val context: Context) {

    companion object {
        private const val TAG = "CallManager"
    }

    enum class CallState {
        IDLE, OUTGOING, RINGING, CONNECTING, ACTIVE, RECONNECTING, ENDED
    }

    enum class CallType {
        AUDIO, VIDEO
    }

    data class CallInfo(
        val callId: String,
        val peerId: String,
        val callType: CallType,
        val state: CallState = CallState.IDLE,
        val duration: Long = 0L,
        val isMuted: Boolean = false,
        val isVideoEnabled: Boolean = true,
        val isSpeakerEnabled: Boolean = false
    )

    private val _currentCall = MutableStateFlow(CallInfo("", "", CallType.AUDIO))
    val currentCall: StateFlow<CallInfo> = _currentCall.asStateFlow()

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private val eglContext = EglBase.create()

    var signalingClient: MqttSignalingClient? = null

    private fun initializeFactory() {
        if (peerConnectionFactory != null) return
        WebRTCConfig.initializePeerConnectionFactory(context)
        peerConnectionFactory = WebRTCConfig.createPeerConnectionFactory(eglContext)
    }

    fun startCall(peerId: String, callType: CallType) {
        initializeFactory()
        val callId = "call_${System.currentTimeMillis()}"
        _currentCall.value = CallInfo(callId, peerId, callType, CallState.OUTGOING)

        val config = WebRTCConfig.createPeerConnectionConfig(WebRTCConfig.getDefaultIceServers())
        peerConnection = peerConnectionFactory?.createPeerConnection(config, createPeerObserver())

        val offerConstraints = WebRTCConfig.createOfferConstraints()
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peerConnection?.setLocalDescription(this, it)
                    signalingClient?.sendSignaling(
                        SignalingMessage(
                            type = SignalingType.OFFER,
                            senderId = "local_user",
                            receiverId = peerId,
                            payload = it.sdp,
                            conversationId = callId
                        )
                    )
                }
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(msg: String?) { Log.e(TAG, "Offer creation failed: $msg") }
            override fun onSetFailure(msg: String?) { Log.e(TAG, "SDP set failed: $msg") }
        }, offerConstraints)
    }

    fun answerCall(signaling: SignalingMessage) {
        initializeFactory()
        _currentCall.value = _currentCall.value.copy(state = CallState.CONNECTING)

        val config = WebRTCConfig.createPeerConnectionConfig(WebRTCConfig.getDefaultIceServers())
        peerConnection = peerConnectionFactory?.createPeerConnection(config, createPeerObserver())

        val answerConstraints = WebRTCConfig.createAnswerConstraints()
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                peerConnection?.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(sdp: SessionDescription?) {
                        sdp?.let {
                            peerConnection?.setLocalDescription(this, it)
                            signalingClient?.sendSignaling(
                                SignalingMessage(
                                    type = SignalingType.ANSWER,
                                    senderId = "local_user",
                                    receiverId = signaling.senderId,
                                    payload = it.sdp,
                                    conversationId = signaling.conversationId
                                )
                            )
                            _currentCall.value = _currentCall.value.copy(
                                state = CallState.ACTIVE,
                                callId = signaling.conversationId
                            )
                        }
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(msg: String?) { Log.e(TAG, "Answer creation failed: $msg") }
                    override fun onSetFailure(msg: String?) { Log.e(TAG, "SDP set failed: $msg") }
                }, answerConstraints)
            }
            override fun onCreateSuccess(sdp: SessionDescription?) {}
            override fun onSetFailure(msg: String?) { Log.e(TAG, "Remote SDP set failed: $msg") }
            override fun onCreateFailure(msg: String?) {}
        }, SessionDescription(SessionDescription.Type.OFFER, signaling.payload))
    }

    fun handleAnswer(signaling: SignalingMessage) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "Remote answer set successfully")
                peerConnection?.let {
                    WebRTCConfig.forceVideoBitrate(it)
                    WebRTCConfig.forceAudioBitrate(it)
                }
                _currentCall.value = _currentCall.value.copy(state = CallState.ACTIVE)
            }
            override fun onSetFailure(msg: String?) { Log.e(TAG, "Set remote answer failed: $msg") }
            override fun onCreateSuccess(sdp: SessionDescription?) {}
            override fun onCreateFailure(msg: String?) {}
        }, SessionDescription(SessionDescription.Type.ANSWER, signaling.payload))
    }

    fun handleIceCandidate(signaling: SignalingMessage) {
        try {
            val candidate = WebRTCConfig.parseIceCandidate(signaling.payload)
            candidate?.let { peerConnection?.addIceCandidate(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle ICE candidate: ${e.message}")
        }
    }

    fun endCall() {
        peerConnection?.close()
        peerConnection = null
        _currentCall.value = CallInfo("", "", CallType.AUDIO)
        Log.d(TAG, "Call ended")
    }

    fun toggleMute() {
        val current = _currentCall.value
        _currentCall.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleVideo() {
        val current = _currentCall.value
        _currentCall.value = current.copy(isVideoEnabled = !current.isVideoEnabled)
    }

    fun toggleSpeaker() {
        val current = _currentCall.value
        _currentCall.value = current.copy(isSpeakerEnabled = !current.isSpeakerEnabled)
    }

    private fun createPeerObserver(): PeerConnection.Observer {
        return object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        _currentCall.value = _currentCall.value.copy(state = CallState.ACTIVE)
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED -> {
                        _currentCall.value = _currentCall.value.copy(state = CallState.RECONNECTING)
                    }
                    PeerConnection.IceConnectionState.FAILED,
                    PeerConnection.IceConnectionState.CLOSED -> {
                        endCall()
                    }
                    else -> {}
                }
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceCandidatesRemoved(candidates: MutableList<IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {
                Log.d(TAG, "Remote stream added")
            }
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    val payload = WebRTCConfig.iceCandidateToString(it)
                    signalingClient?.sendSignaling(
                        SignalingMessage(
                            type = SignalingType.ICE_CANDIDATE,
                            senderId = "local_user",
                            receiverId = _currentCall.value.peerId,
                            payload = payload,
                            conversationId = _currentCall.value.callId
                        )
                    )
                }
            }
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>) {}
        }
    }
}
