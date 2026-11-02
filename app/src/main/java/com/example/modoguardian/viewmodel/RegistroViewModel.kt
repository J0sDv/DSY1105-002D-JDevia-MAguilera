package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.RegistroErrores
import com.example.modoguardian.model.RegistroUiState
import com.example.modoguardian.model.ResultadoRegistro
import com.example.modoguardian.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {

    // Repositorio que se conecta con Firebase
    private val authRepository = AuthRepository()

    // Estado interno mutable
    private val _estado = MutableStateFlow(RegistroUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<RegistroUiState> = _estado

    // Actualiza el campo correo y limpia sus errores
    fun onEmailChange(valor: String) {
        _estado.update { it.copy(email = valor, errores = it.errores.copy(email = null, general = null)) }
    }

    // Actualiza el campo contraseña y limpia sus errores
    fun onPasswordChange(valor: String) {
        _estado.update { it.copy(password = valor, errores = it.errores.copy(password = null, general = null)) }
    }

    // Actualiza el campo confirmar contraseña y limpia sus errores
    fun onConfirmarPasswordChange(valor: String) {
        _estado.update { it.copy(confirmarPassword = valor, errores = it.errores.copy(confirmarPassword = null, general = null)) }
    }

    // Actualiza el campo username y limpia sus errores
    fun onUsernameChange(valor: String) {
        _estado.update { it.copy(username = valor, errores = it.errores.copy(username = null, general = null)) }
    }

    // Actualiza el pais o region elegido en el menu
    fun onRegionChange(valor: String) {
        _estado.update { it.copy(region = valor, errores = it.errores.copy(region = null)) }
    }

    // Validacion global del formulario: retorna true si no hay errores
    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value
        val errores = RegistroErrores(
            email = when {
                estadoActual.email.isBlank() -> "Campo obligatorio"
                !estadoActual.email.contains("@") -> "Correo inválido"
                else -> null
            },
            password = when {
                estadoActual.password.isBlank() -> "Campo obligatorio"
                !passwordValida(estadoActual.password) -> "Mínimo 8 caracteres, con una letra minúscula y un número"
                else -> null
            },
            confirmarPassword = when {
                estadoActual.confirmarPassword.isBlank() -> "Campo obligatorio"
                estadoActual.confirmarPassword != estadoActual.password -> "Las contraseñas no coinciden"
                else -> null
            },
            username = when {
                estadoActual.username.isBlank() -> "Campo obligatorio"
                !usernameValido(estadoActual.username.trim()) -> "3 a 20 caracteres: letras, números y guiones simples (no al inicio ni al final)"
                else -> null
            },
            region = if (estadoActual.region.isBlank()) "Selecciona un país o región" else null
        )

        val hayErrores = listOfNotNull(
            errores.email,
            errores.password,
            errores.confirmarPassword,
            errores.username,
            errores.region
        ).isNotEmpty()

        _estado.update { it.copy(errores = errores) }

        return !hayErrores
    }

    // Accion del boton "Crear cuenta": valida, muestra carga y crea la cuenta en Firebase
    fun onRegistrarClick() {
        // Evita doble clic mientras ya hay una consulta en curso
        if (_estado.value.isLoading) return
        if (!validarFormulario()) return

        val actual = _estado.value

        viewModelScope.launch {
            _estado.update { it.copy(isLoading = true, errores = it.errores.copy(general = null)) }

            // try/catch: ningun error de datos debe detener ni reiniciar la app
            try {
                when (val resultado = authRepository.registrarUsuario(actual.email, actual.password, actual.username, actual.region)) {
                    is ResultadoRegistro.Exito -> {
                        val mensaje = if (resultado.correoEnviado) {
                            "Te enviamos un enlace de verificación a ${actual.email.trim()}. " +
                                    "Ábrelo para activar tu cuenta y luego inicia sesión. " +
                                    "Si no lo ves, revisa la carpeta de spam."
                        } else {
                            "Tu cuenta fue creada, pero no pudimos enviar el correo de verificación. " +
                                    "Inicia sesión y te lo reenviaremos."
                        }
                        _estado.update { it.copy(isLoading = false, mensajeCuentaCreada = mensaje) }
                    }
                    ResultadoRegistro.CorreoExistente -> {
                        _estado.update {
                            it.copy(isLoading = false, errores = it.errores.copy(email = "Este correo ya está registrado"))
                        }
                    }
                    ResultadoRegistro.UsernameExistente -> {
                        _estado.update {
                            it.copy(isLoading = false, errores = it.errores.copy(username = "Este nombre de usuario ya existe"))
                        }
                    }
                    ResultadoRegistro.SinConexion -> {
                        mostrarErrorGeneral("Sin conexión. Revisa tu WiFi o datos móviles e inténtalo nuevamente")
                    }
                    is ResultadoRegistro.Error -> mostrarErrorGeneral(resultado.mensaje)
                }
            } catch (e: Exception) {
                mostrarErrorGeneral("Ocurrió un error inesperado. Inténtalo nuevamente")
            }
        }
    }

    // Muestra el error cuando google no pudo entregar la cuenta
    fun onGoogleError(mensaje: String) {
        mostrarErrorGeneral(mensaje)
    }

    // Muestra un error general y termina la carga
    private fun mostrarErrorGeneral(mensaje: String) {
        _estado.update { it.copy(isLoading = false, errores = it.errores.copy(general = mensaje)) }
    }

    // Contraseña valida: minimo 8 caracteres, con una minuscula y un numero
    private fun passwordValida(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLowerCase() }
    }

    // Username valido: 3 a 20 caracteres; letras, numeros y guiones simples (no al inicio ni al final)
    private fun usernameValido(username: String): Boolean {
        return username.length in 3..20 && Regex("[A-Za-z0-9]+(-[A-Za-z0-9]+)*").matches(username)
    }
}