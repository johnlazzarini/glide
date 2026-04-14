package com.johnny.tier1bankdemo.features.recovery

sealed class PasswordResetAction {
    data class OnNewPasswordChanged(val value: String) : PasswordResetAction()
    data class OnConfirmPasswordChanged(val value: String) : PasswordResetAction()
    object OnSubmitClicked : PasswordResetAction()
}
