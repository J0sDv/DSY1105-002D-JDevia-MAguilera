package com.example.modoguardian.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.ui.theme.ModoGuardianTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenExpandida(
    estado: LoginUiState,
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onRecordarCorreoChange: (Boolean) -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Modo Guardián") })
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Modo Guardián",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Modo Guardián",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Monitoreo móvil de eventos y alertas",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Iniciar sesión",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                LoginFormulario(
                    estado = estado,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onRecordarCorreoChange = onRecordarCorreoChange,
                    onLoginClick = onLoginClick,
                    modifier = Modifier.widthIn(max = 420.dp)
                )
            }
        }
    }
}

@Preview(name = "Expanded", widthDp = 1000, heightDp = 700)
@Composable
fun PreviewLoginExpanded() {
    ModoGuardianTheme {
        LoginScreenExpandida(estado = LoginUiState())
    }
}