package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.example.modoguardian.model.Evento
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.ui.components.FilaEvento
import com.example.modoguardian.viewmodel.GuardianViewModel

@Composable
fun AlertasScreen(guardianVm: GuardianViewModel, onAbrirAlerta: (String) -> Unit) {
    val eventos by guardianVm.eventos.collectAsState()
    val ind by guardianVm.indicadores.collectAsState()
    var soloPendientes by rememberSaveable { mutableStateOf(true) }

    // Alertas: todo lo que no es un simple evento informativo; las pendientes y mas graves primero
    val alertas = eventos
        .filter { it.prioridad != Prioridad.EVENTO }
        .filter { !soloPendientes || it.requiereAtencion }
        .sortedWith(
            compareByDescending<Evento> { it.requiereAtencion }
                .thenBy { it.prioridad.ordinal }
                .thenByDescending { it.fechaMillis }
        )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Alertas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "${ind.pendientes} pendientes de atención",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = soloPendientes, onClick = { soloPendientes = true }, label = { Text("Pendientes") })
                    FilterChip(selected = !soloPendientes, onClick = { soloPendientes = false }, label = { Text("Todas") })
                }
            }
        }

        if (alertas.isEmpty()) {
            item {
                Text(
                    if (soloPendientes) "No hay alertas pendientes. ¡Buen trabajo!" else "No hay alertas registradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(alertas, key = { it.id }) { alerta ->
            FilaEvento(evento = alerta, onClick = { onAbrirAlerta(alerta.id) })
        }
    }
}