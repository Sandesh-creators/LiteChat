package com.litechat.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.litechat.app.core.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userProfileRepo = ServiceLocator.provideUserProfileRepository(application)

    data class SettingsState(
        val displayName: String = "",
        val username: String = "",
        val statusMessage: String = "Hey, I'm using LiteChat",
        val isNotificationsEnabled: Boolean = true,
        val isDataSaverEnabled: Boolean = true,
        val isAutoDownloadMedia: Boolean = false,
        val isLowBandwidthMode: Boolean = true,
        val maxImageResolution: Int = 150,
        val videoQuality: String = "360p",
        val audioQuality: String = "Low (6-12 kbps)",
        val usernameError: String = "",
        val usernameSuccess: Boolean = false
    )

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = userProfileRepo.getProfileSync()
            if (profile != null) {
                _state.value = _state.value.copy(
                    displayName = profile.displayName,
                    username = profile.username,
                    statusMessage = profile.statusMessage
                )
            }
        }
    }

    fun updateDisplayName(name: String) {
        _state.value = _state.value.copy(displayName = name)
        viewModelScope.launch {
            userProfileRepo.changeDisplayName(name)
        }
    }

    fun changeUsername(newUsername: String) {
        _state.value = _state.value.copy(usernameError = "", usernameSuccess = false)

        if (newUsername.length < 3) {
            _state.value = _state.value.copy(usernameError = "Username must be at least 3 characters")
            return
        }
        if (!newUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _state.value = _state.value.copy(usernameError = "Only letters, numbers, and underscores allowed")
            return
        }

        viewModelScope.launch {
            val profile = userProfileRepo.getProfileSync()
            val myUserId = profile?.id ?: ""
            val taken = userProfileRepo.isUsernameTakenByOther(newUsername, myUserId)
            if (taken) {
                _state.value = _state.value.copy(usernameError = "Username is already taken")
            } else {
                val success = userProfileRepo.changeUsername(newUsername)
                if (success) {
                    _state.value = _state.value.copy(
                        username = newUsername,
                        usernameSuccess = true,
                        usernameError = ""
                    )
                } else {
                    _state.value = _state.value.copy(usernameError = "Failed to change username")
                }
            }
        }
    }

    fun updateStatus(status: String) {
        _state.value = _state.value.copy(statusMessage = status)
        viewModelScope.launch {
            userProfileRepo.changeStatus(status)
        }
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
