package com.example.modoguardian.model

// Posibles resultados de un intento de inicio de sesion
sealed class ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin() // Credenciales correctas y correo verificado
    data object CredencialesInvalidas : ResultadoLogin() // Correo o clave incorrectos
    data class CorreoNoVerificado( // El correo existe pero no esta verificado
        val email: String,
        val reenviado: Boolean // true si se reenvio el enlace
    ) : ResultadoLogin()
    data object SinConexion : ResultadoLogin() // No hay WiFi ni datos moviles
    data class Error(val mensaje: String) : ResultadoLogin() // Cualquier otro problema
}