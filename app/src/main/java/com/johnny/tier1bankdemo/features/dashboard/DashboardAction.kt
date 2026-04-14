package com.johnny.tier1bankdemo.features.dashboard

sealed class DashboardAction {
    object OnTransferClicked : DashboardAction()
    object OnLogoutClicked : DashboardAction()
}
