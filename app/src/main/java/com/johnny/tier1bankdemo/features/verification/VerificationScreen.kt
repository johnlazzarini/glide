package com.johnny.tier1bankdemo.features.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.johnny.tier1bankdemo.core.navigation.AppRoutes

/**
 * Verification screen — renders the current [VerificationUiState].
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

    // Navigation handling
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

    // Deep Link Interceptor
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
                fontWeight = FontWeight.Bold,
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
                is VerificationUiState.Idle -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text("Identity Verification", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
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

                is VerificationUiState.Starting -> {
                    LoadingContent(message = "Starting ${state.useCase} verification…")
                }

                is VerificationUiState.AwaitingBrowser -> {
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    LaunchedEffect(state.attemptId) {
                        if (state.verificationUrl.isNotBlank()) {
                            try {
                                val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
                                val telephonyManager = ctx.getSystemService(android.content.Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
                                val plmn = telephonyManager.networkOperator
                                val uriBuilder = android.net.Uri.parse(state.verificationUrl).buildUpon()
                                if (!plmn.isNullOrEmpty()) {
                                    uriBuilder.appendQueryParameter("plmn", plmn)
                                }
                                customTabsIntent.launchUrl(ctx, uriBuilder.build())
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Browser Verification", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "We've opened a secure browser window to verify your identity.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.onAction(VerificationAction.GetStatus(state.attemptId)) },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("I've Completed Verification")
                        }
                    }
                }

                is VerificationUiState.CheckingStatus -> {
                    LoadingContent(message = "Checking verification status…")
                }

                is VerificationUiState.Success -> {
                    LoadingContent(message = "Verified! Redirecting…")
                }

                is VerificationUiState.Failure -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Verification Failed", style = MaterialTheme.typography.headlineMedium)
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
                    }
                }
            }
        }
    }
}

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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
