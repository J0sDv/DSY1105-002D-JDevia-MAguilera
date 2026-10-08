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

    companion object {
        private const val CLAVE_CORREO = "correo_recordado"
    }
}