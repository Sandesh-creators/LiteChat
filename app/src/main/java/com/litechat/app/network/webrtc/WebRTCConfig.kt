package com.litechat.app.network.webrtc

import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

object WebRTCConfig {

    const val AUDIO_BITRATE_BPS = 12000
    const val AUDIO_MIN_BITRATE_BPS = 6000
    const val AUDIO_MAX_BITRATE_BPS = 12000

    const val VIDEO_BITRATE_BPS = 200000
    const val VIDEO_MIN_BITRATE_BPS = 50000
    const val VIDEO_MAX_BITRATE_BPS = 300000
    const val VIDEO_FPS = 15
    const val VIDEO_WIDTH = 480
    const val VIDEO_HEIGHT = 360

    fun createPeerConnectionConfig(
        iceServers: List<PeerConnection.IceServer>
    ): PeerConnection.RTCConfiguration {
        return PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continuousGatheringPolicy = PeerConnection.ContinuousGatheringPolicy.GATHER_CONTINUALLY
            iceCandidatePoolSize = 2
            iceTransportsType = PeerConnection.IceTransportsType.ALL
            enableDtlsSrtp = true
            enableSctpDataChannels = false
            enableRtpDataChannels = true
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            networkPreference = PeerConnection.NetworkPreference.LOW_LATENCY
        }
    }

    fun createAudioConstraints(): MediaConstraints {
        return MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googTypingNoiseDetection", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("minBitrate", AUDIO_MIN_BITRATE_BPS.toString()))
            mandatory.add(MediaConstraints.KeyValuePair("maxBitrate", AUDIO_MAX_BITRATE_BPS.toString()))
        }
    }

    fun createVideoConstraints(): MediaConstraints {
        return MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("minHeight", "360"))
            mandatory.add(MediaConstraints.KeyValuePair("maxHeight", "480"))
            mandatory.add(MediaConstraints.KeyValuePair("minWidth", "480"))
            mandatory.add(MediaConstraints.KeyValuePair("maxWidth", "640"))
            mandatory.add(MediaConstraints.KeyValuePair("minFrameRate", "15"))
            mandatory.add(MediaConstraints.KeyValuePair("maxFrameRate", "20"))
        }
    }

    fun createOfferConstraints(): MediaConstraints {
        return MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        }
    }

    fun createAnswerConstraints(): MediaConstraints {
        return MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        }
    }

    fun getDefaultIceServers(): List<PeerConnection.IceServer> {
        return listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("turn:turn.litechat.internal:3478")
                .setUsername("litechat")
                .setPassword("litechat_turn_secret")
                .createIceServer()
        )
    }

    fun initializePeerConnectionFactory(context: android.content.Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    fun createPeerConnectionFactory(eglContext: EglBase.EglBaseContext): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(org.webrtc.DefaultVideoEncoderFactory(eglContext, true, true))
            .setVideoDecoderFactory(org.webrtc.DefaultVideoDecoderFactory(eglContext))
            .createPeerConnectionFactory()
    }

    fun forceVideoBitrate(peerConnection: PeerConnection) {
        val sender = peerConnection.senders.find { it.track?.kind() == "video" }
        sender?.let {
            val params = it.parameters
            params.encodings.forEach { encoding ->
                encoding.maxBitrate = VIDEO_MAX_BITRATE_BPS
                encoding.minBitrate = VIDEO_MIN_BITRATE_BPS
                encoding.bitratePriority = 1.0
            }
            it.parameters = params
        }
    }

    fun forceAudioBitrate(peerConnection: PeerConnection) {
        val sender = peerConnection.senders.find { it.track?.kind() == "audio" }
        sender?.let {
            val params = it.parameters
            params.encodings.forEach { encoding ->
                encoding.maxBitrate = AUDIO_MAX_BITRATE_BPS
                encoding.minBitrate = AUDIO_MIN_BITRATE_BPS
                encoding.bitratePriority = 1.0
            }
            it.parameters = params
        }
    }

    fun parseIceCandidate(payload: String): IceCandidate? {
        val parts = payload.split("|")
        if (parts.size >= 3) {
            return IceCandidate(
                parts[0],
                parts[1].toIntOrNull() ?: 0,
                parts[2]
            )
        }
        return null
    }

    fun iceCandidateToString(candidate: IceCandidate): String {
        return "${candidate.sdpMid}|${candidate.sdpMLineIndex}|${candidate.sdp}"
    }
}
