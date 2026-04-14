package com.johnny.tier1bankdemo.features.login

/**
 * All UI state for the Login screen.
 * The ViewModel exposes this as a StateFlow; the screen renders it.
 */
data class LoginUiState(
    val username: String = "demo",
    val password: String = "password",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Set to true once credentials are validated; the screen uses this to navigate
    val isLoginSuccess: Boolean = false
)
