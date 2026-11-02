package com.example.modoguardian.model

// Modelo que almacena posibles errores individuales del formulario de registro
data class RegistroErrores(
    val email: String? = null,
    val password: String? = null,
    val confirmarPassword: String? = null,
    val username: String? = null,
    val region: String? = null,
    val general: String? = null // Error de conectividad, google o inesperado
)