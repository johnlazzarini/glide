package com.johnny.tier1bankdemo.features.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val VERIFICATION_THRESHOLD = 1000.0 // Transfers above this require verification

class TransferViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    fun onAction(action: TransferAction) {
        when (action) {
            is TransferAction.OnAmountChanged ->
                _uiState.update { it.copy(amount = action.value, errorMessage = null) }

            is TransferAction.OnRecipientChanged ->
                _uiState.update { it.copy(recipient = action.value, errorMessage = null) }

            is TransferAction.OnSubmitClicked -> submitTransfer()

            // Screen has navigated to Verification — clear the flag so it doesn't re-trigger
            is TransferAction.OnVerificationNavigated ->
                _uiState.update { it.copy(needsVerification = false) }

            // Verification succeeded — complete the transfer
            is TransferAction.OnVerificationSucceeded -> completeTransfer()

            // Verification failed or was cancelled — surface an error
            is TransferAction.OnVerificationFailed ->
                _uiState.update {
                    it.copy(errorMessage = "Verification failed. Transfer has been cancelled.")
                }

            is TransferAction.OnBackClicked -> Unit
        }
    }

    private fun submitTransfer() {
        val state = _uiState.value

        if (state.recipient.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a recipient.") }
            return
        }

        val amountValue = state.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount greater than \$0.") }
            return
        }

        if (amountValue > VERIFICATION_THRESHOLD) {
            // High-value transfer — require identity verification before completing
            _uiState.update { it.copy(errorMessage = null, needsVerification = true) }
        } else {
            // Low-value transfer — complete immediately
            completeTransfer()
        }
    }

    /**
     * Finalises the transfer. Called either directly (amount ≤ threshold) or
     * after successful verification (amount > threshold).
     */
    private fun completeTransfer() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, needsVerification = false) }
        viewModelScope.launch {
            delay(800) // Stub: simulates the API call to submit the transfer
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
