package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Camara
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.veSeguimiento
import com.example.modoguardian.ui.components.Etiqueta
import com.example.modoguardian.ui.components.FilaEvento
import com.example.modoguardian.ui.components.IndicadorNumero
import com.example.modoguardian.ui.components.SeccionTitulo
import com.example.modoguardian.ui.components.TarjetaCamara
import com.example.modoguardian.ui.utils.ColoresGuardian
import com.example.modoguardian.viewmodel.GuardianViewModel

@Composable
fun InicioScreen(
    guardianVm: GuardianViewModel,
    sesion: LoginUiState,
    onAbrirCamara: (String) -> Unit,
    onAbrirAlerta: (String) -> Unit,
    onVerCamaras: () -> Unit,
    onVerEventos: () -> Unit
) {
    val camaras by guardianVm.camaras.collectAsState()
    val eventos by guardianVm.eventos.collectAsState()
    val ind by guardianVm.indicadores.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    val colorTarjeta = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado: avatar, saludo y globo de alertas pendientes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sesion.nombreUsuario.take(2).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hola, ${sesion.nombreUsuario}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${sesion.rol?.etiqueta ?: ""} • Turno activo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BadgedBox(badge = { if (ind.pendientes > 0) Badge { Text("${ind.pendientes}") } }) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Alertas pendientes")
                }
                Etiqueta("EN LÍNEA", ColoresGuardian.Verde)
            }

            // Estado general (se calcula en vivo desde Room)
            Card(modifier = Modifier.fillMaxWidth(), colors = colorTarjeta) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Estado general",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        IndicadorNumero("${ind.totalCamaras}", "Cámaras", MaterialTheme.colorScheme.onSurface)
                        IndicadorNumero("${ind.activas}", "En línea", ColoresGuardian.Verde)
                        IndicadorNumero("${ind.enAtencion}", "Atención", ColoresGuardian.Ambar)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Capacidad operativa", style = MaterialTheme.typography.bodySmall)
                        Text("${ind.capacidad}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = { ind.capacidad / 100f }, modifier = Modifier.fillMaxWidth())
                    Text(
                        "Datos sincronizados con Firebase y guardados en el dispositivo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Seguimiento: solo Supervisor y Administrador
            if (sesion.rol?.veSeguimiento == true) {
                Card(modifier = Modifier.fillMaxWidth(), colors = colorTarjeta) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Seguimiento de atención",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            IndicadorNumero("${ind.pendientes}", "Pendientes", ColoresGuardian.Ambar)
                            IndicadorNumero("${ind.atendidas}", "Atendidas", ColoresGuardian.Verde)
                            IndicadorNumero("${ind.respuestaPromedioMin} min", "Respuesta prom.", ColoresGuardian.Azul)
                        }
                        Text(
                            "${ind.eventosHoy} eventos registrados hoy · ${ind.criticas} alertas críticas en total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Camaras asignadas
            SeccionTitulo("Cámaras asignadas", "Ver todas", onVerCamaras)
            if (camaras.isEmpty()) {
                Text(
                    "Aún no hay datos. Un Administrador puede cargarlos desde Perfil > Cargar datos de prueba.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            camaras.take(4).chunked(2).forEach { fila ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fila.forEach { camara ->
                        TarjetaCamara(camara = camara, onClick = { onAbrirCamara(camara.id) }, modifier = Modifier.weight(1f))
                    }
                    if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Eventos en vivo: los 4 más recientes
            SeccionTitulo("Eventos en vivo", "Ver historial", onVerEventos)
            eventos.take(4).forEach { evento ->
                FilaEvento(evento = evento, onClick = { onAbrirAlerta(evento.id) })
            }

            // Espacio para que el botón flotante no tape el último evento
            Spacer(modifier = Modifier.height(72.dp))
        }

        ExtendedFloatingActionButton(
            onClick = { mostrarDialogo = true },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            text = { Text("Reportar incidencia") },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    if (mostrarDialogo) {
        DialogoIncidencia(
            camaras = camaras,
            onCancelar = { mostrarDialogo = false },
            onEnviar = { texto, camara ->
                guardianVm.reportarIncidencia(texto, camara, sesion)
                mostrarDialogo = false
            }
        )
    }
}

// Formulario para reportar una incidencia (se guarda como evento pendiente en Firestore)
@Composable
private fun DialogoIncidencia(
    camaras: List<Camara>,
    onCancelar: () -> Unit,
    onEnviar: (String, Camara?) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var camara by remember { mutableStateOf(camaras.firstOrNull()) }
    var menu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Reportar incidencia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Describe lo ocurrido") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (camaras.isNotEmpty()) {
                    Box {
                        OutlinedButton(onClick = { menu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(camara?.nombre ?: "Selecciona una cámara")
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            camaras.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.nombre) },
                                    onClick = { camara = c; menu = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onEnviar(texto.trim(), camara) }, enabled = texto.isNotBlank()) { Text("Enviar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}