package com.litechat.app.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun login(username: String, password: String) {
        _isAuthenticated.value = true
    }

    fun register(username: String, password: String) {
        _isAuthenticated.value = true
    }
}
