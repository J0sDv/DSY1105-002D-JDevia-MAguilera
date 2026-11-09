package com.example.modoguardian.repository

import com.example.modoguardian.model.Camara
import com.example.modoguardian.model.EstadoCamara
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Evento
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.TipoEvento

// Datos ficticios y sinteticos
object DatosPrueba {

    fun camaras() = listOf(
        Camara("cam01", "Entrada Principal", "Zona Sur", EstadoCamara.EN_LINEA, esTelefono = true), // usa la cámara del teléfono
        Camara("cam02", "Perímetro Norte", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara("cam03", "Bodega A", "Zona Sur", EstadoCamara.OFFLINE),
        Camara("cam04", "Estacionamiento", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara("cam05", "Almacén Central", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara("cam06", "Perímetro Este", "Zona Sur", EstadoCamara.MANTENCION)
    )

    // Las fechas son relativas a "ahora" para que siempre se vean recientes
    fun eventos(ahora: Long): List<Evento> = listOf(
        ev(ahora, "ev01", "Movimiento no autorizado detectado", "Zona C · Sensor PIR-04", "cam02", "Perímetro Norte",
            TipoEvento.INTRUSION, Prioridad.CRITICA, EstadoEvento.PENDIENTE, minutos = 2, conf = 92, dur = 12),
        ev(ahora, "ev02", "Movimiento detectado", "Zona 02 · Sensor PIR-02", "cam02", "Perímetro Norte",
            TipoEvento.INTRUSION, Prioridad.ALERTA, EstadoEvento.PENDIENTE, minutos = 12, conf = 74, dur = 6),
        ev(ahora, "ev03", "Acceso autorizado", "Credencial #4582 · A. Rivera", "cam01", "Entrada Principal",
            TipoEvento.ACCESO, Prioridad.EVENTO, EstadoEvento.AUTORIZADO, minutos = 25),
        ev(ahora, "ev04", "Cámara desconectada", "Offline 3m 12s · Auto-reconexión", "cam03", "Bodega A",
            TipoEvento.DESCONEXION, Prioridad.ALERTA, EstadoEvento.RESUELTO, minutos = 40, respMin = 4),
        ev(ahora, "ev05", "Puerta abierta fuera de horario", "Puerta D-02 · 4m abierta", "cam05", "Almacén Central",
            TipoEvento.PUERTA, Prioridad.ALERTA, EstadoEvento.PENDIENTE, minutos = 70),
        ev(ahora, "ev06", "Ronda completada", "Guardián · Ruta 2 · 18 checkpoints", "cam04", "Estacionamiento",
            TipoEvento.RONDA, Prioridad.EVENTO, EstadoEvento.RESUELTO, minutos = 100, respMin = 1),
        ev(ahora, "ev07", "Salida autorizada", "Placa JKL-892 · Logística", "cam04", "Estacionamiento",
            TipoEvento.ACCESO, Prioridad.EVENTO, EstadoEvento.AUTORIZADO, minutos = 130),
        ev(ahora, "ev08", "Batería baja - Sensor", "SEN-11 · 12% restante", "cam06", "Perímetro Este",
            TipoEvento.BATERIA, Prioridad.ALERTA, EstadoEvento.FALSA_ALARMA, minutos = 160, respMin = 6)
    )

    // Ayuda para armar un evento: respMin > 0 indica que ya fue atendido y cuanto tardo
    private fun ev(
        ahora: Long, id: String, titulo: String, detalle: String,
        camaraId: String, camaraNombre: String,
        tipo: TipoEvento, prioridad: Prioridad, estado: EstadoEvento,
        minutos: Long, conf: Int = 0, dur: Int = 0, respMin: Long = 0
    ) = Evento(
        id = id, titulo = titulo, detalle = detalle,
        camaraId = camaraId, camaraNombre = camaraNombre, zona = "Zona Sur",
        tipo = tipo, prioridad = prioridad, estado = estado,
        fechaMillis = ahora - minutos * 60_000L,
        confianzaIA = conf, duracionSeg = dur,
        atendidoMillis = if (respMin > 0) ahora - (minutos - respMin) * 60_000L else 0L
    )
}