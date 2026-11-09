package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.viewmodel.GuardianViewModel

// PROVISORIO (se reemplaza en la Parte 3)
@Composable
fun CamaraDetalleScreen(camaraId: String, guardianVm: GuardianViewModel, onVolver: () -> Unit) {
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
        Text("Detalle de cámara $camaraId (provisorio)")
        Button(onClick = onVolver) { Text("Volver") }
    }
}