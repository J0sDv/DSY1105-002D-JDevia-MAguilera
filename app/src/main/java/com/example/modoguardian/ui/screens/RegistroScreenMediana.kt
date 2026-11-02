package com.example.modoguardian.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modoguardian.R
import com.example.modoguardian.model.RegistroUiState
import com.example.modoguardian.ui.theme.ModoGuardianTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreenMediana(
    estado: RegistroUiState,
    acciones: RegistroAcciones = RegistroAcciones()
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Modo Guardián") })
        }
    ) { innerPadding ->
        // Contenedor que centra la tarjeta en pantalla
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card centrada con ancho maximo para que el formulario no se estire
            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo Modo Guardián",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "Crear cuenta",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    RegistroFormulario(estado = estado, acciones = acciones)
                }
            }
        }
    }
}

@Preview(name = "Registro Medium", widthDp = 700, heightDp = 1100)
@Composable
fun PreviewRegistroMedium() {
    ModoGuardianTheme {
        RegistroScreenMediana(estado = RegistroUiState())
    }
}