package com.example.modoguardian.ui.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Hora corta, por ejemplo 14:32
fun Long.aHora(): String =
    DateTimeFormatter.ofPattern("HH:mm").format(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))

// Fecha y hora, por ejemplo 05-11-2026 14:32:18
fun Long.aFechaHora(): String =
    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))

// Inicio de la hora a la que pertenece el instante (sirve para agrupar eventos por hora)
fun Long.inicioDeHora(): Long =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.HOURS).toInstant().toEpochMilli()

// Texto relativo: Ahora, Hace 5 min, Hace 2 h
fun Long.haceTiempo(): String {
    val minutos = (System.currentTimeMillis() - this) / 60_000L
    return when {
        minutos < 1 -> "Ahora"
        minutos < 60 -> "Hace $minutos min"
        minutos < 1440 -> "Hace ${minutos / 60} h"
        else -> "Hace ${minutos / 1440} d"
    }
}

// 95 segundos -> 01:35
fun formatearSegundos(total: Int): String = "%02d:%02d".format(total / 60, total % 60)