package com.example.modoguardian.model

// Modelo principal que representa el estado de la pantalla de registro
data class RegistroUiState(
    val email: String = "", // Correo ingresado
    val password: String = "", // Contraseña ingresada
    val confirmarPassword: String = "", // Repeticion de la contraseña
    val username: String = "", // Nombre de usuario
    val region: String = "Chile", // País o region seleccionado
    val isLoading: Boolean = false, // true mientras se crea la cuenta
    val mensajeCuentaCreada: String? = null, // Si no es null, se muestra el aviso "Revisa tu correo"
    val errores: RegistroErrores = RegistroErrores()
)