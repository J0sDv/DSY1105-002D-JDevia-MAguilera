package com.example.modoguardian.navigation

// Sealed class para definir rutas tipo-safe en la navegacion
sealed class Screen(val route: String) {

    // Representa la ruta a la pantalla de inicio de sesion (destino inicial)
    data object Login : Screen(route = "login_page")

    // Representa la ruta a la pantalla de registro de usuario
    data object Registro : Screen(route = "registro_page")

    // Representa la ruta a la pantalla Home, que cambia según el rol
    data object Home : Screen(route = "home_page")
}