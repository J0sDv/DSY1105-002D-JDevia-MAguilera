package com.example.modoguardian.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext

import androidx.core.content.ContextCompat
import com.example.modoguardian.ui.utils.EscucharMensajes
import com.example.modoguardian.ui.utils.obtenerWindowSizeClass
import com.example.modoguardian.viewmodel.GuardianViewModel
import com.example.modoguardian.viewmodel.LoginViewModel

// Las cinco pestañas del mockup
enum class Pestana(val etiqueta: String, val icono: ImageVector) {
    INICIO("Inicio", Icons.Filled.Home),
    CAMARAS("Cámaras", Icons.Filled.Videocam),
    EVENTOS("Eventos", Icons.AutoMirrored.Filled.List),
    ALERTAS("Alertas", Icons.Filled.Notifications),
    PERFIL("Perfil", Icons.Filled.Person)
}

@Composable
fun HomeScreen(
    loginViewModel: LoginViewModel,
    guardianViewModel: GuardianViewModel,
    onAbrirCamara: (String) -> Unit,
    onAbrirAlerta: (String) -> Unit,
    onGestionarUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val sesion by loginViewModel.estado.collectAsState()
    val indicadores by guardianViewModel.indicadores.collectAsState()
    val context = LocalContext.current
    val pestanas = Pestana.values()
    var indice by rememberSaveable { mutableIntStateOf(0) }
    val compacto = obtenerWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact

    // Muestra los mensajes (ok, errores, "se enviara al reconectar") como Toast
    EscucharMensajes(guardianViewModel)

    // Android 13 o superior: hay que pedir el permiso para mostrar notificaciones
    val pedirNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        guardianViewModel.iniciarSincronizacion()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Contenido de la pestaña elegida
    val contenido: @Composable (Modifier) -> Unit = { modificador ->
        Box(modificador.fillMaxSize()) {
            when (pestanas[indice]) {
                Pestana.INICIO -> InicioScreen(
                    guardianVm = guardianViewModel,
                    sesion = sesion,
                    onAbrirCamara = onAbrirCamara,
                    onAbrirAlerta = onAbrirAlerta,
                    onVerCamaras = { indice = Pestana.CAMARAS.ordinal },
                    onVerEventos = { indice = Pestana.EVENTOS.ordinal }
                )
                Pestana.CAMARAS -> CamarasScreen(guardianViewModel, onAbrirCamara)
                Pestana.EVENTOS -> EventosScreen(guardianViewModel, onAbrirAlerta)
                Pestana.ALERTAS -> AlertasScreen(guardianViewModel, onAbrirAlerta)
                Pestana.PERFIL -> PerfilScreen(
                    guardianVm = guardianViewModel,
                    sesion = sesion,
                    onVerCamaras = { indice = Pestana.CAMARAS.ordinal },
                    onGestionarUsuarios = onGestionarUsuarios,
                    onCerrarSesion = onCerrarSesion
                )
            }
        }
    }

    Scaffold(
        bottomBar = {
            // Celular: barra inferior
            if (compacto) {
                NavigationBar {
                    pestanas.forEachIndexed { i, pestana ->
                        NavigationBarItem(
                            selected = indice == i,
                            onClick = { indice = i },
                            icon = { IconoPestana(pestana, if (pestana == Pestana.ALERTAS) indicadores.pendientes else 0) },
                            label = { Text(pestana.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (compacto) {
            contenido(Modifier.padding(innerPadding))
        } else {
            // Tablet o escritorio: barra lateral + contenido
            Row(Modifier.padding(innerPadding).fillMaxSize()) {
                NavigationRail {
                    Spacer(Modifier.weight(1f))
                    pestanas.forEachIndexed { i, pestana ->
                        NavigationRailItem(
                            selected = indice == i,
                            onClick = { indice = i },
                            icon = { IconoPestana(pestana, if (pestana == Pestana.ALERTAS) indicadores.pendientes else 0) },
                            label = { Text(pestana.etiqueta) }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
                contenido(Modifier.weight(1f))
            }
        }
    }
}

// Icono de la pestaña con globo de cantidad (se usa para las alertas pendientes)
@Composable
private fun IconoPestana(pestana: Pestana, pendientes: Int) {
    BadgedBox(badge = { if (pendientes > 0) Badge { Text("$pendientes") } }) {
        Icon(pestana.icono, contentDescription = pestana.etiqueta)
    }
}