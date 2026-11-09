package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.viewmodel.GuardianViewModel

// PROVISORIO (se reemplaza en la Parte 2): muestra cuántos datos hay en Room
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
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Inicio (provisorio)")
        Text("Cámaras: ${camaras.size} · Eventos: ${eventos.size}")
    }
}