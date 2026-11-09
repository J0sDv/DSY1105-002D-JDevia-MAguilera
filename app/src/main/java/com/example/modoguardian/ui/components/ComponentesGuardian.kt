package com.example.modoguardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Camara
import com.example.modoguardian.model.Evento
import com.example.modoguardian.model.TipoEvento
import com.example.modoguardian.ui.utils.ColoresGuardian
import com.example.modoguardian.ui.utils.aHora
import com.example.modoguardian.ui.utils.color

// Etiqueta de color (estado, prioridad, rol)
@Composable
fun Etiqueta(texto: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Número grande con su título debajo (indicadores)
@Composable
fun IndicadorNumero(valor: String, etiqueta: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Título de sección con enlace opcional a la derecha ("Ver todas")
@Composable
fun SeccionTitulo(titulo: String, accion: String? = null, onAccion: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (accion != null) TextButton(onClick = onAccion) { Text(accion) }
    }
}

// Ícono según el tipo de evento
fun TipoEvento.icono(): ImageVector = when (this) {
    TipoEvento.INTRUSION -> Icons.Filled.Warning
    TipoEvento.ACCESO -> Icons.Filled.CheckCircle
    TipoEvento.DESCONEXION -> Icons.Filled.VideocamOff
    TipoEvento.RONDA -> Icons.Filled.Shield
    TipoEvento.PUERTA -> Icons.Filled.Lock
    TipoEvento.BATERIA -> Icons.Filled.Info
    TipoEvento.INCIDENCIA -> Icons.Filled.Info
}

// Tarjeta de cámara para las grillas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaCamara(camara: Camara, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (camara.enLinea) Etiqueta("● REC", ColoresGuardian.Rojo)
                else Etiqueta("OFFLINE", ColoresGuardian.Gris)
                if (camara.hd) Text("HD", style = MaterialTheme.typography.labelSmall)
            }
            Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (camara.enLinea) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = camara.nombre,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(camara.zona, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(camara.estado.etiqueta, style = MaterialTheme.typography.bodySmall, color = camara.estado.color())
            }
        }
    }
}

// Fila de evento para listas (Inicio, Eventos y Alertas)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilaEvento(evento: Evento, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colorPrioridad = evento.prioridad.color()
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colorPrioridad.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(evento.tipo.icono(), contentDescription = null, tint = colorPrioridad)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Etiqueta(evento.prioridad.etiqueta.uppercase(), colorPrioridad)
                    Text(
                        evento.fechaMillis.aHora(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(evento.titulo, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${evento.camaraNombre} · ${evento.detalle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Etiqueta(evento.estado.etiqueta, evento.estado.color())
        }
    }
}

// Fila de ajuste del Perfil: ícono, título, subtítulo y un elemento a la derecha (switch, etiqueta o flecha)
@Composable
fun FilaAjuste(
    icono: ImageVector,
    titulo: String,
    subtitulo: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val base = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = base.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo)
            if (subtitulo != null) {
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailing != null) trailing()
        else if (onClick != null) Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

// Grupo de ajustes dentro de una tarjeta, con título en mayúsculas
@Composable
fun SeccionAjustes(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(content = contenido)
        }
    }
}