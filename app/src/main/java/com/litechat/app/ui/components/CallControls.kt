package com.litechat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.litechat.app.ui.theme.CallActive
import com.litechat.app.ui.theme.CallEnded

@Composable
fun CallControls(
    isMuted: Boolean,
    isVideoEnabled: Boolean,
    isSpeakerEnabled: Boolean,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    showVideoControls: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CallButton(
            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            isActive = !isMuted,
            onClick = onToggleMute,
            activeColor = MaterialTheme.colorScheme.primary
        )

        CallButton(
            icon = if (isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            isActive = isSpeakerEnabled,
            onClick = onToggleSpeaker,
            activeColor = MaterialTheme.colorScheme.primary
        )

        if (showVideoControls) {
            CallButton(
                icon = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                isActive = isVideoEnabled,
                onClick = onToggleVideo,
                activeColor = MaterialTheme.colorScheme.primary
            )
        }

        CallButton(
            icon = Icons.Default.CallEnd,
            isActive = false,
            onClick = onEndCall,
            activeColor = CallEnded
        )
    }
}

@Composable
private fun CallButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                if (isActive) activeColor.copy(alpha = 0.2f)
                else Color.Gray.copy(alpha = 0.3f)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) activeColor else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}
