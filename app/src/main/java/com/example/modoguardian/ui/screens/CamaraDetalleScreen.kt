package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.ui.components.ControladorCamara
import com.example.modoguardian.ui.components.IndicadorNumero
import com.example.modoguardian.ui.components.SinSenal
import com.example.modoguardian.ui.components.VisorCamaraTelefono
import com.example.modoguardian.ui.components.VisorSimulado
import com.example.modoguardian.ui.components.Etiqueta
import com.example.modoguardian.ui.utils.ColoresGuardian
import com.example.modoguardian.ui.utils.EscucharMensajes
import com.example.modoguardian.ui.utils.aFechaHora
import com.example.modoguardian.ui.utils.color
import com.example.modoguardian.ui.utils.formatearSegundos
import com.example.modoguardian.ui.utils.mostrarToast
import com.example.modoguardian.ui.utils.rememberSolicitarCamara
import com.example.modoguardian.ui.utils.tienePermisoCamara
import com.example.modoguardian.viewmodel.GuardianViewModel
import kotlinx.coroutines.delay

@Composable
fun CamaraDetalleScreen(camaraId: String, guardianVm: GuardianViewModel, onVolver: () -> Unit) {
    val context = LocalContext.current
    val camaras by guardianVm.camaras.collectAsState()
    val camara = camaras.firstOrNull { it.id == camaraId }
    EscucharMensajes(guardianVm)

    val controlador = remember { ControladorCamara() }
    var permiso by remember { mutableStateOf(tienePermisoCamara(context)) }
    val pedirPermiso = rememberSolicitarCamara(
        onConcedido = { permiso = true },
        onDenegado = { mostrarToast(context, "Sin permiso de cámara no se puede mostrar la señal") }
    )
    var grabando by remember { mutableStateOf(false) }
    var segundos by remember { mutableIntStateOf(0) }
    var linterna by remember { mutableStateOf(false) }
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Reloj del visor y cronometro de grabacion
    LaunchedEffect(Unit) { while (true) { ahora = System.currentTimeMillis(); delay(1000) } }
    LaunchedEffect(grabando) { segundos = 0; while (grabando) { delay(1000); segundos++ } }
    // Al abrir CAM-01 se pide el permiso de camara si falta
    LaunchedEffect(camara?.esTelefono) { if (camara?.esTelefono == true && !permiso) pedirPermiso() }
    // Al salir de la pantalla se corta cualquier grabacion en curso
    DisposableEffect(Unit) { onDispose { controlador.detenerGrabacion() } }

    // Solo CAM-01 (la del telefono) tiene foto, video y linterna reales
    fun esReal(): Boolean = camara != null && camara.esTelefono && camara.enLinea && permiso

    fun hacerFoto() {
        if (esReal()) controlador.tomarFoto(context) { mostrarToast(context, it) }
        else mostrarToast(context, "Captura simulada: solo la cámara del teléfono toma fotos reales")
    }

    fun alternarGrabacion() {
        val c = camara ?: return
        when {
            !c.enLinea -> mostrarToast(context, "La cámara no tiene señal")
            esReal() -> {
                if (grabando) {
                    controlador.detenerGrabacion() // el aviso llega cuando el archivo queda guardado
                } else {
                    val iniciado = controlador.iniciarGrabacion(context) { mensaje ->
                        grabando = false
                        mostrarToast(context, mensaje)
                    }
                    if (iniciado) grabando = true
                    else mostrarToast(context, "La grabación no está disponible en este dispositivo")
                }
            }
            else -> {
                grabando = !grabando  // grabacion simulada (solo cronometro)
                if (grabando) mostrarToast(context, "Grabación simulada")
            }
        }
    }

    fun alternarLinterna() {
        if (esReal() && controlador.tieneLinterna) {
            linterna = !linterna
            controlador.linterna(linterna)
        } else {
            mostrarToast(context, "Linterna no disponible en esta cámara o dispositivo")
        }
    }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Barra superior propia: volver, nombre y estado
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        camara?.nombre ?: "Cámara",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (camara != null) {
                        Text(
                            if (camara.enLinea) "EN VIVO • ${if (camara.hd) "HD" else "SD"}" else camara.estado.etiqueta.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = camara.estado.color()
                        )
                    }
                }
                if (grabando) Etiqueta("● REC", ColoresGuardian.Rojo)
            }

            if (camara == null) {
                Text("Cargando cámara…", modifier = Modifier.padding(16.dp))
            } else {
                // Visor: camara real, video de prueba o sin señal, con texto superpuesto
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(Color.Black)) {
                    when {
                        !camara.enLinea -> SinSenal(camara.estado.etiqueta)
                        camara.esTelefono -> {
                            if (permiso) {
                                VisorCamaraTelefono(controlador, Modifier.fillMaxSize())
                            } else {
                                Button(onClick = pedirPermiso, modifier = Modifier.align(Alignment.Center)) {
                                    Text("Permitir cámara")
                                }
                            }
                        }
                        else -> VisorSimulado(Modifier.fillMaxSize())
                    }
                    if (camara.enLinea) {
                        Column(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                            Text(
                                ahora.aFechaHora(),
                                color = ColoresGuardian.Verde,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "INSERCODE ${camara.id.uppercase()}",
                                color = ColoresGuardian.Verde,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    if (grabando) {
                        Text(
                            "REC ${formatearSegundos(segundos)}",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                        )
                    }
                }

                // Controles: FOTO, REC, HABLAR, LUZ y FULL
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BotonControl("FOTO", Icons.Filled.CameraAlt, onClick = { hacerFoto() })
                    BotonControl(
                        etiqueta = if (grabando) "PARAR" else "REC",
                        icono = if (grabando) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                        destacado = true,
                        onClick = { alternarGrabacion() }
                    )
                    BotonControl("HABLAR", Icons.Filled.Mic, onClick = {
                        mostrarToast(context, "Intercomunicador simulado en este MVP")
                    })
                    BotonControl(
                        "LUZ",
                        if (linterna) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        onClick = { alternarLinterna() }
                    )
                    BotonControl("FULL", Icons.Filled.Fullscreen, onClick = {
                        mostrarToast(context, "Pantalla completa simulada en este MVP")
                    })
                }

                // Estado del dispositivo
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("ESTADO DEL DISPOSITIVO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            IndicadorNumero(camara.estado.etiqueta, "Estado", camara.estado.color())
                            IndicadorNumero(if (camara.enLinea) "Excelente" else "Sin señal", "Señal", MaterialTheme.colorScheme.onSurface)
                            IndicadorNumero(if (camara.esTelefono) "Local" else "Simulada", "Grabación", ColoresGuardian.Azul)
                        }
                        Text(
                            if (camara.esTelefono) "Usa la cámara real de este teléfono. Fotos y videos se guardan en la Galería."
                            else "Cámara simulada con datos de prueba.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonControl(
    etiqueta: String,
    icono: ImageVector,
    destacado: Boolean = false,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp),
            colors = if (destacado) {
                IconButtonDefaults.filledIconButtonColors(containerColor = ColoresGuardian.Rojo, contentColor = Color.White)
            } else {
                IconButtonDefaults.filledTonalIconButtonColors()
            }
        ) {
            Icon(icono, contentDescription = etiqueta)
        }
        Text(etiqueta, style = MaterialTheme.typography.labelSmall)
    }
}