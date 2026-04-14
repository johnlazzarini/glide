package com.johnny.tier1bankdemo.features.verification

import androidx.navigation.NavController
import com.johnny.tier1bankdemo.core.navigation.AppRoutes

/**
 * A simple helper that any screen can call to start the verification flow.
 *
 * Centralising this means the route construction and argument format
 * are defined in one place — not scattered across multiple call sites.
 *
 * Usage:
 *   VerificationLauncher.launch(navController, useCase = "kyc")
 */
object VerificationLauncher {

    fun launch(navController: NavController, useCase: String) {
        navController.navigate(AppRoutes.verification(useCase))
    }
}
