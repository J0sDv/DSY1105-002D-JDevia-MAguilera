package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.PerfilUsuario
import com.example.modoguardian.model.Rol
import com.example.modoguardian.ui.utils.EscucharMensajes
import com.example.modoguardian.viewmodel.GuardianViewModel

@Composable
fun UsuariosAdminScreen(guardianVm: GuardianViewModel, onVolver: () -> Unit) {
    val usuarios by guardianVm.usuarios.collectAsState()
    EscucharMensajes(guardianVm)

    // Al abrir la pantalla se lee la lista de usuarios desde Firestore
    LaunchedEffect(Unit) { guardianVm.cargarUsuarios() }

    Scaffold { inner ->
        Column(modifier = Modifier.padding(inner).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Gestión de usuarios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                "Cambia el rol de cada cuenta. La persona debe volver a iniciar sesión para ver el cambio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (usuarios.isEmpty()) {
                    item { Text("No hay usuarios para mostrar.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                items(usuarios, key = { it.uid }) { usuario ->
                    TarjetaUsuario(usuario = usuario, onCambiarRol = { rol -> guardianVm.cambiarRol(usuario.uid, rol) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaUsuario(usuario: PerfilUsuario, onCambiarRol: (Rol) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.username, fontWeight = FontWeight.SemiBold)
                Text(
                    usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                OutlinedButton(onClick = { menu = true }) { Text(usuario.rol.etiqueta) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    Rol.values().forEach { rol ->
                        DropdownMenuItem(
                            text = { Text(rol.etiqueta) },
                            onClick = {
                                menu = false
                                if (rol != usuario.rol) onCambiarRol(rol)
                            }
                        )
                    }
                }
            }
        }
    }
}