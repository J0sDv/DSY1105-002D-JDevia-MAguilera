package com.example.modoguardian.model

data class Usuario(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: Rol
)