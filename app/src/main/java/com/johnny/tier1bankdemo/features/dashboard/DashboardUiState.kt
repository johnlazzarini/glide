package com.johnny.tier1bankdemo.features.dashboard

/**
 * A single item in the recent transaction list shown on the Dashboard.
 */
data class TransactionItem(
    val description: String,
    val amount: String,
    val isCredit: Boolean,   // True = money in (green), False = money out (default)
    val date: String
)

data class DashboardUiState(
    val accountName: String = "John Demo",
    val accountNumber: String = "****  4242",
    val balance: String = "$12,450.00",
    val isLoggedOut: Boolean = false,
    val recentTransactions: List<TransactionItem> = listOf(
        TransactionItem("Payroll Deposit",   "+\$3,200.00", isCredit = true,  date = "Apr 10"),
        TransactionItem("Coffee Shop",       "-\$4.50",     isCredit = false, date = "Apr 11"),
        TransactionItem("Electric Bill",     "-\$120.00",   isCredit = false, date = "Apr 8"),
        TransactionItem("Online Transfer",   "-\$250.00",   isCredit = false, date = "Apr 5"),
        TransactionItem("ATM Withdrawal",    "-\$60.00",    isCredit = false, date = "Apr 3")
    )
)
