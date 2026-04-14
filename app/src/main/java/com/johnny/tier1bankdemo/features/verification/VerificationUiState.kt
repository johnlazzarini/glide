package com.johnny.tier1bankdemo.features.verification

/**
 * All possible states for the reusable verification flow.
 *
 * State machine:
 *
 *   Idle
 *    └─ startVerification(useCase) ──► Starting
 *                                          └─ attempt created ──► AwaitingBrowser
 *                                                                       │
 *                                              OnConfirmClicked ────────┤
 *                                              ResumeVerification ──────┤
 *                                              GetStatus ───────────────┘
 *                                                                ▼
 *                                                         CheckingStatus
 *                                                          ├──► Success
 *                                                          └──► Failure
 *
 * Reset transitions any state back to Idle for a retry.
 */
sealed class VerificationUiState {

    /** No verification in progress. The initial state. */
    object Idle : VerificationUiState()

    /**
     * [startVerification] was called; waiting for the attempt to be created by the repository.
     * [useCase] is displayed so the user knows what they are verifying.
     */
    data class Starting(val useCase: String) : VerificationUiState()

    /**
     * The attempt was created. The user must open [verificationUrl] in a browser
     * to complete the external verification step, then return to confirm.
     *
     * [enteredCode] holds an optional OTP typed while the browser session is open —
     * some verification services send a code alongside the browser redirect.
     */
    data class AwaitingBrowser(
        val attemptId: String,
        val verificationUrl: String,
        val enteredCode: String = ""
    ) : VerificationUiState()

    /**
     * [resumeVerification] or [getVerificationStatus] was called;
     * waiting for the server (or stub) to respond.
     */
    data class CheckingStatus(val attemptId: String) : VerificationUiState()

    /** Verification completed successfully. */
    data class Success(val attemptId: String) : VerificationUiState()

    /** Verification failed or could not be completed. */
    data class Failure(
        val attemptId: String? = null,
        val reason: String
    ) : VerificationUiState()
}
