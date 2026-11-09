package com.example.modoguardian.repository

import android.content.Context
import com.example.modoguardian.local.AppDatabase
import com.example.modoguardian.local.aEntidad
import com.example.modoguardian.local.aModelo
import com.example.modoguardian.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

class GuardianRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dao = AppDatabase.obtener(context).dao()

    // Las pantallas leen desde Room (funciona sin conexión)
    val camaras: Flow<List<Camara>> = dao.camaras().map { lista -> lista.map { it.aModelo() } }
    val eventos: Flow<List<Evento>> = dao.eventos().map { lista -> lista.map { it.aModelo() } }

    private var regCamaras: ListenerRegistration? = null
    private var regEventos: ListenerRegistration? = null

    // Sincronizacion: Firestore
    fun iniciarSincronizacion(
        scope: CoroutineScope,
        onAlertaNueva: (Evento) -> Unit,
        onError: (String) -> Unit
    ) {
        detenerSincronizacion()
        var primeraCarga = true // la primera lectura no dispara notificaciones

        regCamaras = db.collection("camaras").addSnapshotListener { snap, error ->
            if (error != null || snap == null) {
                onError("No se pudieron leer las cámaras. Se muestran los últimos datos guardados")
                return@addSnapshotListener
            }
            // Un resultado vacío que viene solo de la caché no debe borrar la copia local
            if (snap.isEmpty && snap.metadata.isFromCache) return@addSnapshotListener
            val lista = snap.documents.mapNotNull { it.aCamara() }.map { it.aEntidad() }
            scope.launch { try { dao.reemplazarCamaras(lista) } catch (e: Exception) { /* la app no se cae */ } }
        }

        regEventos = db.collection("eventos").addSnapshotListener { snap, error ->
            if (error != null || snap == null) {
                onError("No se pudieron leer los eventos. Se muestran los últimos datos guardados")
                return@addSnapshotListener
            }
            if (snap.isEmpty && snap.metadata.isFromCache) return@addSnapshotListener

            // Alertas criticas nuevas (después de la primera carga) -> notificación local
            if (!primeraCarga) {
                snap.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .mapNotNull { it.document.aEvento() }
                    .filter { it.prioridad == Prioridad.CRITICA && it.estado == EstadoEvento.PENDIENTE }
                    .forEach(onAlertaNueva)
            }
            primeraCarga = false

            val lista = snap.documents.mapNotNull { it.aEvento() }.map { it.aEntidad() }
            scope.launch { try { dao.reemplazarEventos(lista) } catch (e: Exception) { /* la app no se cae */ } }
        }
    }

    fun detenerSincronizacion() {
        regCamaras?.remove()
        regEventos?.remove()
        regCamaras = null
        regEventos = null
    }

    // Escrituras. Sin conexión, Firestore guarda el cambio y lo envía al reconectar:
    // por eso se espera maximo 4 segundos y se informa si quedo pendiente
    suspend fun cargarDatosPrueba(): Boolean {
        val lote = db.batch()
        DatosPrueba.camaras().forEach { lote.set(db.collection("camaras").document(it.id), it.aMapa()) }
        DatosPrueba.eventos(System.currentTimeMillis()).forEach {
            lote.set(db.collection("eventos").document(it.id), it.aMapa())
        }
        return confirmar(lote.commit())
    }

    suspend fun crearEvento(evento: Evento): Boolean =
        confirmar(db.collection("eventos").document(evento.id).set(evento.aMapa()))

    // Cambia el estado del evento (opcional) y registra la acción en la misma operación
    suspend fun atenderEvento(
        evento: Evento,
        nuevoEstado: EstadoEvento?,
        textoAccion: String,
        usuario: String,
        rol: Rol
    ): Boolean {
        val ahora = Timestamp.now()
        val lote = db.batch()
        if (nuevoEstado != null) {
            lote.update(
                db.collection("eventos").document(evento.id),
                mapOf("estado" to nuevoEstado.name, "atendidoMillis" to ahora.toDate().time)
            )
        }
        lote.set(
            db.collection("acciones").document(),
            mapOf(
                "eventoId" to evento.id,
                "accion" to textoAccion,
                "usuario" to usuario,
                "rol" to rol.name,
                "uid" to (auth.currentUser?.uid ?: ""),
                "fecha" to ahora
            )
        )
        return confirmar(lote.commit())
    }

    // Historial de acciones de un evento (trazabilidad)
    fun acciones(eventoId: String): Flow<List<AccionRegistro>> = callbackFlow {
        val registro = db.collection("acciones").whereEqualTo("eventoId", eventoId)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                trySend(snap.documents.mapNotNull { it.aAccion() }.sortedByDescending { it.fechaMillis })
            }
        awaitClose { registro.remove() }
    }

    // Administración de usuarios (las reglas solo lo permiten al ADMIN)
    suspend fun listarUsuarios(): List<PerfilUsuario> =
        db.collection("users").get().await().documents.map { d ->
            PerfilUsuario(
                uid = d.id,
                username = d.getString("username") ?: d.getString("email") ?: "",
                email = d.getString("email") ?: "",
                rol = enumSeguro(d.getString("rol"), Rol.OPERADOR)
            )
        }.sortedBy { it.username.lowercase() }

    suspend fun cambiarRol(uid: String, rol: Rol) {
        db.collection("users").document(uid).update("rol", rol.name).await()
    }

    suspend fun enviarRestablecimiento(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    // Funciones auxiliares
    private suspend fun confirmar(tarea: Task<Void>): Boolean =
        withTimeoutOrNull(4000) { tarea.await(); true } ?: false

    private fun Camara.aMapa(): Map<String, Any> = mapOf(
        "nombre" to nombre, "zona" to zona, "estado" to estado.name,
        "hd" to hd, "esTelefono" to esTelefono
    )

    private fun Evento.aMapa(): Map<String, Any> = mapOf(
        "titulo" to titulo, "detalle" to detalle,
        "camaraId" to camaraId, "camaraNombre" to camaraNombre, "zona" to zona,
        "tipo" to tipo.name, "prioridad" to prioridad.name, "estado" to estado.name,
        "fecha" to Timestamp(Date(fechaMillis)),
        "confianzaIA" to confianzaIA, "duracionSeg" to duracionSeg,
        "atendidoMillis" to atendidoMillis
    )

    // Los datos de la nube se leen con cuidado: un documento dañado se ignora (devuelve null)
    private fun DocumentSnapshot.aCamara(): Camara? {
        return try {
            Camara(
                id = id,
                nombre = getString("nombre") ?: return null,
                zona = getString("zona") ?: "",
                estado = enumSeguro(getString("estado"), EstadoCamara.OFFLINE),
                hd = getBoolean("hd") ?: true,
                esTelefono = getBoolean("esTelefono") ?: false
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.aEvento(): Evento? {
        return try {
            Evento(
                id = id,
                titulo = getString("titulo") ?: return null,
                detalle = getString("detalle") ?: "",
                camaraId = getString("camaraId") ?: "",
                camaraNombre = getString("camaraNombre") ?: "",
                zona = getString("zona") ?: "",
                tipo = enumSeguro(getString("tipo"), TipoEvento.INCIDENCIA),
                prioridad = enumSeguro(getString("prioridad"), Prioridad.EVENTO),
                estado = enumSeguro(getString("estado"), EstadoEvento.PENDIENTE),
                fechaMillis = getTimestamp("fecha")?.toDate()?.time ?: System.currentTimeMillis(),
                confianzaIA = getLong("confianzaIA")?.toInt() ?: 0,
                duracionSeg = getLong("duracionSeg")?.toInt() ?: 0,
                atendidoMillis = getLong("atendidoMillis") ?: 0L
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.aAccion(): AccionRegistro? {
        return try {
            AccionRegistro(
                id = id,
                eventoId = getString("eventoId") ?: "",
                accion = getString("accion") ?: return null,
                usuario = getString("usuario") ?: "",
                rol = getString("rol") ?: "",
                fechaMillis = getTimestamp("fecha")?.toDate()?.time ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}