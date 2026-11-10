package com.example.modoguardian.ui.components

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.annotation.RawRes
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
// Controlador: guarda los casos de uso de CameraX y expone foto, grabacion y linterna
class ControladorCamara {
    var camera: Camera? = null
    var imageCapture: ImageCapture? = null
    var videoCapture: VideoCapture<Recorder>? = null
    private var grabacion: Recording? = null

    val tieneLinterna: Boolean get() = camera?.cameraInfo?.hasFlashUnit() == true

    fun linterna(activa: Boolean) {
        camera?.cameraControl?.enableTorch(activa)
    }

    // Guarda la foto en Galeria
    fun tomarFoto(context: Context, alTerminar: (String) -> Unit) {
        val captura = imageCapture ?: return alTerminar("La cámara aún no está lista")
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "guardian_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ModoGuardian")
        }
        val opciones = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores
        ).build()

        captura.takePicture(
            opciones,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    alTerminar("Foto guardada en Galería > ModoGuardian")
                }

                override fun onError(exception: ImageCaptureException) {
                    alTerminar("No se pudo guardar la foto")
                }
            }
        )
    }

    // Devuelve false si el dispositivo no permite grabar video
    fun iniciarGrabacion(context: Context, alTerminar: (String) -> Unit): Boolean {
        val captura = videoCapture ?: return false
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "guardian_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/ModoGuardian")
        }
        val salida = MediaStoreOutputOptions.Builder(
            context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(valores).build()

        return try {
            grabacion = captura.output.prepareRecording(context, salida)
                .start(ContextCompat.getMainExecutor(context)) { evento ->
                    if (evento is VideoRecordEvent.Finalize) {
                        grabacion = null
                        alTerminar(
                            if (evento.hasError()) "La grabación terminó con un error"
                            else "Video guardado en Galería > ModoGuardian"
                        )
                    }
                }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun detenerGrabacion() {
        grabacion?.stop()
        grabacion = null
    }
}

// Une la vista previa, la foto y el video al ciclo de vida de la pantalla
private fun vincular(
    contexto: Context,
    duenio: LifecycleOwner,
    vista: PreviewView,
    controlador: ControladorCamara
) {
    val futuro = ProcessCameraProvider.getInstance(contexto)
    futuro.addListener({
        try {
            val proveedor = futuro.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(vista.surfaceProvider) }
            val imagen = ImageCapture.Builder().build()
            val grabador = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD, FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)))
                .build()
            val video = VideoCapture.withOutput(grabador)
            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            proveedor.unbindAll()
            try {
                controlador.camera = proveedor.bindToLifecycle(duenio, selector, preview, imagen, video)
                controlador.videoCapture = video
            } catch (e: Exception) {
                proveedor.unbindAll()
                controlador.camera = proveedor.bindToLifecycle(duenio, selector, preview, imagen)
                controlador.videoCapture = null
            }
            controlador.imageCapture = imagen
        } catch (e: Exception) {
        }
    }, ContextCompat.getMainExecutor(contexto))
}

// Vista previa de la camara real del telefono
@Composable
fun VisorCamaraTelefono(controlador: ControladorCamara, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val duenio = contexto as ComponentActivity

    AndroidView(
        factory = { ctx -> PreviewView(ctx).also { vincular(ctx, duenio, it, controlador) } },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            try {
                ProcessCameraProvider.getInstance(contexto).get().unbindAll()
            } catch (e: Exception) {
            }
            controlador.camera = null
            controlador.imageCapture = null
            controlador.videoCapture = null
        }
    }
}

// Cámaras simuladas

// Si existe res/raw/camara_demo.mp4 reproduce el video; si no, muestra la señal animada
@Composable
fun VisorSimulado(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val recurso = remember { context.resources.getIdentifier("camara_demo", "raw", context.packageName) }
    if (recurso != 0) VideoDemo(recurso, modifier) else SenalSimulada(modifier)
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun VideoDemo(@RawRes recurso: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val reproductor = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri("android.resource://${context.packageName}/$recurso"))
            repeatMode = Player.REPEAT_MODE_ONE   // bucle: parece una transmisión continua
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { reproductor.release() } }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = reproductor
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )
}

// Señal de prueba: lineas de barrido que bajan en bucle (no necesita ningún archivo)
@Composable
fun SenalSimulada(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "barrido")
    val progreso by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "progreso"
    )
    Canvas(modifier = modifier.background(Color(0xFF0B1F14))) {
        var y = 0f
        while (y < size.height) {
            drawLine(Color(0x1122C55E), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += 8f
        }
        drawRect(Color(0x3322C55E), topLeft = Offset(0f, size.height * progreso), size = Size(size.width, 24f))
    }
}

// Camara desconectada o en mantencion
@Composable
fun SinSenal(motivo: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF111827)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.VideocamOff, contentDescription = null, tint = Color.Gray)
            Text("Sin señal · $motivo", color = Color.Gray)
        }
    }
}