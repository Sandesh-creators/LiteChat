package com.litechat.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    data class SettingsState(
        val userName: String = "User",
        val statusMessage: String = "Hey, I'm using LiteChat",
        val isNotificationsEnabled: Boolean = true,
        val isDataSaverEnabled: Boolean = true,
        val isAutoDownloadMedia: Boolean = false,
        val isLowBandwidthMode: Boolean = true,
        val maxImageResolution: Int = 150,
        val videoQuality: String = "360p",
        val audioQuality: String = "Low (6-12 kbps)",
        val storageUsed: String = "0 MB"
    )

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun updateUserName(name: String) {
        _state.value = _state.value.copy(userName = name)
    }

    fun updateStatusMessage(status: String) {
        _state.value = _state.value.copy(statusMessage = status)
    }

    fun toggleNotifications() {
        _state.value = _state.value.copy(isNotificationsEnabled = !_state.value.isNotificationsEnabled)
    }

    fun toggleDataSaver() {
        _state.value = _state.value.copy(isDataSaverEnabled = !_state.value.isDataSaverEnabled)
    }

    fun toggleAutoDownload() {
        _state.value = _state.value.copy(isAutoDownloadMedia = !_state.value.isAutoDownloadMedia)
    }

    fun toggleLowBandwidth() {
        _state.value = _state.value.copy(isLowBandwidthMode = !_state.value.isLowBandwidthMode)
    }
}
