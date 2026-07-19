package com.litechat.app.ui.screens.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.litechat.app.core.di.ServiceLocator
import com.litechat.app.ui.components.CallControls

@Composable
fun VideoCallScreen(onEndCall: () -> Unit) {
    val context = LocalContext.current
    val callManager = remember { ServiceLocator.provideCallManager(context) }
    val currentCall by callManager.currentCall.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Remote Video",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(160.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Local",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        CallControls(
            isMuted = currentCall.isMuted,
            isVideoEnabled = currentCall.isVideoEnabled,
            isSpeakerEnabled = currentCall.isSpeakerEnabled,
            onToggleMute = { callManager.toggleMute() },
            onToggleVideo = { callManager.toggleVideo() },
            onToggleSpeaker = { callManager.toggleSpeaker() },
            onEndCall = {
                callManager.endCall()
                onEndCall()
            },
            showVideoControls = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
