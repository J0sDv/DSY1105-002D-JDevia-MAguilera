package com.example.modoguardian.model

// Usuario autenticado. Los datos vienen de Firebase (Authentication y Firestore)
data class Usuario(
    val nombre: String, // Nombre de usuario (username)
    val email: String,
    val rol: Rol,
    val region: String = "" // País o región elegido en el registro
)