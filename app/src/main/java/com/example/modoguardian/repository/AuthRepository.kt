package com.example.modoguardian.repository

import com.example.modoguardian.model.ResultadoLogin
import com.example.modoguardian.model.ResultadoRegistro
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.Usuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

// Conecta la app con Firebase: Authentication (cuentas) y Firestore (perfil y rol)
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Registro con correo: crea la cuenta, guarda el perfil y envia el correo de verificacion
    suspend fun registrarUsuario(
        email: String,
        password: String,
        username: String,
        region: String
    ): ResultadoRegistro {
        val correo = email.trim()
        val nombre = username.trim()
        val claveUsername = nombre.lowercase()
        var usuarioCreado: FirebaseUser? = null

        return try {
            // 1) Crear la cuenta en Firebase Authentication
            val resultado = auth.createUserWithEmailAndPassword(correo, password).await()
            val usuario = resultado.user ?: return ResultadoRegistro.Error("No se pudo crear la cuenta")
            usuarioCreado = usuario

            // 2) Revisar que el nombre de usuario no este repetido
            val refUsername = db.collection("usernames").document(claveUsername)
            if (refUsername.get().await().exists()) {
                usuario.delete().await()   // se elimina la cuenta recién creada
                usuarioCreado = null
                auth.signOut()
                return ResultadoRegistro.UsernameExistente
            }

            // 3) Guardar perfil y username de forma atomica (o se guardan los dos o ninguno)
            val lote = db.batch()
            lote.set(
                db.collection("users").document(usuario.uid),
                mapOf(
                    "username" to nombre,
                    "email" to correo,
                    "region" to region,
                    "rol" to Rol.OPERADOR.name,   // toda cuenta nueva parte como Operador
                    "creado" to FieldValue.serverTimestamp()
                )
            )
            lote.set(refUsername, mapOf("uid" to usuario.uid))
            lote.commit().await()

            // 4) Enviar el correo con el enlace de verificacion (si falla, la cuenta igual queda creada)
            val enviado = try {
                usuario.sendEmailVerification().await()
                true
            } catch (ex: Exception) {
                false
            }

            // 5) Cerrar sesion: no se puede entrar hasta verificar el correo
            auth.signOut()
            ResultadoRegistro.Exito(correoEnviado = enviado)
        } catch (e: FirebaseAuthUserCollisionException) {
            // Ya existe una cuenta con ese correo
            ResultadoRegistro.CorreoExistente
        } catch (e: FirebaseAuthWeakPasswordException) {
            ResultadoRegistro.Error("La contraseña es muy débil. Usa otra más segura.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            ResultadoRegistro.Error("El correo no tiene un formato válido.")
        } catch (e: FirebaseNetworkException) {
            eliminarCuentaIncompleta(usuarioCreado)
            ResultadoRegistro.SinConexion
        } catch (e: Exception) {
            eliminarCuentaIncompleta(usuarioCreado)
            ResultadoRegistro.Error("No se pudo crear la cuenta. Inténtalo nuevamente.")
        }
    }

    // Login con correo: valida credenciales y exige el correo verificado
    suspend fun iniciarSesion(email: String, password: String): ResultadoLogin {
        val correo = email.trim()

        return try {
            // 1) Validar correo y contraseña en Firebase Authentication
            val resultado = auth.signInWithEmailAndPassword(correo, password).await()
            val usuario = resultado.user ?: return ResultadoLogin.Error("No se pudo iniciar sesión")

            // 2) Actualizar los datos de la cuenta y comprobar si el correo ya fue verificado
            usuario.reload().await()
            if (!usuario.isEmailVerified) {
                // Se reenvia el enlace y se cierra la sesion
                val reenviado = try {
                    usuario.sendEmailVerification().await()
                    true
                } catch (ex: Exception) {
                    false
                }
                auth.signOut()
                return ResultadoLogin.CorreoNoVerificado(correo, reenviado)
            }

            // 3) Leer el perfil guardado en Firestore (username, rol y región)
            val perfil = db.collection("users").document(usuario.uid).get().await()
            ResultadoLogin.Exito(armarUsuario(perfil, correo))
        } catch (e: FirebaseAuthInvalidUserException) {
            ResultadoLogin.CredencialesInvalidas
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            ResultadoLogin.CredencialesInvalidas
        } catch (e: FirebaseNetworkException) {
            ResultadoLogin.SinConexion
        } catch (e: FirebaseTooManyRequestsException) {
            ResultadoLogin.Error("Demasiados intentos. Espera unos minutos e inténtalo de nuevo.")
        } catch (e: Exception) {
            ResultadoLogin.Error("Ocurrió un error inesperado. Inténtalo nuevamente.")
        }
    }

    // Login con Google: recibe el token que entrega Credential Manager
    suspend fun iniciarSesionConGoogle(idToken: String): ResultadoLogin {
        return try {
            val credencial = GoogleAuthProvider.getCredential(idToken, null)
            val resultado = auth.signInWithCredential(credencial).await()
            val usuario = resultado.user ?: return ResultadoLogin.Error("No se pudo iniciar sesión con Google")
            val correo = usuario.email ?: ""

            val refPerfil = db.collection("users").document(usuario.uid)
            var perfil = refPerfil.get().await()

            // Primera vez con Google: se crea el perfil (Google ya verifico el correo)
            if (!perfil.exists()) {
                crearPerfilGoogle(usuario, correo)
                perfil = refPerfil.get().await()
            }

            ResultadoLogin.Exito(armarUsuario(perfil, correo))
        } catch (e: FirebaseNetworkException) {
            ResultadoLogin.SinConexion
        } catch (e: FirebaseAuthUserCollisionException) {
            ResultadoLogin.Error("Ya existe una cuenta con ese correo creada con contraseña. Ingresa con tu contraseña.")
        } catch (e: Exception) {
            ResultadoLogin.Error("No se pudo iniciar sesión con Google. Inténtalo nuevamente.")
        }
    }

    // Cierra la sesion de Firebase
    fun cerrarSesion() {
        auth.signOut()
    }

    // Funciones auxiliares

    // Arma el usuario a partir del perfil guardado en Firestore
    private fun armarUsuario(perfil: DocumentSnapshot, correo: String): Usuario {
        val nombre = perfil.getString("username") ?: correo.substringBefore("@")
        val rol = try {
            Rol.valueOf(perfil.getString("rol") ?: Rol.OPERADOR.name)
        } catch (ex: Exception) {
            Rol.OPERADOR
        }
        val region = perfil.getString("region") ?: ""
        return Usuario(nombre = nombre, email = correo, rol = rol, region = region)
    }

    // Crea el perfil de una cuenta nueva de Google, con un username que no este repetido
    private suspend fun crearPerfilGoogle(usuario: FirebaseUser, correo: String) {
        var base = (usuario.displayName ?: correo.substringBefore("@"))
            .filter { it.isLetterOrDigit() && it.code < 128 }
            .take(14)
        if (base.length < 3) base += "usr"

        var nombre = base
        var intentos = 0
        while (db.collection("usernames").document(nombre.lowercase()).get().await().exists() && intentos < 10) {
            nombre = base + SecureRandom().nextInt(1000)
            intentos++
        }

        val lote = db.batch()
        lote.set(
            db.collection("users").document(usuario.uid),
            mapOf(
                "username" to nombre,
                "email" to correo,
                "region" to "Sin especificar",
                "rol" to Rol.OPERADOR.name,
                "creado" to FieldValue.serverTimestamp()
            )
        )
        lote.set(db.collection("usernames").document(nombre.lowercase()), mapOf("uid" to usuario.uid))
        lote.commit().await()
    }

    // Si algo falla a mitad del registro, se borra la cuenta para poder reintentar
    private suspend fun eliminarCuentaIncompleta(usuario: FirebaseUser?) {
        try {
            usuario?.delete()?.await()
        } catch (ex: Exception) {
            // Si no se puede borrar, no se hace nada mas: la app no se cae
        }
        auth.signOut()
    }
}