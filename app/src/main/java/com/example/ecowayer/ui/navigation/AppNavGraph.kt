package com.example.ecowayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecowayer.ui.screens.home.HomeScreen
import com.example.ecowayer.ui.screens.splash.SplashScreen

// Arquivo responsável pela navegação do app: ele define quais telas
// existem e como o usuário vai de uma para outra.

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
                    // por enquanto só navega; depois a API será chamada aqui
                    navController.navigate(Routes.Home.route) {
                        // tira a splash da pilha: o botão voltar não retorna pra ela
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onCriarConta = {
                    // TODO: navegar para o cadastro quando a tela existir
                },
                onEsqueciSenha = {
                    // TODO: tela de recuperação de senha
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen()
        }
    }
}