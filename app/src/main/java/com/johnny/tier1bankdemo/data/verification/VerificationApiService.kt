package com.johnny.tier1bankdemo.data.verification

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// ── API Models ─────────────────────────────────────────────────────────────

data class StartVerificationRequest(val useCase: String)

data class VerificationApiResponse(
    val attemptId: String,
    val status: String, // "pending", "success", "failed"
    val useCase: String,
    val verificationUrl: String?,
    val createdAt: Long,
    val completedAt: Long?
)

// ── Retrofit Interface ─────────────────────────────────────────────────────

interface VerificationApiService {

    @POST("/verification/start")
    suspend fun startVerification(@Body request: StartVerificationRequest): VerificationApiResponse

    @GET("/verification/{id}/status")
    suspend fun getVerificationStatus(@Path("id") id: String): VerificationApiResponse

    companion object {
        // Pointing to the local laptop's IP address so it works untethered over Wi-Fi
        private const val BASE_URL = "http://192.168.1.65:3000"

        fun create(): VerificationApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VerificationApiService::class.java)
        }
    }
}
