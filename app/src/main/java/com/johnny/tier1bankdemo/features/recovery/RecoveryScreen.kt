package com.johnny.tier1bankdemo.features.recovery

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.johnny.tier1bankdemo.features.verification.VerificationLauncher

@Composable
fun RecoveryScreen(
    navController: NavHostController,
    viewModel: RecoveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Email confirmed — hand off to verification before allowing a password reset
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            VerificationLauncher.launch(navController, useCase = "account_recovery")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Account Recovery",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Enter your email and we'll verify your identity before resetting your password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onAction(RecoveryAction.OnEmailChanged(it)) },
            label = { Text("Email Address") },
            singleLine = true,
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
            onClick = { viewModel.onAction(RecoveryAction.OnSubmitClicked) },
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
                Text("Continue")
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = {
                viewModel.onAction(RecoveryAction.OnBackClicked)
                navController.popBackStack()
            }
        ) {
            Text("Back to Login")
        }
    }
}
