package com.johnny.tier1bankdemo.features.verification

import com.johnny.tier1bankdemo.data.verification.VerificationAttempt
import com.johnny.tier1bankdemo.data.verification.VerificationStatus

/**
 * Coordinates what happens after each verification operation resolves.
 *
 * The ViewModel calls coordinator methods; the coordinator decides what the
 * outcome means at the application level and returns a simplified result.
 *
 * This is the right place to add cross-cutting concerns such as:
 *   - Analytics events ("verification_started", "verification_succeeded")
 *   - Session token refresh after success
 *   - Retry policy decisions
 *
 * [startVerification] returns the full [VerificationAttempt] so the ViewModel
 * can extract the [VerificationAttempt.verificationUrl] for the UI.
 * All other methods return just [VerificationStatus] — the caller decides what to do.
 */
class VerificationCoordinator(
    private val useCase: VerificationUseCase = VerificationUseCase()
) {

    // ── Legacy OTP flow ───────────────────────────────────────────────────────

    suspend fun verify(userId: String, code: String): VerificationStatus =
        useCase.execute(userId, code).status

    // ── Browser-based flow ────────────────────────────────────────────────────

    /**
     * Starts a new attempt. Returns the full attempt so the ViewModel can surface
     * [VerificationAttempt.verificationUrl] to the UI.
     */
    suspend fun startVerification(useCaseName: String): VerificationAttempt =
        useCase.startVerification(useCaseName)

    /** Resolves a pending attempt after the user returns from the browser. */
    suspend fun resumeVerification(attemptId: String): VerificationStatus =
        useCase.resumeVerification(attemptId).status

    /** Polls the current status of an attempt without user interaction. */
    suspend fun getVerificationStatus(attemptId: String): VerificationStatus =
        useCase.getVerificationStatus(attemptId).status
}
