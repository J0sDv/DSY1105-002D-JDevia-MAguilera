package com.example.modoguardian.model

// Estados posibles de una camara
enum class EstadoCamara(val etiqueta: String) {
    EN_LINEA("En línea"),
    OFFLINE("Sin conexión"),
    MANTENCION("Mantención")
}

// Camara o dispositivo simulado de Modo Guardian
data class Camara(
    val id: String,
    val nombre: String,
    val zona: String,
    val estado: EstadoCamara,
    val hd: Boolean = true,
    val esTelefono: Boolean = false // true: usa la camara real del telefono
) {
    val enLinea: Boolean get() = estado == EstadoCamara.EN_LINEA
}