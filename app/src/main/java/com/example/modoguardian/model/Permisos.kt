package com.example.modoguardian.model

// Permisos por rol
val Rol.puedeAtenderAlertas: Boolean
    get() = this == Rol.OPERADOR || this == Rol.SUPERVISOR

val Rol.veSeguimiento: Boolean
    get() = this == Rol.SUPERVISOR || this == Rol.ADMIN