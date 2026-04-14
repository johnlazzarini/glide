package com.johnny.tier1bankdemo.features.recovery

data class RecoveryUiState(
    val email: String = "demo@tier1bank.com",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,       // True once the recovery email is "sent"
    val errorMessage: String? = null
)
