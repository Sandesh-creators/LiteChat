package org.webrtc

import android.content.Context

/**
 * Stub classes for WebRTC API.
 * To use real WebRTC, place google-webrtc AAR in app/libs/ and add:
 *   implementation(files("libs/google-webrtc-1.0.32006.aar"))
 * then delete this stub file.
 */

class DataChannel
class MediaStream
class RtpReceiver {
    val track: MediaStreamTrack? get() = null
}
class MediaStreamTrack {
    fun kind(): String = ""
}
class AudioSource
class VideoSource
class VideoCapturer

data class SessionDescription(val type: Type, val sdp: String) {
    enum class Type { OFFER, ANSWER, PRANSWER, ROLLBACK }
}

data class IceCandidate(
    val sdpMid: String?,
    val sdpMLineIndex: Int,
    val sdp: String
)

object EglBase {
    fun create(): EglBaseContext = EglBaseContext()

    class EglBaseContext {
        val eglBaseContext: Any get() = this
    }
}

class PeerConnectionFactory {
    class InitializationOptions private constructor(val context: Context) {
        class Builder(private val context: Context) {
            fun createInitializationOptions() = InitializationOptions(context)
        }
        companion object {
            fun builder(context: Context) = Builder(context)
        }
    }

    class Builder {
        fun setVideoEncoderFactory(factory: Any?) = this
        fun setVideoDecoderFactory(factory: Any?) = this
        fun createPeerConnectionFactory() = PeerConnectionFactory()
    }

    companion object {
        fun initialize(options: InitializationOptions) {}
        fun builder(): Builder = Builder()
    }

    fun createPeerConnection(config: PeerConnection.RTCConfiguration, observer: PeerConnection.Observer): PeerConnection? = null
}

class DefaultVideoEncoderFactory(context: Any, private1: Boolean, private2: Boolean)
class DefaultVideoDecoderFactory(context: Any)

class PeerConnection {
    enum class SignalingState { STABLE, HAVE_LOCAL_OFFER, HAVE_REMOTE_OFFER, HAVE_LOCAL_PRANSWER, HAVE_REMOTE_PRANSWER, CLOSED }
    enum class IceConnectionState { NEW, CHECKING, CONNECTED, COMPLETED, FAILED, DISCONNECTED, CLOSED }
    enum class IceGatheringState { NEW, GATHERING, COMPLETE }
    enum class SdpSemantics(val value: Int) { UNIFIED_PLAN(0) }
    enum class IceTransportsType { ALL, RELAY, NOHOST, NONE }
    enum class TcpCandidatePolicy { ENABLED, DISABLED }
    enum class NetworkPreference { LOW_LATENCY, HIGH_BANDWIDTH, BALANCED }
    enum class ContinuousGatheringPolicy { GATHER_CONTINUALLY, GATHER_ONCE }

    class RTCConfiguration(iceServers: List<IceServer>) {
        var sdpSemantics: SdpSemantics = SdpSemantics.UNIFIED_PLAN
        var continuousGatheringPolicy: ContinuousGatheringPolicy = ContinuousGatheringPolicy.GATHER_CONTINUALLY
        var iceCandidatePoolSize: Int = 0
        var iceTransportsType: IceTransportsType = IceTransportsType.ALL
        var enableDtlsSrtp: Boolean = false
        var enableSctpDataChannels: Boolean = false
        var enableRtpDataChannels: Boolean = false
        var tcpCandidatePolicy: TcpCandidatePolicy = TcpCandidatePolicy.ENABLED
        var networkPreference: NetworkPreference = NetworkPreference.LOW_LATENCY
    }

    data class RtpEncodingParameters(
        var maxBitrate: Int = 0,
        var minBitrate: Int = 0,
        var bitratePriority: Double = 1.0
    )

    class RtpSendParameters {
        var encodings: Array<RtpEncodingParameters> = emptyArray()
    }

    class RtpSender(val track: MediaStreamTrack?) {
        var parameters: RtpSendParameters = RtpSendParameters()
    }

    class IceServer {
        companion object {
            fun builder(uri: String): IceServerBuilder = IceServerBuilder()
        }
    }

    class IceServerBuilder {
        fun setUsername(u: String): IceServerBuilder = this
        fun setPassword(p: String): IceServerBuilder = this
        fun createIceServer(): IceServer = IceServer()
    }

    interface Observer {
        fun onSignalingChange(state: SignalingState?) {}
        fun onIceChange(state: IceGatheringState?) {}
        fun onIceConnectionChange(state: IceConnectionState?) {}
        fun onIceConnectionReceivingChange(receiving: Boolean) {}
        fun onIceCandidatesRemoved(candidates: MutableList<IceCandidate>?) {}
        fun onAddStream(stream: MediaStream?) {}
        fun onRemoveStream(stream: MediaStream?) {}
        fun onDataChannel(channel: DataChannel?) {}
        fun onRenegotiationNeeded() {}
        fun onIceCandidate(candidate: IceCandidate?) {}
        fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>) {}
    }

    val senders: List<RtpSender> get() = emptyList()

    fun setLocalDescription(observer: SdpObserver, sdp: SessionDescription) {}
    fun setRemoteDescription(observer: SdpObserver, sdp: SessionDescription) {}
    fun createOffer(observer: SdpObserver, constraints: MediaConstraints) {}
    fun createAnswer(observer: SdpObserver, constraints: MediaConstraints) {}
    fun addIceCandidate(candidate: IceCandidate) {}
    fun close() {}
}

interface SdpObserver {
    fun onCreateSuccess(sdp: SessionDescription?)
    fun onSetSuccess()
    fun onCreateFailure(msg: String?)
    fun onSetFailure(msg: String?)
}

class MediaConstraints {
    val mandatory = mutableListOf<KeyValuePair>()
    val optional = mutableListOf<KeyValuePair>()

    class KeyValuePair(val key: String, val value: String)
}
