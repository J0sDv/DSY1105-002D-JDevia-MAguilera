package com.example.modoguardian.model

// Registro de trazabilidad: quien hizo que sobre un evento y cuando
data class AccionRegistro(
    val id: String,
    val eventoId: String,
    val accion: String,
    val usuario: String,
    val rol: String,
    val fechaMillis: Long
)

// Usuario tal como lo ve el Administrador en la gestion de roles
data class PerfilUsuario(
    val uid: String,
    val username: String,
    val email: String,
    val rol: Rol
)