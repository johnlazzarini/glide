package com.johnny.tier1bankdemo.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.johnny.tier1bankdemo.features.dashboard.DashboardScreen
import com.johnny.tier1bankdemo.features.login.LoginScreen
import com.johnny.tier1bankdemo.features.recovery.PasswordResetScreen
import com.johnny.tier1bankdemo.features.recovery.RecoveryScreen
import com.johnny.tier1bankdemo.features.transfer.TransferScreen
import com.johnny.tier1bankdemo.features.verification.VerificationScreen

/**
 * Central navigation graph for the app.
 *
 * Flow:
 *   Login ──► Verification(login_2fa) ──► Dashboard ──► Transfer
 *     └──► Recovery ──► Verification(account_recovery) ──► PasswordReset ──► Login
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(AppRoutes.DASHBOARD) {
            DashboardScreen(navController = navController)
        }

        composable(AppRoutes.TRANSFER) {
            TransferScreen(navController = navController)
        }

        composable(AppRoutes.RECOVERY) {
            RecoveryScreen(navController = navController)
        }

        composable(AppRoutes.PASSWORD_RESET) {
            PasswordResetScreen(navController = navController)
        }

        // Verification receives a useCase so it knows which flow to start
        composable(
            route = AppRoutes.VERIFICATION,
            arguments = listOf(
                navArgument("useCase") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val useCase = backStackEntry.arguments?.getString("useCase").orEmpty()
            VerificationScreen(
                navController = navController,
                useCase = useCase
            )
        }
    }
}
