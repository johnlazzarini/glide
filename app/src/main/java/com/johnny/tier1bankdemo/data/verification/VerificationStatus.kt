package com.johnny.tier1bankdemo.data.verification

/** Represents the outcome of a verification attempt. */
enum class VerificationStatus {
    PENDING,   // Submitted but not yet resolved
    SUCCESS,   // Code matched; user is verified
    FAILED,    // Code was wrong
    EXPIRED    // Code window has passed
}
