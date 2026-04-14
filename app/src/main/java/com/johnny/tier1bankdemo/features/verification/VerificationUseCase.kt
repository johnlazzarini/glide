package com.johnny.tier1bankdemo.features.verification

import com.johnny.tier1bankdemo.data.verification.VerificationAttempt
import com.johnny.tier1bankdemo.data.verification.VerificationRepository

/**
 * Encapsulates all verification business rules.
 *
 * Each method maps to exactly one repository operation — keeping the rule
 * ("what counts as verified?") separate from the data access ("how do we check?").
 * This also makes unit testing easy: inject a fake repository, test the use case alone.
 */
class VerificationUseCase {

    // ── Legacy OTP path ───────────────────────────────────────────────────────

    suspend fun execute(userId: String, code: String): VerificationAttempt =
        VerificationRepository.verify(userId, code)

    // ── Browser-based path ────────────────────────────────────────────────────

    suspend fun startVerification(useCase: String): VerificationAttempt =
        VerificationRepository.startVerification(useCase)

    suspend fun resumeVerification(attemptId: String): VerificationAttempt =
        VerificationRepository.resumeVerification(attemptId)

    suspend fun getVerificationStatus(attemptId: String): VerificationAttempt =
        VerificationRepository.getVerificationStatus(attemptId)
}
