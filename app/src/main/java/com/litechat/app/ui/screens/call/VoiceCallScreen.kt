package com.litechat.app.ui.screens.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.litechat.app.core.di.ServiceLocator
import com.litechat.app.ui.components.CallControls

@Composable
fun VoiceCallScreen(onEndCall: () -> Unit) {
    val context = LocalContext.current
    val callManager = remember { ServiceLocator.provideCallManager(context) }
    val currentCall by callManager.currentCall.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 80.dp)
        ) {
            Text(
                text = currentCall.peerId.ifEmpty { "Unknown" },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = when (currentCall.state.name) {
                    "OUTGOING" -> "Calling..."
                    "RINGING" -> "Ringing..."
                    "CONNECTING" -> "Connecting..."
                    "ACTIVE" -> formatDuration(currentCall.duration)
                    "RECONNECTING" -> "Reconnecting..."
                    else -> "Call ended"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        CallControls(
            isMuted = currentCall.isMuted,
            isVideoEnabled = false,
            isSpeakerEnabled = currentCall.isSpeakerEnabled,
            onToggleMute = { callManager.toggleMute() },
            onToggleVideo = {},
            onToggleSpeaker = { callManager.toggleSpeaker() },
            onEndCall = {
                callManager.endCall()
                onEndCall()
            },
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / 60000) % 60
    val hours = durationMs / 3600000
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
