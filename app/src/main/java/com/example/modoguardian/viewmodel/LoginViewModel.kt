package com.example.modoguardian.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.LoginErrores
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.ResultadoLogin
import com.example.modoguardian.repository.AuthRepository
import com.example.modoguardian.repository.PreferenciasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application.applicationContext)
    private val preferencias = PreferenciasRepository(application.applicationContext)
    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado

    init {
        try {
            val correoGuardado = preferencias.obtenerCorreo()
            if (correoGuardado.isNotBlank()) {
                _estado.update { it.copy(email = correoGuardado, recordarCorreo = true) }
            }
        } catch (e: Exception) {
        }
    }

    fun onEmailChange(valor: String) {
        _estado.update { it.copy(email = valor, errores = it.errores.copy(email = null, general = null)) }
    }

    fun onPasswordChange(valor: String) {
        _estado.update { it.copy(password = valor, errores = it.errores.copy(password = null, general = null)) }
    }

    fun onRecordarCorreoChange(valor: Boolean) {
        _estado.update { it.copy(recordarCorreo = valor) }
    }

    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value
        val errores = LoginErrores(
            email = when {
                estadoActual.email.isBlank() -> "Campo obligatorio"
                !estadoActual.email.contains("@") -> "Correo inválido"
                else -> null
            },
            password = when {
                estadoActual.password.isBlank() -> "Campo obligatorio"
                estadoActual.password.length < 6 -> "Debe tener al menos 6 caracteres"
                else -> null
            }
        )

        val hayErrores = listOfNotNull(
            errores.email,
            errores.password
        ).isNotEmpty()

        _estado.update { it.copy(errores = errores) }

        return !hayErrores
    }

    fun onLoginClick() {
        if (_estado.value.isLoading) return
        if (!validarFormulario()) return

        val actual = _estado.value

        viewModelScope.launch {
            _estado.update { it.copy(isLoading = true, errores = it.errores.copy(general = null)) }
            try {
                when (val resultado = authRepository.iniciarSesion(actual.email, actual.password)) {
                    is ResultadoLogin.Exito -> {
                        guardarPreferencia(actual)
                        _estado.update {
                            it.copy(
                                isLoading = false,
                                rol = resultado.usuario.rol,
                                nombreUsuario = resultado.usuario.nombre
                            )
                        }
                    }
                    ResultadoLogin.CredencialesInvalidas -> mostrarErrorGeneral("Correo o contraseña incorrectos")
                    ResultadoLogin.SinConexion -> mostrarErrorGeneral("Sin conexión. Revisa tu WiFi o datos móviles e inténtalo nuevamente")
                }
            } catch (e: Exception) {
                mostrarErrorGeneral("Ocurrió un error inesperado. Inténtalo nuevamente")
            }
        }
    }

    fun cerrarSesion() {
        _estado.update {
            it.copy(
                password = "",
                rol = null,
                nombreUsuario = "",
                isLoading = false,
                errores = LoginErrores()
            )
        }
    }

    private fun mostrarErrorGeneral(mensaje: String) {
        _estado.update { it.copy(isLoading = false, errores = it.errores.copy(general = mensaje)) }
    }

    private fun guardarPreferencia(actual: LoginUiState) {
        try {
            if (actual.recordarCorreo) {
                preferencias.guardarCorreo(actual.email.trim())
            } else {
                preferencias.borrarCorreo()
            }
        } catch (e: Exception) {
        }
    }
}