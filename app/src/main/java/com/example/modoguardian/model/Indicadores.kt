package com.example.modoguardian.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Indicadores basicos del caso: cantidad de eventos, alertas pendientes o atendidas y tiempos de respuesta
data class Indicadores(
    val totalCamaras: Int = 0,
    val activas: Int = 0,
    val enAtencion: Int = 0,
    val capacidad: Int = 0, // % de camaras en linea
    val eventosHoy: Int = 0,
    val criticas: Int = 0,
    val pendientes: Int = 0,
    val atendidas: Int = 0,
    val respuestaPromedioMin: Int = 0
)

// Funcion pura (sin Android)
fun calcularIndicadores(
    camaras: List<Camara>,
    eventos: List<Evento>,
    hoy: LocalDate = LocalDate.now()
): Indicadores {
    val activas = camaras.count { it.enLinea }
    val tiemposMin = eventos
        .filter { it.atendidoMillis > it.fechaMillis }
        .map { (it.atendidoMillis - it.fechaMillis) / 60_000L }

    return Indicadores(
        totalCamaras = camaras.size,
        activas = activas,
        enAtencion = camaras.size - activas,
        capacidad = if (camaras.isEmpty()) 0 else activas * 100 / camaras.size,
        eventosHoy = eventos.count {
            Instant.ofEpochMilli(it.fechaMillis).atZone(ZoneId.systemDefault()).toLocalDate() == hoy
        },
        criticas = eventos.count { it.prioridad == Prioridad.CRITICA },
        pendientes = eventos.count { it.requiereAtencion },
        atendidas = eventos.count {
            it.estado == EstadoEvento.CONFIRMADA ||
                    it.estado == EstadoEvento.FALSA_ALARMA ||
                    it.estado == EstadoEvento.RESUELTO
        },
        respuestaPromedioMin = if (tiemposMin.isEmpty()) 0 else tiemposMin.average().toInt()
    )
}