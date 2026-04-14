package com.johnny.tier1bankdemo.features.recovery

sealed class RecoveryAction {
    data class OnEmailChanged(val value: String) : RecoveryAction()
    object OnSubmitClicked : RecoveryAction()
    object OnBackClicked : RecoveryAction()
}
