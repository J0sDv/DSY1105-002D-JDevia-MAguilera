package com.example.modoguardian.navigation

sealed class Screen(val route: String) {

    data object Login : Screen(route = "login_page")
    data object Home : Screen(route = "home_page")
}