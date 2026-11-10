package com.example.modoguardian.model

// Modelo principal que representa el estado de la pantalla de registro
data class RegistroUiState(
    val email: String = "",
    val password: String = "",
    val confirmarPassword: String = "",
    val username: String = "",
    val region: String = "Chile",
    val isLoading: Boolean = false,
    val mensajeCuentaCreada: String? = null,
    val errores: RegistroErrores = RegistroErrores()
)