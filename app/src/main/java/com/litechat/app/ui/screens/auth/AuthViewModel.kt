package com.litechat.app.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.litechat.app.core.di.ServiceLocator
import com.litechat.app.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userProfileRepo = ServiceLocator.provideUserProfileRepository(application)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userProfileRepo.getProfileSync()
            if (profile != null) {
                _isAuthenticated.value = true
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank()) {
            _error.value = "Username cannot be empty"
            return
        }
        if (username.length < 3) {
            _error.value = "Username must be at least 3 characters"
            return
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _error.value = "Only letters, numbers, and underscores allowed"
            return
        }
        if (password.isNotBlank() && password.length < 4) {
            _error.value = "Password must be at least 4 characters"
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val existingProfile = userProfileRepo.getProfileSync()
                if (existingProfile != null) {
                    _isAuthenticated.value = true
                    _isLoading.value = false
                    return@launch
                }

                val taken = userProfileRepo.isUsernameTaken(username)
                if (taken) {
                    _error.value = "Username is already taken"
                    _isLoading.value = false
                    return@launch
                }

                userProfileRepo.initializeProfile(username, username)
                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
