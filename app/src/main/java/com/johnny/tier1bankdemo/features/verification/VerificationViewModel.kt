package com.johnny.tier1bankdemo.features.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.johnny.tier1bankdemo.data.verification.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages all verification state transitions.
 *
 * Public API matches the three operations the module supports:
 *   startVerification(useCase)     — creates an attempt, transitions to AwaitingBrowser
 *   resumeVerification(attemptId)  — resolves the attempt, transitions to Success or Failure
 *   getVerificationStatus(attemptId) — polls status without user action
 *
 * All user intents also arrive as [VerificationAction] via [onAction] so the
 * pattern stays consistent with every other ViewModel in this project.
 */
class VerificationViewModel(
    private val coordinator: VerificationCoordinator = VerificationCoordinator()
) : ViewModel() {

    private val _uiState = MutableStateFlow<VerificationUiState>(VerificationUiState.Idle)
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun onAction(action: VerificationAction) {
        when (action) {
            is VerificationAction.StartVerification  -> startVerification(action.useCase)
            is VerificationAction.ResumeVerification -> resumeVerification(action.attemptId)
            is VerificationAction.GetStatus          -> getVerificationStatus(action.attemptId)
            is VerificationAction.OnCodeChanged      -> updateCode(action.value)
            is VerificationAction.OnConfirmClicked   -> confirmFromBrowser()
            is VerificationAction.OnResendClicked    -> _uiState.value = VerificationUiState.Idle
            is VerificationAction.Reset              -> _uiState.value = VerificationUiState.Idle
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Initiates a new verification attempt for [useCase].
     * Transitions: Idle → Starting → AwaitingBrowser
     */
    fun startVerification(useCase: String) {
        _uiState.value = VerificationUiState.Starting(useCase)
        viewModelScope.launch {
            val attempt = coordinator.startVerification(useCase)
            _uiState.value = VerificationUiState.AwaitingBrowser(
                attemptId       = attempt.id,
                verificationUrl = attempt.verificationUrl.orEmpty()
            )
        }
    }

    /**
     * Resolves the pending attempt after the user returns from the browser.
     * Transitions: AwaitingBrowser → CheckingStatus → Success | Failure
     */
    fun resumeVerification(attemptId: String) {
        _uiState.value = VerificationUiState.CheckingStatus(attemptId)
        viewModelScope.launch {
            val status = coordinator.resumeVerification(attemptId)
            _uiState.value = when (status) {
                VerificationStatus.SUCCESS ->
                    VerificationUiState.Success(attemptId)
                VerificationStatus.EXPIRED ->
                    VerificationUiState.Failure(attemptId, "Session expired. Please restart.")
                else ->
                    VerificationUiState.Failure(attemptId, "Verification could not be completed.")
            }
        }
    }

    /**
     * Polls the status of an existing attempt without requiring user confirmation.
     * Useful for background polling or automatic status refresh.
     * Transitions: any → CheckingStatus → Success | AwaitingBrowser | Failure
     */
    fun getVerificationStatus(attemptId: String) {
        _uiState.value = VerificationUiState.CheckingStatus(attemptId)
        viewModelScope.launch {
            val status = coordinator.getVerificationStatus(attemptId)
            _uiState.value = when (status) {
                VerificationStatus.SUCCESS ->
                    VerificationUiState.Success(attemptId)
                VerificationStatus.PENDING ->
                    // Still in-progress — return to AwaitingBrowser (URL already opened)
                    VerificationUiState.AwaitingBrowser(
                        attemptId       = attemptId,
                        verificationUrl = ""
                    )
                VerificationStatus.EXPIRED ->
                    VerificationUiState.Failure(attemptId, "Session expired. Please restart.")
                else ->
                    VerificationUiState.Failure(attemptId, "Verification failed.")
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Updates the OTP code typed while in [VerificationUiState.AwaitingBrowser]. */
    private fun updateCode(code: String) {
        val current = _uiState.value as? VerificationUiState.AwaitingBrowser ?: return
        _uiState.value = current.copy(enteredCode = code)
    }

    /** Handles OnConfirmClicked by resuming the current AwaitingBrowser attempt. */
    private fun confirmFromBrowser() {
        val current = _uiState.value as? VerificationUiState.AwaitingBrowser ?: return
        resumeVerification(current.attemptId)
    }
}
