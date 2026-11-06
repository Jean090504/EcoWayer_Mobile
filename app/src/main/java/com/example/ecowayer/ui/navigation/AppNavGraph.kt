package com.example.ecowayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecowayer.ui.screens.CadastroScreen.CadastroScreen
import com.example.ecowayer.ui.screens.home.HomeScreen
import com.example.ecowayer.ui.screens.splash.SplashScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                onEntrar = { email, senha ->
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onCriarConta = {
                    navController.navigate(Routes.Cadastro.route)
                },
                onEsqueciSenha = {
                    // TODO: tela de recuperação de senha
                }
            )
        }

        composable(Routes.Cadastro.route) {
            CadastroScreen(
                onBack = { navController.popBackStack() },
                onAvancar = {
                    // por enquanto vai para a Home; depois chame a API aqui
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen()
        }
    }
}