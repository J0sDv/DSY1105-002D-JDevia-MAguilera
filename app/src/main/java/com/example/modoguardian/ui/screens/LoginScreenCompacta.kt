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
import com.example.modoguardian.model.LoginErrores
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.ui.theme.ModoGuardianTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenCompacta(
    estado: LoginUiState,
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onRecordarCorreoChange: (Boolean) -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Modo Guardian") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Modo Guardian",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Bienvenido a Modo Guardian",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Ingresa con tu usuario para monitorear eventos y alertas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LoginFormulario(
                estado = estado,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onRecordarCorreoChange = onRecordarCorreoChange,
                onLoginClick = onLoginClick
            )
        }
    }
}

@Preview(name = "Compact", widthDp = 360, heightDp = 800)
@Composable
fun PreviewLoginCompact() {
    ModoGuardianTheme {
        LoginScreenCompacta(estado = LoginUiState())
    }
}

@Preview(name = "Compact con errores", widthDp = 360, heightDp = 800)
@Composable
fun PreviewLoginCompactErrores() {
    ModoGuardianTheme {
        LoginScreenCompacta(
            estado = LoginUiState(
                email = "admin",
                password = "123",
                errores = LoginErrores(email = "Correo invalido", password = "Debe tener al menos 6 caracteres")
            )
        )
    }
}