package com.johnny.tier1bankdemo.features.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun onAction(action: DashboardAction) {
        when (action) {
            // Navigation to Transfer is handled by the screen
            is DashboardAction.OnTransferClicked -> Unit

            is DashboardAction.OnLogoutClicked ->
                _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
