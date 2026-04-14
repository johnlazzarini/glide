package com.johnny.tier1bankdemo.data.verification

import kotlinx.coroutines.delay

/**
 * Single source of truth for all verification data.
 *
 * Implemented as a Kotlin object (singleton) — no DI framework needed.
 */
object VerificationRepository {

    // Lazy initialization of the API service so it's only created when first used.
    private val api: VerificationApiService by lazy { VerificationApiService.create() }
    
    // In-memory local cache so we don't always hit the network for same session read
    private val attemptStore = mutableMapOf<String, VerificationAttempt>()

    // ── Helper ───────────────────────────────────────────────────────────────

    private fun parseStatus(apiStr: String): VerificationStatus {
        return when (apiStr.lowercase()) {
            "success" -> VerificationStatus.SUCCESS
            "failed" -> VerificationStatus.FAILED
            else -> VerificationStatus.PENDING
        }
    }

    private fun mapResponseToAttempt(response: VerificationApiResponse): VerificationAttempt {
        val attempt = VerificationAttempt(
            id = response.attemptId,
            useCase = response.useCase,
            verificationUrl = response.verificationUrl ?: "",
            timestamp = response.createdAt,
            status = parseStatus(response.status)
        )
        attemptStore[attempt.id] = attempt
        return attempt
    }

    // ── Legacy OTP path ───────────────────────────────────────────────────────

    suspend fun verify(userId: String, code: String): VerificationAttempt {
        delay(700)
        val status = if (code == "123456") VerificationStatus.SUCCESS else VerificationStatus.FAILED
        return VerificationAttempt(
            id        = "otp_${System.currentTimeMillis()}",
            userId    = userId,
            timestamp = System.currentTimeMillis(),
            status    = status
        )
    }

    // ── Browser-based verification path ──────────────────────────────────────

    /**
     * Creates a new verification attempt via HTTP POST.
     */
    suspend fun startVerification(useCase: String): VerificationAttempt {
        return try {
            val response = api.startVerification(StartVerificationRequest(useCase = useCase))
            mapResponseToAttempt(response)
        } catch (e: Exception) {
            e.printStackTrace()
            // Return a failed attempt if the network throws
            VerificationAttempt(
                id = "error_${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis(),
                status = VerificationStatus.FAILED
            )
        }
    }

    /**
     * Called when the user returns from the browser.
     * Hits the status endpoint to verify the backend session state.
     */
    suspend fun resumeVerification(attemptId: String): VerificationAttempt {
        return getVerificationStatus(attemptId)
    }

    /**
     * Polls the current status from the backend.
     */
    suspend fun getVerificationStatus(attemptId: String): VerificationAttempt {
        return try {
            val response = api.getVerificationStatus(attemptId)
            mapResponseToAttempt(response)
        } catch (e: Exception) {
            e.printStackTrace()
            attemptStore[attemptId] ?: VerificationAttempt(
                id = attemptId,
                timestamp = System.currentTimeMillis(),
                status = VerificationStatus.FAILED
            )
        }
    }
}
