package com.litechat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.litechat.app.data.db.entity.MessageEntity
import com.litechat.app.data.db.entity.MessageStatus
import com.litechat.app.ui.theme.StatusDelivered
import com.litechat.app.ui.theme.StatusPending
import com.litechat.app.ui.theme.StatusSent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageEntity,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (message.isOutgoing) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val alignment = if (message.isOutgoing) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isOutgoing) 16.dp else 4.dp,
                        bottomEnd = if (message.isOutgoing) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                if (message.isOutgoing) {
                    StatusIcon(status = message.status)
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(status: MessageStatus) {
    val (icon, color) = when (status) {
        MessageStatus.PENDING -> Icons.Default.Schedule to StatusPending
        MessageStatus.SENT_TO_QUEUE -> Icons.Default.Check to StatusSent
        MessageStatus.STREAMING -> Icons.Default.Check to StatusSent
        MessageStatus.DELIVERED -> Icons.Default.CheckCircle to StatusDelivered
        MessageStatus.PURGED -> Icons.Default.CheckCircle to StatusDelivered
    }
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.padding(start = 4.dp)
    )
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
