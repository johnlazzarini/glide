package com.johnny.tier1bankdemo.features.verification

sealed class VerificationAction {

    // ── Browser-based flow ─────────────────────────────────────────────────────

    /** Initiates a new attempt for [useCase] (e.g. "kyc", "login_2fa", "transfer_auth"). */
    data class StartVerification(val useCase: String) : VerificationAction()

    /** Called when the user returns from the browser and wants to resolve the attempt. */
    data class ResumeVerification(val attemptId: String) : VerificationAction()

    /** Polls the current status of an existing attempt (no user interaction required). */
    data class GetStatus(val attemptId: String) : VerificationAction()

    /** Resets the flow back to Idle, e.g. to retry after a Failure. */
    object Reset : VerificationAction()

    // ── Code-entry actions (active while in AwaitingBrowser) ──────────────────

    /** Updates the optional OTP code the user types during [VerificationUiState.AwaitingBrowser]. */
    data class OnCodeChanged(val value: String) : VerificationAction()

    /** Confirms the current AwaitingBrowser state — triggers [ResumeVerification] internally. */
    object OnConfirmClicked : VerificationAction()

    /** Requests a new code or restarts the browser session, resetting to Idle. */
    object OnResendClicked : VerificationAction()
}
