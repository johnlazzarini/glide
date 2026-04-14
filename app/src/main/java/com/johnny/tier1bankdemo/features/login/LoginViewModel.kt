package com.johnny.tier1bankdemo.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnUsernameChanged ->
                _uiState.update { it.copy(username = action.value, errorMessage = null) }

            is LoginAction.OnPasswordChanged ->
                _uiState.update { it.copy(password = action.value, errorMessage = null) }

            is LoginAction.OnLoginClicked    -> login()

            // Navigation is triggered by the screen; nothing to do in the VM
            is LoginAction.OnForgotPasswordClicked -> Unit
        }
    }

    private fun login() {
        val state = _uiState.value

        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your username and password.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(800) // Stub: simulates a network call

            // Stub credentials — replace with real auth logic later
            if (state.username == "demo" && state.password == "password") {
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Invalid username or password.")
                }
            }
        }
    }
}
