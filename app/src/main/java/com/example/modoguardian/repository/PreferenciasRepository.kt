package com.example.modoguardian.repository

import android.content.Context

class PreferenciasRepository(context: Context) {

    private val prefs = context.getSharedPreferences("modo_guardian_prefs", Context.MODE_PRIVATE)

    fun guardarCorreo(correo: String) {
        prefs.edit().putString(CLAVE_CORREO, correo).apply()
    }

    fun obtenerCorreo(): String = prefs.getString(CLAVE_CORREO, "") ?: ""

    fun borrarCorreo() {
        prefs.edit().remove(CLAVE_CORREO).apply()
    }

    fun guardarModoOscuro(valor: Boolean) {
        prefs.edit().putBoolean(CLAVE_OSCURO, valor).apply()
    }

    fun obtenerModoOscuro(): Boolean? =
        if (prefs.contains(CLAVE_OSCURO)) prefs.getBoolean(CLAVE_OSCURO, false) else null

    fun guardarNotificaciones(valor: Boolean) {
        prefs.edit().putBoolean(CLAVE_NOTIF, valor).apply()
    }

    fun obtenerNotificaciones(): Boolean = prefs.getBoolean(CLAVE_NOTIF, true)

    companion object {
        private const val CLAVE_CORREO = "correo_recordado"
        private const val CLAVE_OSCURO = "modo_oscuro"
        private const val CLAVE_NOTIF = "notificaciones"
    }
}