package com.example.modoguardian.ui.screens

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.enumSeguro
import com.example.modoguardian.model.puedeAtenderAlertas
import com.example.modoguardian.ui.components.Etiqueta
import com.example.modoguardian.ui.components.SeccionTitulo
import com.example.modoguardian.ui.components.icono
import com.example.modoguardian.ui.utils.ColoresGuardian
import com.example.modoguardian.ui.utils.EscucharMensajes
import com.example.modoguardian.ui.utils.aFechaHora
import com.example.modoguardian.ui.utils.color
import com.example.modoguardian.ui.utils.formatearSegundos
import com.example.modoguardian.ui.utils.haceTiempo
import com.example.modoguardian.ui.utils.mostrarToast
import com.example.modoguardian.ui.utils.rememberSolicitarCamara
import com.example.modoguardian.viewmodel.GuardianViewModel
import kotlinx.coroutines.delay

@Composable
fun AlertaDetalleScreen(
    eventoId: String,
    guardianVm: GuardianViewModel,
    sesion: LoginUiState,
    onVerCamara: (String) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val eventos by guardianVm.eventos.collectAsState()
    val evento = eventos.firstOrNull { it.id == eventoId }
    // Historial de acciones de este evento (trazabilidad), en tiempo real
    val acciones by remember(eventoId) { guardianVm.acciones(eventoId) }.collectAsState(initial = emptyList())
    val rol = sesion.rol ?: Rol.OPERADOR
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    EscucharMensajes(guardianVm)

    LaunchedEffect(Unit) { while (true) { delay(1000); ahora = System.currentTimeMillis() } }

    // Foto de evidencia: se crea el archivo en la galería y la camara del sistema lo llena
    var uriFoto by remember { mutableStateOf<Uri?>(null) }
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        if (guardada && evento != null) {
            guardianVm.registrarAccion(evento, "Incidencia reportada con foto (evidencia en la galería)", sesion)
        } else {
            uriFoto?.let { runCatching { context.contentResolver.delete(it, null, null) } }
        }
    }
    val pedirCamara = rememberSolicitarCamara(onConcedido = {
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "incidencia_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ModoGuardian")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores)
        if (uri != null) {
            uriFoto = uri
            tomarFoto.launch(uri)
        }
    })

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Barra superior propia
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = if (evento?.prioridad == Prioridad.CRITICA) "Alerta crítica" else "Detalle del evento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (evento != null) {
                    Etiqueta(evento.prioridad.etiqueta.uppercase(), evento.prioridad.color())
                    Text(
                        "  ${evento.fechaMillis.haceTiempo()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (evento == null) {
                Text("Cargando evento…")
            } else {
                val colorPrioridad = evento.prioridad.color()
                // Resumen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorPrioridad.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(colorPrioridad.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(evento.tipo.icono(), contentDescription = null, tint = colorPrioridad)
                            }
                            Text(evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colorPrioridad)
                            Text("${evento.camaraNombre} · ${evento.zona}")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = colorPrioridad)
                            Text(evento.fechaMillis.aFechaHora())
                        }
                        if (evento.confianzaIA > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Visibility, contentDescription = null, tint = ColoresGuardian.Verde)
                                Text("Confianza IA: ${evento.confianzaIA}% persona detectada")
                            }
                            LinearProgressIndicator(
                                progress = { evento.confianzaIA / 100f },
                                color = ColoresGuardian.Verde,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Ver la camara asociada
                if (evento.camaraId.isNotBlank()) {
                    OutlinedButton(onClick = { onVerCamara(evento.camaraId) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Videocam, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Ver ${evento.camaraNombre} en vivo")
                    }
                }

                // Detalles
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("DETALLES DEL EVENTO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        FilaDato("Tipo", evento.tipo.etiqueta)
                        FilaDato("Zona", evento.zona)
                        FilaDato("Cámara", evento.camaraNombre)
                        if (evento.duracionSeg > 0) FilaDato("Duración", "${evento.duracionSeg} segundos")
                        FilaDato("Detalle", evento.detalle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estado", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Etiqueta(evento.estado.etiqueta, evento.estado.color())
                        }
                    }
                }

                // Acciones requeridas (segun el rol)
                Text("ACCIONES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                when {
                    !rol.puedeAtenderAlertas -> Text(
                        "Tu rol (${rol.etiqueta}) no puede atender alertas. Debe hacerlo un Operador o un Supervisor.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    evento.requiereAtencion -> {
                        Button(
                            onClick = {
                                guardianVm.atenderEvento(evento, EstadoEvento.CONFIRMADA, "Alerta confirmada y enviada a Central", sesion)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColoresGuardian.Rojo, contentColor = androidx.compose.ui.graphics.Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Confirmar alerta - Enviar a Central")
                        }
                        OutlinedButton(
                            onClick = {
                                guardianVm.atenderEvento(evento, EstadoEvento.FALSA_ALARMA, "Marcada como falsa alarma", sesion)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Falsa alarma")
                        }
                        OutlinedButton(
                            onClick = {
                                guardianVm.atenderEvento(evento, EstadoEvento.RESUELTO, "Marcada como resuelta", sesion)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Marcar como resuelta") }
                    }
                    else -> Text(
                        "Esta alerta ya fue atendida (${evento.estado.etiqueta}).",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (rol.puedeAtenderAlertas) {
                    OutlinedButton(onClick = { pedirCamara() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Reportar incidencia con foto")
                    }
                    OutlinedButton(
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+56200000000")))
                            } catch (e: Exception) {
                                mostrarToast(context, "No se pudo abrir el marcador del teléfono")
                            }
                            guardianVm.registrarAccion(evento, "Llamada a Central iniciada", sesion)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Llamar a Central")
                    }
                }

                // Cuenta regresiva de 2 minutos (solo mientras este pendiente)
                if (evento.requiereAtencion) {
                    val restante = ((evento.fechaMillis + 120_000L - ahora) / 1000L).toInt()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("TIEMPO DE RESPUESTA", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            if (restante > 0) {
                                Text("${formatearSegundos(restante)} restante", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                LinearProgressIndicator(
                                    progress = { (restante / 120f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    "Tiempo excedido: se escala a Central y Jefe de Seguridad (simulado)",
                                    color = ColoresGuardian.Rojo,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                "Protocolo Insercode: confirmar en menos de 2 minutos o se escala automáticamente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Trazabilidad: quién hizo que y cuando
                SeccionTitulo("Historial de acciones")
                if (acciones.isEmpty()) {
                    Text(
                        "Aún no hay acciones registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                acciones.forEach { accion ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(accion.accion, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${accion.usuario} (${enumSeguro(accion.rol, Rol.OPERADOR).etiqueta}) · ${accion.fechaMillis.aFechaHora()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Fila "etiqueta valor" de la tarjeta de detalles
@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp))
    }
}