package com.example.modoguardian.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.modoguardian.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

// Posibles resultados al pedir la cuenta de Google
sealed class ResultadoGoogle {
    data class Token(val idToken: String) : ResultadoGoogle() // Cuenta elegida
    data object Cancelado : ResultadoGoogle() // La persona cerró el selector
    data object SinCuentas : ResultadoGoogle() // No hay cuentas de Google en el dispositivo
    data class Error(val mensaje: String) : ResultadoGoogle() // Cualquier otro problema
}

// Abre el selector de cuentas de Google y devuelve el token
suspend fun obtenerTokenGoogle(context: Context): ResultadoGoogle {
    return try {
        // default_web_client_id lo genera el plugin de Google a partir de google-services.json
        val opcion = GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id)).build()
        val solicitud = GetCredentialRequest.Builder().addCredentialOption(opcion).build()

        val respuesta = CredentialManager.create(context).getCredential(context = context, request = solicitud)
        val credencial = respuesta.credential

        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            ResultadoGoogle.Token(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
        } else {
            ResultadoGoogle.Error("No se pudo leer la cuenta de Google.")
        }
    } catch (e: GetCredentialCancellationException) {
        ResultadoGoogle.Cancelado
    } catch (e: NoCredentialException) {
        ResultadoGoogle.SinCuentas
    } catch (e: GetCredentialException) {
        ResultadoGoogle.Error("No se pudo iniciar sesión con Google. Revisa que el Paso 5 esté completo.")
    } catch (e: Exception) {
        ResultadoGoogle.Error("No se pudo iniciar sesión con Google.")
    }
}

// Devuelve la accion del boton "Continuar con Google": abre el selector y entrega el resultado
@Composable
fun rememberGoogleSignIn(
    onToken: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return {
        scope.launch {
            when (val resultado = obtenerTokenGoogle(context)) {
                is ResultadoGoogle.Token -> onToken(resultado.idToken)
                ResultadoGoogle.Cancelado -> { /* No pasa nada: la persona decidió no continuar */ }
                ResultadoGoogle.SinCuentas ->
                    onError("No hay una cuenta de Google en este dispositivo. Agrega una en Ajustes e inténtalo de nuevo.")
                is ResultadoGoogle.Error -> onError(resultado.mensaje)
            }
        }
    }
}