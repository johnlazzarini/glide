package com.johnny.tier1bankdemo.features.verification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.johnny.tier1bankdemo.core.navigation.AppRoutes

/**
 * Verification screen — renders the current [VerificationUiState].
 *
 * Entry points (any of these work):
 *   1. Navigation:  VerificationLauncher.launch(navController, useCase = "kyc")
 *   2. Deep link:   tier1bank://verify?useCase=kyc
 *   3. Direct call: viewModel.startVerification("login_2fa") from any other screen
 *
 * The [useCase] nav argument auto-starts the flow if the screen opens in Idle.
 * On [VerificationUiState.Success], the screen navigates to Dashboard and
 * removes itself from the back stack.
 */
@Composable
fun VerificationScreen(
    navController: NavHostController,
    useCase: String,
    viewModel: VerificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-start the flow when arriving with a useCase argument
    LaunchedEffect(useCase) {
        if (uiState is VerificationUiState.Idle && useCase.isNotBlank()) {
            viewModel.startVerification(useCase)
        }
    }

    // Navigate to the correct destination on success based on which flow triggered verification:
    //   login_2fa       → Dashboard (clear Login + Verification)
    //   account_recovery → PasswordReset (clear Recovery + Verification; Login stays for after reset)
    //   transfer_auth   → pop back to TransferScreen, writing "success" via savedStateHandle
    LaunchedEffect(uiState) {
        if (uiState is VerificationUiState.Success) {
            when (useCase) {
                "account_recovery" -> navController.navigate(AppRoutes.PASSWORD_RESET) {
                    popUpTo(AppRoutes.RECOVERY) { inclusive = true }
                }
                "transfer_auth" -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("transfer_verified", "success")
                    navController.popBackStack()
                }
                else -> navController.navigate(AppRoutes.DASHBOARD) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                }
            }
        }
    }

    // ── Deep Link Intent Interceptor ─────────────────────────────────────────
    // Listens for the `tier1bank://verify?attemptId=...` deep link returning 
    // from Chrome Custom Tabs, and kicks off a status check automatically.
    val context = androidx.compose.ui.platform.LocalContext.current
    DisposableEffect(context) {
        val activity = context as? androidx.activity.ComponentActivity
        val listener = androidx.core.util.Consumer<android.content.Intent> { intent ->
            if (intent.action == android.content.Intent.ACTION_VIEW && intent.data?.host == "verify") {
                val returnedId = intent.data?.getQueryParameter("attemptId")
                if (returnedId != null) {
                    viewModel.onAction(VerificationAction.GetStatus(returnedId))
                }
            }
        }
        activity?.addOnNewIntentListener(listener)
        // Also check the current intent in case it resumed us
        listener.accept(activity?.intent ?: android.content.Intent())
        
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Top Bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = "Secure Verification",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
        when (val state = uiState) {

            // ── Idle ──────────────────────────────────────────────────────────
            is VerificationUiState.Idle -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text("Identity Verification", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "Tap below to begin the verification process.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            viewModel.onAction(VerificationAction.StartVerification(useCase))
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Verify through Carrier")
                    }
                }
            }

            // ── Starting ──────────────────────────────────────────────────────
            is VerificationUiState.Starting -> {
                LoadingContent(message = "Starting ${state.useCase} verification…")
            }

            // ── AwaitingBrowser ───────────────────────────────────────────────
            is VerificationUiState.AwaitingBrowser -> {
                val context = androidx.compose.ui.platform.LocalContext.current
                
                // Launch Chrome Custom Tab automatically when reaching this state
                LaunchedEffect(state.attemptId) {
                    if (state.verificationUrl.isNotBlank()) {
                        try {
                            val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
                            
                            val telephonyManager = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
                            val plmn = telephonyManager.networkOperator
                            
                            val uriBuilder = android.net.Uri.parse(state.verificationUrl).buildUpon()
                            if (!plmn.isNullOrEmpty()) {
                                uriBuilder.appendQueryParameter("plmn", plmn)
                            }
                            val finalUrl = uriBuilder.build()
                            
                            customTabsIntent.launchUrl(context, finalUrl)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Browser Verification",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "We've opened a secure browser window to verify your identity. Please complete the steps there.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))

                    // Fallback CTA just in case CCT fails or user closes it prematurely
                    Button(
                        onClick = { viewModel.onAction(VerificationAction.GetStatus(state.attemptId)) },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("I've Completed Verification")
                    }

                    // Secondary actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.onAction(VerificationAction.GetStatus(state.attemptId))
                            }
                        ) {
                            Text("Check Status")
                        }
                        TextButton(
                            onClick = { viewModel.onAction(VerificationAction.OnResendClicked) }
                        ) {
                            Text("Restart")
                        }
                    }
                }
            }

            // ── CheckingStatus ────────────────────────────────────────────────
            is VerificationUiState.CheckingStatus -> {
                LoadingContent(message = "Checking verification status…")
            }

            // ── Success ───────────────────────────────────────────────────────
            is VerificationUiState.Success -> {
                // LaunchedEffect above handles navigation;
                // show a brief success indicator while the transition occurs
                LoadingContent(message = "Verified! Redirecting…")
            }

            // ── Failure ───────────────────────────────────────────────────────
            is VerificationUiState.Failure -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Verification Failed",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = state.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.onAction(VerificationAction.Reset) },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Try Again")
                    }
                    TextButton(onClick = {
                        // For transfer_auth: signal failure back to TransferScreen before popping
                        if (useCase == "transfer_auth") {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("transfer_verified", "failed")
                        }
                        navController.popBackStack()
                    }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

/** Shared loading indicator used by Starting, CheckingStatus, and Success states. */
@Composable
private fun LoadingContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
