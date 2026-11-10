package com.example.modoguardian.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.TipoEvento
import com.example.modoguardian.ui.components.FilaEvento
import com.example.modoguardian.ui.components.IndicadorNumero
import com.example.modoguardian.ui.utils.ColoresGuardian
import com.example.modoguardian.ui.utils.aHora
import com.example.modoguardian.ui.utils.inicioDeHora
import com.example.modoguardian.viewmodel.GuardianViewModel


private enum class FiltroTipo(val etiqueta: String) {
    TODOS("Todos"),
    ALERTAS("Alertas"),
    EVENTOS("Eventos"),
    ACCESOS("Accesos")
}

@Composable
fun EventosScreen(guardianVm: GuardianViewModel, onAbrirAlerta: (String) -> Unit) {
    val eventos by guardianVm.eventos.collectAsState()
    val ind by guardianVm.indicadores.collectAsState()

    var filtro by rememberSaveable { mutableStateOf(FiltroTipo.TODOS) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var soloCriticas by rememberSaveable { mutableStateOf(false) }
    var soloPendientes by rememberSaveable { mutableStateOf(false) }

    // Filtros combinados: pestaña + texto + criticidad + estado
    val filtrados = eventos.filter { e ->
        val porTipo = when (filtro) {
            FiltroTipo.TODOS -> true
            FiltroTipo.ALERTAS -> e.prioridad != Prioridad.EVENTO
            FiltroTipo.EVENTOS -> e.tipo != TipoEvento.ACCESO
            FiltroTipo.ACCESOS -> e.tipo == TipoEvento.ACCESO
        }
        val porTexto = busqueda.isBlank() ||
                listOf(e.titulo, e.camaraNombre, e.zona, e.detalle).any { it.contains(busqueda, ignoreCase = true) }
        porTipo && porTexto && (!soloCriticas || e.prioridad == Prioridad.CRITICA) && (!soloPendientes || e.requiereAtencion)
    }

    // Agrupa por hora, de la mas reciente a la más antigua
    val grupos = filtrados.groupBy { it.fechaMillis.inicioDeHora() }.entries.sortedByDescending { it.key }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text("Historial", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Turno actual · 14:00 - 22:00",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IndicadorNumero("${ind.eventosHoy}", "Eventos hoy", MaterialTheme.colorScheme.onSurface)
                    IndicadorNumero("${ind.criticas}", "Críticas", ColoresGuardian.Rojo)
                    IndicadorNumero("${ind.pendientes}", "Pendientes", ColoresGuardian.Ambar)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroTipo.values().forEach { f ->
                    FilterChip(selected = filtro == f, onClick = { filtro = f }, label = { Text(f.etiqueta) })
                }
            }
        }

        item {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar por cámara, zona o tipo...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = soloCriticas, onClick = { soloCriticas = !soloCriticas }, label = { Text("Críticas") })
                FilterChip(selected = soloPendientes, onClick = { soloPendientes = !soloPendientes }, label = { Text("Pendientes") })
            }
        }

        if (grupos.isEmpty()) {
            item {
                Text(
                    "No hay eventos con estos filtros.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        grupos.forEach { (hora, lista) ->
            item(key = "h$hora") {
                Text(
                    "${hora.aHora()} - ${(hora + 3_600_000L).aHora()}  ·  ${lista.size} eventos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(lista, key = { it.id }) { evento ->
                FilaEvento(evento = evento, onClick = { onAbrirAlerta(evento.id) })
            }
        }
    }
}