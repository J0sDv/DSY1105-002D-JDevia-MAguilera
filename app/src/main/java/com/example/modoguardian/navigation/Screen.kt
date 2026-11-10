package com.example.modoguardian.navigation

sealed class Screen(val route: String) {

    data object Login : Screen(route = "login_page")
    data object Registro : Screen(route = "registro_page")
    data object Home : Screen(route = "home_page")
    data object Usuarios : Screen(route = "usuarios_page")
    data class CamaraDetalle(val camaraId: String) : Screen(route = "camara_detalle/$camaraId")
    data class AlertaDetalle(val eventoId: String) : Screen(route = "alerta_detalle/$eventoId")

    companion object {
        const val CAMARA_DETALLE = "camara_detalle/{camaraId}"
        const val ALERTA_DETALLE = "alerta_detalle/{eventoId}"
    }
}