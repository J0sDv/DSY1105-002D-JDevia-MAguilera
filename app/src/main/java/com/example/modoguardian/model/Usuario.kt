package com.example.modoguardian.model

// Usuario autenticado.
data class Usuario(
    val nombre: String,
    val email: String,
    val rol: Rol,
    val region: String = ""
)