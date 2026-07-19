package com.litechat.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.litechat.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(context.applicationContext as android.app.Application) }
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Profile") {
                SettingsItem(
                    label = "Name",
                    value = state.userName
                )
                SettingsItem(
                    label = "Status",
                    value = state.statusMessage
                )
            }

            SettingsSection(title = "Data & Storage") {
                SettingsToggleItem(
                    label = "Data Saver",
                    description = "Reduce data usage across the app",
                    checked = state.isDataSaverEnabled,
                    onToggle = { viewModel.toggleDataSaver() }
                )
                SettingsToggleItem(
                    label = "Low Bandwidth Mode",
                    description = "Force low bitrate audio/video calls",
                    checked = state.isLowBandwidthMode,
                    onToggle = { viewModel.toggleLowBandwidth() }
                )
                SettingsToggleItem(
                    label = "Auto-download Media",
                    description = "Automatically download received media",
                    checked = state.isAutoDownloadMedia,
                    onToggle = { viewModel.toggleAutoDownload() }
                )
                SettingsItem(
                    label = "Video Quality",
                    value = state.videoQuality
                )
                SettingsItem(
                    label = "Audio Quality",
                    value = state.audioQuality
                )
                SettingsItem(
                    label = "Thumbnail Resolution",
                    value = "${state.maxImageResolution}x${state.maxImageResolution}px"
                )
            }

            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    label = "Push Notifications",
                    description = "Receive notifications for new messages",
                    checked = state.isNotificationsEnabled,
                    onToggle = { viewModel.toggleNotifications() }
                )
            }

            SettingsSection(title = "About") {
                SettingsItem(label = "Version", value = "1.0.0")
                SettingsItem(label = "Build", value = "1")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun SettingsItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun SettingsToggleItem(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
    }
}
