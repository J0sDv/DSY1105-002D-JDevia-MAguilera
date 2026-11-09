package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.viewmodel.GuardianViewModel

// PROVISORIO (se reemplaza en la Parte 4)
@Composable
fun AlertasScreen(guardianVm: GuardianViewModel, onAbrirAlerta: (String) -> Unit) {
    Box(Modifier.fillMaxSize().padding(16.dp)) { Text("Alertas (provisorio)") }
}