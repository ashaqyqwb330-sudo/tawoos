package com.example.service

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthService(context: Context) {
    private val prefs = context.getSharedPreferences("tawus_auth", Context.MODE_PRIVATE)
    
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("loggedIn", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun loginWithPin(pin: String): Boolean {
        if (pin == "123456") {
            prefs.edit().putBoolean("loggedIn", true).apply()
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    fun logout() {
        prefs.edit().putBoolean("loggedIn", false).apply()
        _isLoggedIn.value = false
    }
}
