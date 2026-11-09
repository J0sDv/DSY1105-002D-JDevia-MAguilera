package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.Rol
import com.example.modoguardian.viewmodel.GuardianViewModel

// PROVISORIO (se reemplaza en la Parte 5)
@Composable
fun PerfilScreen(
    guardianVm: GuardianViewModel,
    sesion: LoginUiState,
    onVerCamaras: () -> Unit,
    onGestionarUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val camaras by guardianVm.camaras.collectAsState()
    val eventos by guardianVm.eventos.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Perfil (provisorio)")
        Text("Rol: ${sesion.rol?.etiqueta} · Cámaras: ${camaras.size} · Eventos: ${eventos.size}")
        if (sesion.rol == Rol.ADMIN) {
            Button(onClick = { guardianVm.cargarDatosPrueba() }) { Text("Cargar datos de prueba") }
            Button(onClick = { guardianVm.simularAlerta() }) { Text("Simular alerta crítica") }
        }
        Button(onClick = onCerrarSesion) { Text("Cerrar sesión") }
    }
}