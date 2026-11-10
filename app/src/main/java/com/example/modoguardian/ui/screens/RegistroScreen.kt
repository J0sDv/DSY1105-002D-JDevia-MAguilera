package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.RegistroUiState
import com.example.modoguardian.model.listaPaises
import com.example.modoguardian.ui.utils.obtenerWindowSizeClass
import com.example.modoguardian.ui.utils.rememberGoogleSignIn
import com.example.modoguardian.viewmodel.RegistroViewModel

// Agrupa las acciones del formulario para pasarlas facilmente a cada diseño
data class RegistroAcciones(
    val onEmailChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onConfirmarPasswordChange: (String) -> Unit = {},
    val onUsernameChange: (String) -> Unit = {},
    val onRegionChange: (String) -> Unit = {},
    val onRegistrarClick: () -> Unit = {},
    val onGoogleClick: () -> Unit = {},
    val onVolver: () -> Unit = {}
)

// Archivo principal: identifica el tamaño de pantalla y elige el diseño
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onGoogleToken: (String) -> Unit
) {
    // Observa el estado del ViewModel (se recompone cuando cambia)
    val estado by viewModel.estado.collectAsState()
    val windowSizeClass = obtenerWindowSizeClass()

    // Boton de Google: obtiene el token y se lo entrega a quien maneja el inicio de sesion
    val onGoogleClick = rememberGoogleSignIn(
        onToken = onGoogleToken,
        onError = viewModel::onGoogleError
    )

    val acciones = RegistroAcciones(
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmarPasswordChange = viewModel::onConfirmarPasswordChange,
        onUsernameChange = viewModel::onUsernameChange,
        onRegionChange = viewModel::onRegionChange,
        onRegistrarClick = viewModel::onRegistrarClick,
        onGoogleClick = onGoogleClick,
        onVolver = onVolver
    )

    // Elige el diseño según el ancho de la pantalla: celular, tablet pequeña o tablet grande
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> RegistroScreenCompacta(estado = estado, acciones = acciones)
        WindowWidthSizeClass.Medium -> RegistroScreenMediana(estado = estado, acciones = acciones)
        WindowWidthSizeClass.Expanded -> RegistroScreenExpandida(estado = estado, acciones = acciones)
    }

    // Aviso de cuenta creada: indica revisar el correo y vuelve al login
    estado.mensajeCuentaCreada?.let { mensaje ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Revisa tu correo") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = onVolver) { Text("Ir a iniciar sesión") }
            }
        )
    }
}

// Linea con una "o" al centro: separa el boton de google del formulario con correo
@Composable
fun SeparadorO() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Text(
            text = "o",
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

// Formulario compartido por los tres diseños
@Composable
fun RegistroFormulario(
    estado: RegistroUiState,
    acciones: RegistroAcciones,
    modifier: Modifier = Modifier
) {
    // Controla si el menu de paises esta abierto
    var menuExpandido by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Registro con google (crea la cuenta o entra si ya existe)
        OutlinedButton(
            onClick = acciones.onGoogleClick,
            enabled = !estado.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Google")
        }

        SeparadorO()

        // Campo correo
        OutlinedTextField(
            value = estado.email,
            onValueChange = acciones.onEmailChange,
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

        // Campo contraseña (con texto de ayuda)
        OutlinedTextField(
            value = estado.password,
            onValueChange = acciones.onPasswordChange,
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !estado.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            isError = estado.errores.password != null,
            supportingText = {
                val error = estado.errores.password
                Text(
                    text = error ?: "Mínimo 8 caracteres, con una letra minúscula y un número",
                    color = if (error != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo confirmar contraseña
        OutlinedTextField(
            value = estado.confirmarPassword,
            onValueChange = acciones.onConfirmarPasswordChange,
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            enabled = !estado.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            isError = estado.errores.confirmarPassword != null,
            supportingText = {
                estado.errores.confirmarPassword?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo nombre de usuario (con texto de ayuda)
        OutlinedTextField(
            value = estado.username,
            onValueChange = acciones.onUsernameChange,
            label = { Text("Nombre de usuario") },
            singleLine = true,
            enabled = !estado.isLoading,
            isError = estado.errores.username != null,
            supportingText = {
                val error = estado.errores.username
                Text(
                    text = error ?: "3 a 20 caracteres: letras, números y guiones simples",
                    color = if (error != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Menu desplegable Pais / Region
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = estado.region,
                onValueChange = {},
                readOnly = true,
                label = { Text("País / Región") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Abrir lista"
                    )
                },
                isError = estado.errores.region != null,
                supportingText = {
                    estado.errores.region?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            // Capa transparente: al tocarla se abre el menu
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = !estado.isLoading) { menuExpandido = true }
            )
            DropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = { menuExpandido = false }
            ) {
                listaPaises.forEach { pais ->
                    DropdownMenuItem(
                        text = { Text(pais) },
                        onClick = {
                            acciones.onRegionChange(pais)
                            menuExpandido = false
                        }
                    )
                }
            }
        }

        // Error general: sin conexion, Google u otro error inesperado
        estado.errores.general?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Boton crear cuenta: muestra carga mientras registra
        Button(
            onClick = acciones.onRegistrarClick,
            enabled = !estado.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creando cuenta...")
            } else {
                Text("Crear cuenta")
            }
        }

        // Enlace para volver al login
        TextButton(
            onClick = acciones.onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}