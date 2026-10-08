package com.example.modoguardian.model

sealed class ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin()
    data object CredencialesInvalidas : ResultadoLogin()
    data object SinConexion : ResultadoLogin()
}