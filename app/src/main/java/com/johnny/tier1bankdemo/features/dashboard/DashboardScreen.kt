package com.johnny.tier1bankdemo.features.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.johnny.tier1bankdemo.R
import com.johnny.tier1bankdemo.core.navigation.AppRoutes

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate to Login and clear the entire back stack when the user logs out
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navController.navigate(AppRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top Bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.chase_logo),
                contentDescription = "Chase Logo",
                modifier = Modifier.height(24.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            TextButton(onClick = { viewModel.onAction(DashboardAction.OnLogoutClicked) }) {
                Text("Sign Out", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Account Summary Card ─────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.accountName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = uiState.accountNumber,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Available Balance",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = uiState.balance,
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            // ── Transfer Button ──────────────────────────────────────────────
            item {
                Button(
                    onClick = {
                        viewModel.onAction(DashboardAction.OnTransferClicked)
                        navController.navigate(AppRoutes.TRANSFER)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Transfer Funds")
                }
            }

            // ── Recent Transactions Header ───────────────────────────────────
            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // ── Transaction Rows ─────────────────────────────────────────────
            items(uiState.recentTransactions) { tx ->
                TransactionRow(tx)
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: TransactionItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = tx.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = tx.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = tx.amount,
            style = MaterialTheme.typography.bodyMedium,
            color = if (tx.isCredit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
}
