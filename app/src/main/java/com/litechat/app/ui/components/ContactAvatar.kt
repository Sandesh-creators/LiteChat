package com.litechat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.litechat.app.ui.theme.OnlineGreen

@Composable
fun ContactAvatar(
    name: String,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    val bgColor = rememberAvatarColor(name)

    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

private fun rememberAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF1B5E20),
        Color(0xFF0D47A1),
        Color(0xFFB71C1C),
        Color(0xFF4A148C),
        Color(0xFFE65100),
        Color(0xFF006064),
        Color(0xFF880E4F),
        Color(0xFF33691E)
    )
    return colors[name.hashCode().and(0x7FFFFFFF) % colors.size]
}
