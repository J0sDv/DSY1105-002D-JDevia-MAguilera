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
import com.example.modoguardian.ui.utils.rememberGoogleSignIn
import com.example.modoguardian.viewmodel.LoginViewModel

// Archivo principal: identifica el tamaño de pantalla y elige el layout
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    // Observa el estado del ViewModel (se recompone cuando cambia)
    val estado by viewModel.estado.collectAsState()
    val windowSizeClass = obtenerWindowSizeClass()

    // Cuando el login es correcto el ViewModel guarda el rol; aqui se avisa para navegar
    LaunchedEffect(estado.rol) {
        if (estado.rol != null) onLoginExitoso()
    }

    // Boton de google: obtiene el token y se lo entrega al ViewModel
    val onGoogleClick = rememberGoogleSignIn(
        onToken = viewModel::onGoogleToken,
        onError = viewModel::onGoogleError
    )

    // Compact = celular (Column) | Medium = Card centrada | Expanded = Row
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> LoginScreenCompacta(
            estado = estado,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRecordarCorreoChange = viewModel::onRecordarCorreoChange,
            onLoginClick = viewModel::onLoginClick,
            onGoogleClick = onGoogleClick,
            onIrARegistro = onIrARegistro
        )
        WindowWidthSizeClass.Medium -> LoginScreenMediana(
            estado = estado,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRecordarCorreoChange = viewModel::onRecordarCorreoChange,
            onLoginClick = viewModel::onLoginClick,
            onGoogleClick = onGoogleClick,
            onIrARegistro = onIrARegistro
        )
        WindowWidthSizeClass.Expanded -> LoginScreenExpandida(
            estado = estado,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onRecordarCorreoChange = viewModel::onRecordarCorreoChange,
            onLoginClick = viewModel::onLoginClick,
            onGoogleClick = onGoogleClick,
            onIrARegistro = onIrARegistro
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
    onGoogleClick: () -> Unit = {},
    onIrARegistro: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Inicio de sesion con Google
        OutlinedButton(
            onClick = onGoogleClick,
            enabled = !estado.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Google")
        }

        SeparadorO()

        // Campo correo
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

        // Campo contraseña (texto oculto con PasswordVisualTransformation)
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

        // Checkbox: recordar correo (persistencia local limitada)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = estado.recordarCorreo,
                onCheckedChange = onRecordarCorreoChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recordar mi correo")
        }

        // Error general: credenciales incorrectas, correo sin verificar, sin conexion o google
        estado.errores.general?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Boton siempre disponible: valida al presionarlo y muestra carga mientras consulta
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

        // Enlace al registro de usuario
        TextButton(
            onClick = onIrARegistro,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿No tienes cuenta? Crear cuenta")
        }
    }
}