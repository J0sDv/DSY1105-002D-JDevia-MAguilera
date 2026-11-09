package com.example.modoguardian.navigation

// Sealed class para definir rutas tipo-safe en la navegación
sealed class Screen(val route: String) {

    // Representa la ruta a la pantalla de inicio de sesión (destino inicial)
    data object Login : Screen(route = "login_page")

    // Representa la ruta a la pantalla de registro de usuario
    data object Registro : Screen(route = "registro_page")

    // Representa la ruta a la pantalla principal con las 5 pestañas
    data object Home : Screen(route = "home_page")

    // Gestión de usuarios (solo Administrador)
    data object Usuarios : Screen(route = "usuarios_page")

    // Rutas con argumento. La ruta real lleva el id; el patrón se usa en el NavHost
    data class CamaraDetalle(val camaraId: String) : Screen(route = "camara_detalle/$camaraId")
    data class AlertaDetalle(val eventoId: String) : Screen(route = "alerta_detalle/$eventoId")

    companion object {
        // Patrones con {argumento} para declarar los composable del NavHost
        const val CAMARA_DETALLE = "camara_detalle/{camaraId}"
        const val ALERTA_DETALLE = "alerta_detalle/{eventoId}"
    }
}