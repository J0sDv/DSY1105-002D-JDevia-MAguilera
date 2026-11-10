package com.example.modoguardian.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla local de camaras
@Entity(tableName = "camaras")
data class CamaraEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val zona: String,
    val estado: String,
    val hd: Boolean,
    val esTelefono: Boolean
)

// Tabla local de eventos
@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val detalle: String,
    val camaraId: String,
    val camaraNombre: String,
    val zona: String,
    val tipo: String,
    val prioridad: String,
    val estado: String,
    val fechaMillis: Long,
    val confianzaIA: Int,
    val duracionSeg: Int,
    val atendidoMillis: Long
)