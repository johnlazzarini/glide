package com.johnny.tier1bankdemo.data.verification

/**
 * A record of a single verification attempt.
 *
 * [useCase]          — what kind of verification this is (e.g. "kyc", "login_2fa")
 * [verificationUrl]  — browser URL for external verification steps (null for OTP-only flows)
 * [userId]           — optional; populated on legacy OTP-based verify calls
 */
data class VerificationAttempt(
    val id: String,
    val userId: String = "",
    val useCase: String = "",
    val verificationUrl: String? = null,
    val timestamp: Long,
    val status: VerificationStatus
)
