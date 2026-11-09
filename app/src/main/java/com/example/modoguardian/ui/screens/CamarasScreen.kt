package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.ui.components.TarjetaCamara
import com.example.modoguardian.viewmodel.GuardianViewModel

private enum class FiltroCamara(val etiqueta: String) {
    TODAS("Todas"),
    EN_LINEA("En línea"),
    SIN_CONEXION("Sin conexión")
}

@Composable
fun CamarasScreen(guardianVm: GuardianViewModel, onAbrirCamara: (String) -> Unit) {
    val camaras by guardianVm.camaras.collectAsState()
    val ind by guardianVm.indicadores.collectAsState()
    var filtro by rememberSaveable { mutableStateOf(FiltroCamara.TODAS) }

    val lista = when (filtro) {
        FiltroCamara.TODAS -> camaras
        FiltroCamara.EN_LINEA -> camaras.filter { it.enLinea }
        FiltroCamara.SIN_CONEXION -> camaras.filter { !it.enLinea }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 170.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Encabezado que ocupa todo el ancho de la grilla
        item(span = { GridItemSpan(maxLineSpan) }) {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cámaras", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "${ind.activas} de ${ind.totalCamaras} en línea",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroCamara.values().forEach { f ->
                        FilterChip(
                            selected = filtro == f,
                            onClick = { filtro = f },
                            label = { Text(f.etiqueta) }
                        )
                    }
                }
            }
        }

        if (lista.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "No hay cámaras para mostrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(lista, key = { it.id }) { camara ->
            TarjetaCamara(camara = camara, onClick = { onAbrirCamara(camara.id) })
        }
    }
}