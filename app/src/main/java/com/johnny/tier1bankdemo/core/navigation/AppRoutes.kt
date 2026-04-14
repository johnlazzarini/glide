package com.johnny.tier1bankdemo.core.navigation

/**
 * All navigation destinations in the app, as simple string constants.
 *
 * Verification takes a useCase argument embedded in the path:
 *   route:    "verification/{useCase}"
 *   navigate: AppRoutes.verification("kyc")  →  "verification/kyc"
 */
object AppRoutes {
    const val LOGIN          = "login"
    const val DASHBOARD      = "dashboard"
    const val TRANSFER       = "transfer"
    const val RECOVERY       = "recovery"
    const val PASSWORD_RESET = "password_reset"
    const val VERIFICATION   = "verification/{useCase}"

    /** Builds the concrete navigation path for the verification screen. */
    fun verification(useCase: String) = "verification/$useCase"
}
