package com.johnny.tier1bankdemo.features.transfer

sealed class TransferAction {
    data class OnAmountChanged(val value: String) : TransferAction()
    data class OnRecipientChanged(val value: String) : TransferAction()
    object OnSubmitClicked : TransferAction()
    object OnBackClicked : TransferAction()

    // Called after navigating to Verification, so the flag is cleared before the screen recomposes
    object OnVerificationNavigated : TransferAction()
    // Called when VerificationScreen returns with a result (via savedStateHandle)
    object OnVerificationSucceeded : TransferAction()
    object OnVerificationFailed : TransferAction()
}
