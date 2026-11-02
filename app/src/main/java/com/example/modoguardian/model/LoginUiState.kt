package com.example.modoguardian.model

// Modelo principal que representa el estado de la pantalla de login
data class LoginUiState(
    val email: String = "", // Correo ingresado
    val password: String = "", // Contraseña ingresada
    val isLoading: Boolean = false, // true mientras se valida el acceso
    val recordarCorreo: Boolean = false, // Checkbox: persistencia local limitada
    val rol: Rol? = null, // Rol del usuario autenticado (null = sin sesión)
    val nombreUsuario: String = "", // Nombre del usuario autenticado
    val errores: LoginErrores = LoginErrores() // Errores por campo
) {
    // Se mantiene por compatibilidad con la Guía 11 (el botón de login ya no se bloquea)
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}