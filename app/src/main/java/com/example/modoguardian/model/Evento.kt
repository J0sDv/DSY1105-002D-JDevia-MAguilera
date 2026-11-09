package com.example.modoguardian.model

// Nivel de prioridad del evento
enum class Prioridad(val etiqueta: String) {
    CRITICA("Crítica"),
    ALERTA("Alerta"),
    EVENTO("Evento")
}

// Estado de atencion del evento
enum class EstadoEvento(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    FALSA_ALARMA("Falsa alarma"),
    RESUELTO("Resuelto"),
    AUTORIZADO("Autorizado")
}

// Tipo de evento
enum class TipoEvento(val etiqueta: String) {
    INTRUSION("Intrusión perimetral"),
    ACCESO("Control de acceso"),
    DESCONEXION("Cámara desconectada"),
    RONDA("Ronda de guardia"),
    PUERTA("Puerta abierta"),
    BATERIA("Batería de sensor"),
    INCIDENCIA("Incidencia reportada")
}

// Evento o alerta
data class Evento(
    val id: String,
    val titulo: String,
    val detalle: String,
    val camaraId: String,
    val camaraNombre: String,
    val zona: String,
    val tipo: TipoEvento,
    val prioridad: Prioridad,
    val estado: EstadoEvento,
    val fechaMillis: Long,            // cuando ocurrio
    val confianzaIA: Int = 0,         // 0 a 100
    val duracionSeg: Int = 0,
    val atendidoMillis: Long = 0L     // cuando se atendio (0 = aun no); sirve para el tiempo de respuesta
) {
    // Una alerta pendiente (no un simple evento informativo) requiere acción
    val requiereAtencion: Boolean
        get() = estado == EstadoEvento.PENDIENTE && prioridad != Prioridad.EVENTO
}