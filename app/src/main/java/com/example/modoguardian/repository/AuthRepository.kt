package com.example.modoguardian.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.modoguardian.model.ResultadoLogin
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.Usuario
import kotlinx.coroutines.delay

class AuthRepository(private val context: Context) {

    private val usuariosPrueba = listOf(
        Usuario("Admin Guardián", "admin@guardian.test", "123456", Rol.ADMIN),
        Usuario("Supervisor Guardián", "supervisor@guardian.test", "123456", Rol.SUPERVISOR),
        Usuario("Operador Guardián", "operador@guardian.test", "123456", Rol.OPERADOR)
    )

    suspend fun iniciarSesion(email: String, password: String): ResultadoLogin {
        delay(1500)

        if (VERIFICAR_CONECTIVIDAD && !hayConexion()) {
            return ResultadoLogin.SinConexion
        }

        val usuario = usuariosPrueba.firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        }

        return if (usuario != null) {
            ResultadoLogin.Exito(usuario)
        } else {
            ResultadoLogin.CredencialesInvalidas
        }
    }

    private fun hayConexion(): Boolean {
        return try {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val red = manager.activeNetwork ?: return false
            val capacidades = manager.getNetworkCapabilities(red) ?: return false
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val VERIFICAR_CONECTIVIDAD = true
    }
}