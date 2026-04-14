package com.johnny.tier1bankdemo.features.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecoveryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecoveryUiState())
    val uiState: StateFlow<RecoveryUiState> = _uiState.asStateFlow()

    fun onAction(action: RecoveryAction) {
        when (action) {
            is RecoveryAction.OnEmailChanged ->
                _uiState.update { it.copy(email = action.value, errorMessage = null) }

            is RecoveryAction.OnSubmitClicked -> submitRecovery()

            // Navigation back is handled by the screen
            is RecoveryAction.OnBackClicked -> Unit
        }
    }

    private fun submitRecovery() {
        val email = _uiState.value.email

        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(1000) // Stub: simulates sending a reset email
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
