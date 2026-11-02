package com.example.modoguardian.model

// Posibles resultados de un intento de registro
sealed class ResultadoRegistro {
    data class Exito(val correoEnviado: Boolean) : ResultadoRegistro() // Cuenta creada (y enlace enviado o no)
    data object CorreoExistente : ResultadoRegistro() // Ya hay una cuenta con ese correo
    data object UsernameExistente : ResultadoRegistro() // Ya hay una cuenta con ese username
    data object SinConexion : ResultadoRegistro() // No hay WiFi ni datos moviles
    data class Error(val mensaje: String) : ResultadoRegistro() // Cualquier otro problema
}