package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.Rol
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel

@Composable
fun HomeScreen(
    viewModel: LoginViewModel,
    onCerrarSesion: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    HomeContenido(estado = estado, onCerrarSesion = onCerrarSesion)
}

// Contenido sin ViewModel: permite hacer Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContenido(
    estado: LoginUiState,
    onCerrarSesion: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pantalla Home") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val rol = estado.rol

            if (rol == null) {
                Text(
                    text = "Sesión no válida",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge
                )
                Text("Vuelve a iniciar sesión para continuar.")
            } else {
                Text(
                    text = "¡Bienvenido, ${estado.nombreUsuario}!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(text = "Rol: ${rol.etiqueta}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Correo: ${estado.email}")

                when (rol) {
                    Rol.ADMIN -> TarjetaFunciones(
                        titulo = "Panel de Administrador",
                        funciones = listOf(
                            "Gestionar usuarios de prueba",
                            "Administrar roles y permisos",
                            "Configurar parámetros funcionales del MVP"
                        )
                    )
                    Rol.SUPERVISOR -> TarjetaFunciones(
                        titulo = "Panel de Supervisor",
                        funciones = listOf(
                            "Revisar estados operacionales",
                            "Revisar eventos y alertas",
                            "Ver indicadores básicos y dar seguimiento a la atención"
                        )
                    )
                    Rol.OPERADOR -> TarjetaFunciones(
                        titulo = "Panel de Operador / Monitor",
                        funciones = listOf(
                            "Consultar cámaras o dispositivos simulados",
                            "Revisar eventos y alertas",
                            "Registrar acciones de atención y actualizar estados"
                        )
                    )
                }
            }

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (rol == null) "Ir al inicio de sesión" else "Cerrar sesión")
            }
        }
    }
}

@Composable
private fun TarjetaFunciones(titulo: String, funciones: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
            funciones.forEach { funcion ->
                Text(
                    text = "• $funcion",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(name = "Home Supervisor", widthDp = 360, heightDp = 800)
@Composable
fun PreviewHome() {
    ModoGuardianTheme {
        HomeContenido(
            estado = LoginUiState(
                email = "supervisor@guardian.test",
                rol = Rol.SUPERVISOR,
                nombreUsuario = "Supervisor Guardián"
            )
        )
    }
}