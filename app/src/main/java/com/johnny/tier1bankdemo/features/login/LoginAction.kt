package com.johnny.tier1bankdemo.features.login

/**
 * All user intents on the Login screen.
 * The screen dispatches these; the ViewModel handles them.
 */
sealed class LoginAction {
    data class OnUsernameChanged(val value: String) : LoginAction()
    data class OnPasswordChanged(val value: String) : LoginAction()
    object OnLoginClicked : LoginAction()
    object OnForgotPasswordClicked : LoginAction()
}
