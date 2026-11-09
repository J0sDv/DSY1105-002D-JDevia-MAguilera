package com.example.modoguardian.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.modoguardian.MainActivity
import com.example.modoguardian.model.Evento

// Notificaciones de prueba: las crea la propia app (no usan servidor)
object Notificador {

    private const val CANAL_ID = "alertas_criticas"

    fun mostrarAlerta(context: Context, evento: Evento) {
        try {
            // Desde Android 13 hay que tener el permiso de notificaciones
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CANAL_ID, "Alertas críticas", NotificationManager.IMPORTANCE_HIGH)
            )

            // Al tocar la notificacion se abre la app
            val intento = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendiente = PendingIntent.getActivity(
                context, 0, intento, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificacion = NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Alerta crítica: ${evento.titulo}")
                .setContentText("${evento.camaraNombre} · ${evento.zona}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendiente)
                .build()

            manager.notify(evento.id.hashCode(), notificacion)
        } catch (e: Exception) {
            // Una notificación fallida nunca debe detener la app
        }
    }
}