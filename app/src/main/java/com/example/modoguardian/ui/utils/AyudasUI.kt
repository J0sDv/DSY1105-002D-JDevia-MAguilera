package com.example.modoguardian.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.modoguardian.viewmodel.GuardianViewModel

fun mostrarToast(context: Context, texto: String) {
    Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()
}

// Muestra como Toast los mensajes que emite el GuardianViewModel (ok, errores, "se enviará al reconectar")
@Composable
fun EscucharMensajes(viewModel: GuardianViewModel) {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.mensajes.collect { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }
}

fun tienePermisoCamara(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

// Devuelve una acción que pide el permiso de cámara si falta y luego ejecuta onConcedido
@Composable
fun rememberSolicitarCamara(onConcedido: () -> Unit, onDenegado: () -> Unit = {}): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) onConcedido() else onDenegado()
    }
    return {
        if (tienePermisoCamara(context)) onConcedido() else launcher.launch(Manifest.permission.CAMERA)
    }
}