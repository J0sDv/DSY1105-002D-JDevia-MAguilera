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
                        composable(route = Screen.Login.route) {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginExitoso = {
                                    navViewModel.navigateTo(
                                        screen = Screen.Home,
                                        popUpToRoute = Screen.Login,
                                        inclusive = true
                                    )
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