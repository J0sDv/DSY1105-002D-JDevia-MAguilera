package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.Rol
import com.example.modoguardian.ui.components.Etiqueta
import com.example.modoguardian.ui.components.FilaAjuste
import com.example.modoguardian.ui.components.IndicadorNumero
import com.example.modoguardian.ui.components.SeccionAjustes
import com.example.modoguardian.ui.utils.ColoresGuardian
import com.example.modoguardian.ui.utils.mostrarToast
import com.example.modoguardian.viewmodel.GuardianViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PerfilScreen(
    guardianVm: GuardianViewModel,
    sesion: LoginUiState,
    onVerCamaras: () -> Unit,
    onGestionarUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val camaras by guardianVm.camaras.collectAsState()
    val ind by guardianVm.indicadores.collectAsState()
    val modoOscuro by guardianVm.modoOscuro.collectAsState()
    val notificaciones by guardianVm.notificaciones.collectAsState()
    val cargando by guardianVm.cargando.collectAsState()

    val rol = sesion.rol
    val verificado = FirebaseAuth.getInstance().currentUser?.isEmailVerified == true
    val oscuroActivo = modoOscuro ?: isSystemInDarkTheme()
    val simulado: () -> Unit = { mostrarToast(context, "Función simulada en este MVP") }

    // Ancho maximo para que en tablet el perfil no se estire
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mi perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Datos de la cuenta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            sesion.nombreUsuario.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(sesion.nombreUsuario, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            sesion.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (rol != null) Etiqueta(rol.etiqueta, ColoresGuardian.Azul)
                            if (verificado) Etiqueta("Correo verificado", ColoresGuardian.Verde)
                        }
                    }
                }
            }

            // Resumen del turno
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IndicadorNumero("14:00-22:00", "Turno", MaterialTheme.colorScheme.onSurface)
                    IndicadorNumero("${camaras.size}", "Cámaras", ColoresGuardian.Azul)
                    IndicadorNumero("${ind.atendidas}", "Atendidas", ColoresGuardian.Verde)
                }
            }

            SeccionAjustes("MI CUENTA") {
                FilaAjuste(Icons.Filled.Videocam, "Mis cámaras asignadas", "${camaras.size} cámaras", onClick = onVerCamaras)
                FilaAjuste(Icons.Filled.History, "Historial de turnos", "Turnos anteriores y registros", onClick = simulado)
                FilaAjuste(Icons.Filled.VerifiedUser, "Certificado de guardia", "Documento vigente", onClick = simulado)
            }

            SeccionAjustes("SEGURIDAD") {
                // Real: Firebase envia un correo para elegir una contraseña nueva
                FilaAjuste(
                    Icons.Filled.Key, "Cambiar contraseña", "Te enviamos un correo para cambiarla",
                    onClick = { guardianVm.enviarRestablecimiento(sesion.email) }
                )
                FilaAjuste(Icons.Filled.PhoneAndroid, "Sesiones activas", "Dispositivo actual", trailing = {
                    Etiqueta("1 dispositivo", ColoresGuardian.Azul)
                })
                // Real: apaga o enciende las notificaciones de alertas criticas
                FilaAjuste(Icons.Filled.NotificationsActive, "Notificaciones", "Avisos de alertas críticas", trailing = {
                    Switch(checked = notificaciones, onCheckedChange = { guardianVm.setNotificaciones(it) })
                })
                FilaAjuste(Icons.Filled.Warning, "Alertas críticas", "Siempre visibles dentro de la app", trailing = {
                    Switch(checked = true, onCheckedChange = null, enabled = false)
                })
            }

            SeccionAjustes("APP") {
                // Real: cambia el tema de toda la app y se recuerda
                FilaAjuste(Icons.Filled.DarkMode, "Modo oscuro", "Aplica el tema oscuro", trailing = {
                    Switch(checked = oscuroActivo, onCheckedChange = { guardianVm.setModoOscuro(it) })
                })
                FilaAjuste(Icons.Filled.Info, "Ayuda y soporte", "Guías y contacto", onClick = simulado)
                FilaAjuste(Icons.Filled.Description, "Términos y políticas de seguridad", onClick = simulado)
            }

            // Herramientas segun el rol: Administrador y Supervisor
            if (rol == Rol.ADMIN || rol == Rol.SUPERVISOR) {
                SeccionAjustes(if (rol == Rol.ADMIN) "ADMINISTRACIÓN" else "SUPERVISIÓN") {
                    if (rol == Rol.ADMIN) {
                        FilaAjuste(
                            Icons.Filled.Storage, "Cargar datos de prueba",
                            if (cargando) "Cargando..." else "Crea cámaras y eventos ficticios",
                            onClick = { if (!cargando) guardianVm.cargarDatosPrueba() }
                        )
                        FilaAjuste(
                            Icons.Filled.Group, "Gestionar usuarios y roles", "Cambia el rol de cada cuenta",
                            onClick = onGestionarUsuarios
                        )
                    }
                    FilaAjuste(
                        Icons.Filled.Warning, "Simular alerta crítica", "Genera una alerta y una notificación de prueba",
                        onClick = { guardianVm.simularAlerta() }
                    )
                }
            }

            Button(
                onClick = onCerrarSesion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Cerrar sesión")
            }

            Text(
                "Modo Guardián v1.0 · MVP académico con datos de prueba",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}