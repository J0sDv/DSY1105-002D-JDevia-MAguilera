package com.example.modoguardian.local

import com.example.modoguardian.model.Camara
import com.example.modoguardian.model.EstadoCamara
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Evento
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.TipoEvento
import com.example.modoguardian.model.enumSeguro

fun Camara.aEntidad() = CamaraEntity(id, nombre, zona, estado.name, hd, esTelefono)

fun CamaraEntity.aModelo() = Camara(
    id = id,
    nombre = nombre,
    zona = zona,
    estado = enumSeguro(estado, EstadoCamara.OFFLINE),
    hd = hd,
    esTelefono = esTelefono
)

fun Evento.aEntidad() = EventoEntity(
    id, titulo, detalle, camaraId, camaraNombre, zona,
    tipo.name, prioridad.name, estado.name,
    fechaMillis, confianzaIA, duracionSeg, atendidoMillis
)

fun EventoEntity.aModelo() = Evento(
    id = id,
    titulo = titulo,
    detalle = detalle,
    camaraId = camaraId,
    camaraNombre = camaraNombre,
    zona = zona,
    tipo = enumSeguro(tipo, TipoEvento.INCIDENCIA),
    prioridad = enumSeguro(prioridad, Prioridad.EVENTO),
    estado = enumSeguro(estado, EstadoEvento.PENDIENTE),
    fechaMillis = fechaMillis,
    confianzaIA = confianzaIA,
    duracionSeg = duracionSeg,
    atendidoMillis = atendidoMillis
)