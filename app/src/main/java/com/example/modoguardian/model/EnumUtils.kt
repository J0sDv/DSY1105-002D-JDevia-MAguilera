package com.example.modoguardian.model

// Convierte un texto en enum sin lanzar errores: si no coincide, devuelve el valor por defecto.
inline fun <reified T : Enum<T>> enumSeguro(valor: String?, defecto: T): T {
    return try {
        if (valor == null) defecto else enumValueOf<T>(valor)
    } catch (e: IllegalArgumentException) {
        defecto
    }
}