package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object GlobalChat : Screen("global_chat")
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
    data object ChangePassword : Screen("change_password")
    data object AccountManagement : Screen("account_management")
    data object CreateAccount : Screen("create_account")
}
