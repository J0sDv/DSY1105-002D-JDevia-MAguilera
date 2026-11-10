package com.example.modoguardian.model

// Modelo principal que representa el estado de la pantalla de login
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val recordarCorreo: Boolean = false,
    val rol: Rol? = null,
    val nombreUsuario: String = "",
    val errores: LoginErrores = LoginErrores()
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}