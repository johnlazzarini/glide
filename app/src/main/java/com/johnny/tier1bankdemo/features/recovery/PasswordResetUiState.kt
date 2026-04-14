package com.johnny.tier1bankdemo.features.recovery

data class PasswordResetUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
