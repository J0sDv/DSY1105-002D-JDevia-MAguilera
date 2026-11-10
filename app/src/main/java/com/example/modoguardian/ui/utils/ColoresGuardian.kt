package com.example.modoguardian.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.modoguardian.model.EstadoCamara
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Prioridad

// Colores semanticos de estados y prioridades (el resto del color viene de MaterialTheme)
object ColoresGuardian {
    val Rojo = Color(0xFFEF4444)
    val Ambar = Color(0xFFF59E0B)
    val Verde = Color(0xFF22C55E)
    val Azul = Color(0xFF3B82F6)
    val Gris = Color(0xFF9CA3AF)
}

fun Prioridad.color(): Color = when (this) {
    Prioridad.CRITICA -> ColoresGuardian.Rojo
    Prioridad.ALERTA -> ColoresGuardian.Ambar
    Prioridad.EVENTO -> ColoresGuardian.Azul
}

fun EstadoEvento.color(): Color = when (this) {
    EstadoEvento.PENDIENTE -> ColoresGuardian.Ambar
    EstadoEvento.CONFIRMADA -> ColoresGuardian.Verde
    EstadoEvento.FALSA_ALARMA -> ColoresGuardian.Gris
    EstadoEvento.RESUELTO -> ColoresGuardian.Azul
    EstadoEvento.AUTORIZADO -> ColoresGuardian.Verde
}

fun EstadoCamara.color(): Color = when (this) {
    EstadoCamara.EN_LINEA -> ColoresGuardian.Verde
    EstadoCamara.OFFLINE -> ColoresGuardian.Rojo
    EstadoCamara.MANTENCION -> ColoresGuardian.Ambar
}