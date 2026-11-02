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

    // Repositorios: Firebase (cuentas) y preferencias locales (correo recordado)
    private val authRepository = AuthRepository()
    private val preferencias = PreferenciasRepository(application.applicationContext)

    // Estado interno mutable
    private val _estado = MutableStateFlow(LoginUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<LoginUiState> = _estado

    init {
        // Si el usuario pidio recordar su correo, se carga al abrir la app
        try {
            val correoGuardado = preferencias.obtenerCorreo()
            if (correoGuardado.isNotBlank()) {
                _estado.update { it.copy(email = correoGuardado, recordarCorreo = true) }
            }
        } catch (e: Exception) {
            // Si falla la lectura local, el formulario parte vacio (la app no se cae)
        }
    }

    // Actualiza el campo email y limpia sus errores
    fun onEmailChange(valor: String) {
        _estado.update { it.copy(email = valor, errores = it.errores.copy(email = null, general = null)) }
    }

    // Actualiza el campo password y limpia sus errores
    fun onPasswordChange(valor: String) {
        _estado.update { it.copy(password = valor, errores = it.errores.copy(password = null, general = null)) }
    }

    // Actualiza el checkbox "Recordar mi correo"
    fun onRecordarCorreoChange(valor: Boolean) {
        _estado.update { it.copy(recordarCorreo = valor) }
    }

    // Validacion global del formulario: retorna true si no hay errores
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

    // Accion del boton "Iniciar sesion": valida, muestra carga y consulta Firebase
    fun onLoginClick() {
        // Evita doble clic mientras ya hay una consulta en curso
        if (_estado.value.isLoading) return
        if (!validarFormulario()) return

        val actual = _estado.value

        viewModelScope.launch {
            _estado.update { it.copy(isLoading = true, errores = it.errores.copy(general = null)) }

            // try/catch: ningun error de datos debe detener ni reiniciar la app
            try {
                val resultado = authRepository.iniciarSesion(actual.email, actual.password)
                if (resultado is ResultadoLogin.Exito) guardarPreferencia(actual)
                procesarResultado(resultado)
            } catch (e: Exception) {
                mostrarErrorGeneral("Ocurrió un error inesperado. Inténtalo nuevamente")
            }
        }
    }

    // Recibe el token de Google (lo obtiene la pantalla con Credential Manager) y entra con Firebase
    fun onGoogleToken(idToken: String) {
        if (_estado.value.isLoading) return

        viewModelScope.launch {
            _estado.update { it.copy(isLoading = true, errores = it.errores.copy(general = null)) }
            try {
                procesarResultado(authRepository.iniciarSesionConGoogle(idToken))
            } catch (e: Exception) {
                mostrarErrorGeneral("Ocurrió un error inesperado. Inténtalo nuevamente")
            }
        }
    }

    // Muestra el error cuando Google no pudo entregar la cuenta
    fun onGoogleError(mensaje: String) {
        mostrarErrorGeneral(mensaje)
    }

    // Cierra la sesion: cierra Firebase y limpia el formulario y el correo guardado
    fun cerrarSesion() {
        try {
            authRepository.cerrarSesion()
            preferencias.borrarCorreo()
        } catch (e: Exception) {
            // Si falla el borrado, igual se cierra la sesion en pantalla (la app no se cae)
        }

        _estado.update {
            it.copy(
                email = "",
                password = "",
                recordarCorreo = false,
                rol = null,
                nombreUsuario = "",
                isLoading = false,
                errores = LoginErrores()
            )
        }
    }

    // Traduce el resultado del repositorio a estado de pantalla
    private fun procesarResultado(resultado: ResultadoLogin) {
        when (resultado) {
            is ResultadoLogin.Exito -> {
                // Se guarda el rol: el Home lo lee desde este mismo ViewModel
                _estado.update {
                    it.copy(
                        isLoading = false,
                        email = resultado.usuario.email,
                        rol = resultado.usuario.rol,
                        nombreUsuario = resultado.usuario.nombre
                    )
                }
            }
            ResultadoLogin.CredencialesInvalidas -> mostrarErrorGeneral("Correo o contraseña incorrectos")
            is ResultadoLogin.CorreoNoVerificado -> {
                if (resultado.reenviado) {
                    mostrarErrorGeneral(
                        "Tu correo aún no está verificado. Te reenviamos el enlace a ${resultado.email}. " +
                                "Ábrelo (revisa también el spam) y vuelve a iniciar sesión."
                    )
                } else {
                    mostrarErrorGeneral(
                        "Tu correo aún no está verificado. Revisa tu bandeja de entrada " +
                                "o inténtalo de nuevo en unos minutos."
                    )
                }
            }
            ResultadoLogin.SinConexion -> mostrarErrorGeneral("Sin conexión. Revisa tu WiFi o datos móviles e inténtalo nuevamente")
            is ResultadoLogin.Error -> mostrarErrorGeneral(resultado.mensaje)
        }
    }

    // Muestra un error general y termina la carga
    private fun mostrarErrorGeneral(mensaje: String) {
        _estado.update { it.copy(isLoading = false, errores = it.errores.copy(general = mensaje)) }
    }

    // Guarda o borra el correo segun el checkbox (un fallo aqui no afecta el login)
    private fun guardarPreferencia(actual: LoginUiState) {
        try {
            if (actual.recordarCorreo) {
                preferencias.guardarCorreo(actual.email.trim())
            } else {
                preferencias.borrarCorreo()
            }
        } catch (e: Exception) {
            // Persistencia opcional: se ignora el error
        }
    }
}