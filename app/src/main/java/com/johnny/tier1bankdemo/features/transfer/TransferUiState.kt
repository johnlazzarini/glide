package com.johnny.tier1bankdemo.features.transfer

data class TransferUiState(
    val amount: String = "1001",
    val recipient: String = "Jane Smith",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val needsVerification: Boolean = false,  // True when amount > $1000; triggers nav to Verification
    val errorMessage: String? = null
)
