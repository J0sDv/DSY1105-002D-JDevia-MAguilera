package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.navigation.MainViewModel
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.screens.HomeScreen
import com.example.modoguardian.ui.screens.LoginScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.collectLatest
import com.example.modoguardian.ui.screens.RegistroScreen
import com.example.modoguardian.viewmodel.RegistroViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModoGuardianTheme {

                val navViewModel: MainViewModel = viewModel()
                val loginViewModel: LoginViewModel = viewModel()
                val navController = rememberNavController()

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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(paddingValues = innerPadding)
                    ) {
                        // Pantalla de login: al tener exito pasa al Home; tambien puede ir al registro
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
                                // Google desde el registro: se entrega el token al login y se vuelve a el
                                onGoogleToken = { idToken ->
                                    loginViewModel.onGoogleToken(idToken)
                                    navViewModel.navigateBack()
                                }
                            )
                        }
                        composable(route = Screen.Home.route) {
                            HomeScreen(
                                viewModel = loginViewModel,
                                onCerrarSesion = {
                                    loginViewModel.cerrarSesion()
                                    navViewModel.navigateTo(
                                        screen = Screen.Login,
                                        popUpToRoute = Screen.Home,
                                        inclusive = true
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}