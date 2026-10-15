package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.ui.utils.obtenerWindowSizeClass
import com.example.modoguardian.viewmodel.LoginViewModel

// Archivo principal, identifica el tamaño de pantalla y elige el layout
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val windowSizeClass = obtenerWindowSizeClass()

    LaunchedEffect(estado.rol) {
        if (estado.rol != null) onLoginExitoso()
    }

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> LoginScreenCompacta(
            estado = estado,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRecordarCorreoChange = viewModel::onRecordarCorreoChange,
            onLoginClick = viewModel::onLoginClick
        )
        WindowWidthSizeClass.Medium -> LoginScreenMediana(
            estado = estado,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRecordarCorreoChange = viewModel::onRecordarCorreoChange,
            onLoginClick = viewModel::onLoginClick
        )
        WindowWidthSizeClass.Expanded -> LoginScreenExpandida(
            estado = estado,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRecordarCorreoChange = viewModel::onRecordarCorreoChange,
            onLoginClick = viewModel::onLoginClick
        )
    }
}

// Formulario compartido por los tres layouts
@Composable
fun LoginFormulario(
    estado: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRecordarCorreoChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = estado.email,
            onValueChange = onEmailChange,
            label = { Text("Correo electrónico") },
            singleLine = true,
            enabled = !estado.isLoading,
            isError = estado.errores.email != null,
            supportingText = {
                estado.errores.email?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !estado.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            isError = estado.errores.password != null,
            supportingText = {
                estado.errores.password?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = estado.recordarCorreo,
                onCheckedChange = onRecordarCorreoChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recordar mi correo")
        }

        estado.errores.general?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ingresando...")
            } else {
                Text("Iniciar sesión")
            }
        }
    }
}