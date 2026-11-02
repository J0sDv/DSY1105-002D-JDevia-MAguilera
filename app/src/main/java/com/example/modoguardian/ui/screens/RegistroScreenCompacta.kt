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
import com.example.modoguardian.model.RegistroErrores
import com.example.modoguardian.model.RegistroUiState
import com.example.modoguardian.ui.theme.ModoGuardianTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreenCompacta(
    estado: RegistroUiState,
    acciones: RegistroAcciones = RegistroAcciones()
) {
    Scaffold(
        // Barra superior (igual que el login)
        topBar = {
            TopAppBar(title = { Text("Modo Guardián") })
        }
    ) { innerPadding ->
        // Layout vertical: logo, titulos y formulario uno bajo el otro
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo desde res/drawable
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Modo Guardián",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )

            // Título con color y tipografia del tema
            Text(
                text = "Crea tu cuenta en Modo Guardián",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Regístrate con Google o con tu correo. Te enviaremos un enlace para verificarlo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Formulario compartido
            RegistroFormulario(estado = estado, acciones = acciones)
        }
    }
}

// Previews para ver el diseño sin ejecutar la app
@Preview(name = "Registro Compact", widthDp = 360, heightDp = 900)
@Composable
fun PreviewRegistroCompact() {
    ModoGuardianTheme {
        RegistroScreenCompacta(estado = RegistroUiState())
    }
}

@Preview(name = "Registro Compact con errores", widthDp = 360, heightDp = 900)
@Composable
fun PreviewRegistroCompactErrores() {
    ModoGuardianTheme {
        RegistroScreenCompacta(
            estado = RegistroUiState(
                email = "juan",
                password = "123",
                confirmarPassword = "12",
                username = "-juan",
                errores = RegistroErrores(
                    email = "Correo inválido",
                    password = "Mínimo 8 caracteres, con una letra minúscula y un número",
                    confirmarPassword = "Las contraseñas no coinciden",
                    username = "3 a 20 caracteres: letras, números y guiones simples (no al inicio ni al final)"
                )
            )
        )
    }
}