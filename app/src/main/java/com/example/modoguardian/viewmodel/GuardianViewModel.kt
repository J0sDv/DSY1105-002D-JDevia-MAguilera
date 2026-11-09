package com.example.modoguardian.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.*
import com.example.modoguardian.notificaciones.Notificador
import com.example.modoguardian.repository.GuardianRepository
import com.example.modoguardian.repository.PreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuardianViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = GuardianRepository(application.applicationContext)
    private val prefs = PreferenciasRepository(application.applicationContext)

    // Datos que leen las pantallas (vienen de Room)
    val camaras: StateFlow<List<Camara>> =
        repo.camaras.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val eventos: StateFlow<List<Evento>> =
        repo.eventos.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val indicadores: StateFlow<Indicadores> =
        combine(camaras, eventos) { c, e -> calcularIndicadores(c, e) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Indicadores())

    // Ajustes
    private val _modoOscuro = MutableStateFlow(prefs.obtenerModoOscuro())
    val modoOscuro: StateFlow<Boolean?> = _modoOscuro
    private val _notificaciones = MutableStateFlow(prefs.obtenerNotificaciones())
    val notificaciones: StateFlow<Boolean> = _notificaciones

    // Estado de operaciones y mensajes para el usuario (Toast)
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando
    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    // Lista de usuarios (solo la usa el Administrador)
    private val _usuarios = MutableStateFlow<List<PerfilUsuario>>(emptyList())
    val usuarios: StateFlow<List<PerfilUsuario>> = _usuarios

    // Sincronizacion
    fun iniciarSincronizacion() {
        repo.iniciarSincronizacion(
            scope = viewModelScope,
            onAlertaNueva = { evento ->
                if (_notificaciones.value) Notificador.mostrarAlerta(getApplication<Application>(), evento)
            },
            onError = { _mensajes.tryEmit(it) }
        )
    }

    fun detenerSincronizacion() = repo.detenerSincronizacion()

    override fun onCleared() {
        repo.detenerSincronizacion()
        super.onCleared()
    }

    // Datos de prueba y simulación
    fun cargarDatosPrueba() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val enviado = repo.cargarDatosPrueba()
                _mensajes.emit(if (enviado) "Datos de prueba cargados" else "Guardado en el dispositivo; se enviará al reconectar")
            } catch (e: Exception) {
                _mensajes.emit("No se pudieron cargar los datos. ¿Tu cuenta es Administrador y publicaste las reglas?")
            }
            _cargando.value = false
        }
    }

    // Crea una alerta crítica ficticia (dispara la notificación local de prueba)
    fun simularAlerta() {
        viewModelScope.launch {
            val camara = camaras.value.filter { it.enLinea }.randomOrNull()
            val ahora = System.currentTimeMillis()
            val evento = Evento(
                id = "sim$ahora",
                titulo = "Movimiento no autorizado detectado",
                detalle = "Alerta simulada para pruebas",
                camaraId = camara?.id ?: "cam02",
                camaraNombre = camara?.nombre ?: "Perímetro Norte",
                zona = camara?.zona ?: "Zona Sur",
                tipo = TipoEvento.INTRUSION,
                prioridad = Prioridad.CRITICA,
                estado = EstadoEvento.PENDIENTE,
                fechaMillis = ahora,
                confianzaIA = (80..97).random(),
                duracionSeg = (5..20).random()
            )
            try {
                repo.crearEvento(evento)
                _mensajes.emit("Alerta simulada creada")
            } catch (e: Exception) {
                _mensajes.emit("No se pudo crear la alerta")
            }
        }
    }

    // Incidencias y acciones (trazabilidad)
    fun reportarIncidencia(descripcion: String, camara: Camara?, sesion: LoginUiState) {
        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            val evento = Evento(
                id = "inc$ahora",
                titulo = "Incidencia reportada",
                detalle = "$descripcion (por ${sesion.nombreUsuario})",
                camaraId = camara?.id ?: "",
                camaraNombre = camara?.nombre ?: "Sin cámara",
                zona = camara?.zona ?: "Zona Sur",
                tipo = TipoEvento.INCIDENCIA,
                prioridad = Prioridad.ALERTA,
                estado = EstadoEvento.PENDIENTE,
                fechaMillis = ahora
            )
            try {
                val enviado = repo.crearEvento(evento)
                _mensajes.emit(if (enviado) "Incidencia reportada" else "Incidencia guardada; se enviará al reconectar")
            } catch (e: Exception) {
                _mensajes.emit("No se pudo reportar la incidencia")
            }
        }
    }

    // Cambia el estado de un evento y deja registro de quién lo hizo
    fun atenderEvento(evento: Evento, nuevoEstado: EstadoEvento?, textoAccion: String, sesion: LoginUiState) {
        viewModelScope.launch {
            try {
                val enviado = repo.atenderEvento(
                    evento, nuevoEstado, textoAccion, sesion.nombreUsuario, sesion.rol ?: Rol.OPERADOR
                )
                _mensajes.emit(if (enviado) "Acción registrada" else "Acción guardada; se enviará al reconectar")
            } catch (e: Exception) {
                _mensajes.emit("No se pudo registrar la acción")
            }
        }
    }

    // Registra una acción sin cambiar el estado (por ejemplo, una llamada o una foto)
    fun registrarAccion(evento: Evento, textoAccion: String, sesion: LoginUiState) =
        atenderEvento(evento, null, textoAccion, sesion)

    fun acciones(eventoId: String): Flow<List<AccionRegistro>> = repo.acciones(eventoId)

    // Administración de usuarios
    fun cargarUsuarios() {
        viewModelScope.launch {
            try {
                _usuarios.value = repo.listarUsuarios()
            } catch (e: Exception) {
                _mensajes.emit("No se pudo leer la lista (solo el Administrador puede)")
            }
        }
    }

    fun cambiarRol(uid: String, rol: Rol) {
        viewModelScope.launch {
            try {
                repo.cambiarRol(uid, rol)
                _usuarios.update { lista -> lista.map { if (it.uid == uid) it.copy(rol = rol) else it } }
                _mensajes.emit("Rol actualizado. La persona debe volver a iniciar sesión")
            } catch (e: Exception) {
                _mensajes.emit("No se pudo cambiar el rol")
            }
        }
    }

    // Cuenta y ajustes
    fun enviarRestablecimiento(email: String) {
        viewModelScope.launch {
            try {
                repo.enviarRestablecimiento(email)
                _mensajes.emit("Te enviamos un correo para cambiar tu contraseña")
            } catch (e: Exception) {
                _mensajes.emit("No se pudo enviar el correo. Revisa tu conexión")
            }
        }
    }

    fun setModoOscuro(valor: Boolean) {
        _modoOscuro.value = valor
        try { prefs.guardarModoOscuro(valor) } catch (e: Exception) { /* opcional */ }
    }

    fun setNotificaciones(valor: Boolean) {
        _notificaciones.value = valor
        try { prefs.guardarNotificaciones(valor) } catch (e: Exception) { /* opcional */ }
    }
}