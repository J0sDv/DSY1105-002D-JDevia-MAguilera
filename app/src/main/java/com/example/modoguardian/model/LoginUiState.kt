package com.example.modoguardian.model

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