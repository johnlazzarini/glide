package com.johnny.tier1bankdemo.features.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.johnny.tier1bankdemo.features.verification.VerificationLauncher

@Composable
fun TransferScreen(
    navController: NavHostController,
    viewModel: TransferViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ── Navigation: send high-value transfers to Verification ─────────────────
    LaunchedEffect(uiState.needsVerification) {
        if (uiState.needsVerification) {
            // Clear the flag before navigating so it doesn't re-fire on recompose
            viewModel.onAction(TransferAction.OnVerificationNavigated)
            VerificationLauncher.launch(navController, useCase = "transfer_auth")
        }
    }

    // ── Read result from VerificationScreen via savedStateHandle ──────────────
    // When VerificationScreen pops itself, it writes "success" or "failed" here.
    // collectAsStateWithLifecycle() makes the LaunchedEffect re-fire automatically.
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val verificationResult by (
        savedStateHandle
            ?.getStateFlow("transfer_verified", "pending")
            ?.collectAsStateWithLifecycle()
            ?: remember { mutableStateOf("pending") }
    )

    LaunchedEffect(verificationResult) {
        when (verificationResult) {
            "success" -> {
                savedStateHandle?.set("transfer_verified", "pending") // consume the result
                viewModel.onAction(TransferAction.OnVerificationSucceeded)
            }
            "failed" -> {
                savedStateHandle?.set("transfer_verified", "pending") // consume the result
                viewModel.onAction(TransferAction.OnVerificationFailed)
            }
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Transfer Funds",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Transfers over \$1,000 require identity verification.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        if (uiState.isSuccess) {
            // ── Success state ─────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Transfer Submitted!",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\$${uiState.amount} to ${uiState.recipient} is being processed.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Dashboard")
            }

        } else {
            // ── Transfer Form ─────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.recipient,
                onValueChange = { viewModel.onAction(TransferAction.OnRecipientChanged(it)) },
                label = { Text("Recipient Name or Account") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.onAction(TransferAction.OnAmountChanged(it)) },
                label = { Text("Amount (USD)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { viewModel.onAction(TransferAction.OnSubmitClicked) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Transfer")
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = {
                    viewModel.onAction(TransferAction.OnBackClicked)
                    navController.popBackStack()
                }
            ) {
                Text("Cancel")
            }
        }
    }
}
