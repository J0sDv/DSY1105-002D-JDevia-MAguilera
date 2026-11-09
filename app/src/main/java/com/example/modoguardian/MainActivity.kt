package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.modoguardian.navigation.MainViewModel
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.screens.AlertaDetalleScreen
import com.example.modoguardian.ui.screens.CamaraDetalleScreen
import com.example.modoguardian.ui.screens.HomeScreen
import com.example.modoguardian.ui.screens.LoginScreen
import com.example.modoguardian.ui.screens.RegistroScreen
import com.example.modoguardian.ui.screens.UsuariosAdminScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.GuardianViewModel
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.RegistroViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            // ◀ NUEVO: se crea antes del tema porque controla el modo oscuro
            val guardianViewModel: GuardianViewModel = viewModel()
            val modoOscuro by guardianViewModel.modoOscuro.collectAsState()

            // ◀ CAMBIO: darkTheme sigue la preferencia; si no hay, el tema del sistema
            ModoGuardianTheme(darkTheme = modoOscuro ?: isSystemInDarkTheme()) {

                // ViewModels y NavController
                val navViewModel: MainViewModel = viewModel()      // eventos de navegación
                val loginViewModel: LoginViewModel = viewModel()   // sesión compartida Login y Home
                val navController = rememberNavController()

                // Escuchar eventos de navegación emitidos por el ViewModel
                LaunchedEffect(key1 = Unit) {
                    navViewModel.navigationEvents.collectLatest { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> {
                                navController.navigate(route = event.route.route) {
                                    event.popUpToRoute?.let {
                                        popUpTo(route = it.route) {
                                            inclusive = event.inclusive
                                        }
                                    }
                                    launchSingleTop = event.singleTop
                                    restoreState = true
                                }
                            }
                            is NavigationEvent.PopBackStack -> navController.popBackStack()
                            is NavigationEvent.NavigateUp -> navController.navigateUp()
                        }
                    }
                }

                // Layout base con NavHost
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(paddingValues = innerPadding)
                    ) {
                        // Pantalla de login: al tener éxito pasa al Home; también puede ir al registro
                        composable(route = Screen.Login.route) {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginExitoso = {
                                    navViewModel.navigateTo(
                                        screen = Screen.Home,
                                        popUpToRoute = Screen.Login,
                                        inclusive = true
                                    )
                                },
                                onIrARegistro = {
                                    navViewModel.navigateTo(
                                        screen = Screen.Registro,
                                        singleTop = true
                                    )
                                }
                            )
                        }

                        // Pantalla de registro: tiene su propio ViewModel; volver regresa al login
                        composable(route = Screen.Registro.route) {
                            val registroViewModel: RegistroViewModel = viewModel()
                            RegistroScreen(
                                viewModel = registroViewModel,
                                onVolver = { navViewModel.navigateBack() },
                                onGoogleToken = { idToken ->
                                    loginViewModel.onGoogleToken(idToken)
                                    navViewModel.navigateBack()
                                }
                            )
                        }

                        // ◀ CAMBIO: Home con las 5 pestañas
                        composable(route = Screen.Home.route) {
                            HomeScreen(
                                loginViewModel = loginViewModel,
                                guardianViewModel = guardianViewModel,
                                onAbrirCamara = { id -> navViewModel.navigateTo(Screen.CamaraDetalle(id)) },
                                onAbrirAlerta = { id -> navViewModel.navigateTo(Screen.AlertaDetalle(id)) },
                                onGestionarUsuarios = { navViewModel.navigateTo(Screen.Usuarios) },
                                onCerrarSesion = {
                                    guardianViewModel.detenerSincronizacion()   // ◀ NUEVO
                                    loginViewModel.cerrarSesion()
                                    navViewModel.navigateTo(
                                        screen = Screen.Login,
                                        popUpToRoute = Screen.Home,
                                        inclusive = true
                                    )
                                }
                            )
                        }

                        // ◀ NUEVO: detalle de cámara (recibe el id por la ruta)
                        composable(
                            route = Screen.CAMARA_DETALLE,
                            arguments = listOf(navArgument("camaraId") { type = NavType.StringType })
                        ) { entrada ->
                            CamaraDetalleScreen(
                                camaraId = entrada.arguments?.getString("camaraId") ?: "",
                                guardianVm = guardianViewModel,
                                onVolver = { navViewModel.navigateBack() }
                            )
                        }

                        // ◀ NUEVO: detalle de alerta o evento
                        composable(
                            route = Screen.ALERTA_DETALLE,
                            arguments = listOf(navArgument("eventoId") { type = NavType.StringType })
                        ) { entrada ->
                            val sesion by loginViewModel.estado.collectAsState()
                            AlertaDetalleScreen(
                                eventoId = entrada.arguments?.getString("eventoId") ?: "",
                                guardianVm = guardianViewModel,
                                sesion = sesion,
                                onVerCamara = { id -> navViewModel.navigateTo(Screen.CamaraDetalle(id)) },
                                onVolver = { navViewModel.navigateBack() }
                            )
                        }

                        // ◀ NUEVO: gestión de usuarios (Administrador)
                        composable(route = Screen.Usuarios.route) {
                            UsuariosAdminScreen(
                                guardianVm = guardianViewModel,
                                onVolver = { navViewModel.navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}