package com.johnny.tier1bankdemo.features.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordResetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

    fun onAction(action: PasswordResetAction) {
        when (action) {
            is PasswordResetAction.OnNewPasswordChanged ->
                _uiState.update { it.copy(newPassword = action.value, errorMessage = null) }
            is PasswordResetAction.OnConfirmPasswordChanged ->
                _uiState.update { it.copy(confirmPassword = action.value, errorMessage = null) }
            is PasswordResetAction.OnSubmitClicked -> submitReset()
        }
    }

    private fun submitReset() {
        val state = _uiState.value

        if (state.newPassword.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters.") }
            return
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            delay(800) // Stub: simulates password update API call
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
