package com.example.ecowayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.screens.CadastroScreen.CadastroScreen
import com.example.ecowayer.ui.screens.boasvindas.BoasVindasScreen
import com.example.ecowayer.ui.screens.endereco.EnderecoScreen
import com.example.ecowayer.ui.screens.home.HomeScreen
import com.example.ecowayer.ui.screens.loja.LojaScreen
import com.example.ecowayer.ui.screens.splash.SplashScreen
import com.example.ecowayer.ui.screens.vereficacao.VerificacaoEmailScreen
import com.example.ecowayer.ui.screens.vereficacao.VerificacaoNumeroScreen

private fun NavController.navegarPelaBarra(destino: DestinoBarra) {
    val rota = when (destino) {
        DestinoBarra.Inicio -> Routes.Home.route
        DestinoBarra.Loja -> Routes.Loja.route
        DestinoBarra.Carteira -> null   // TODO: Routes.Carteira.route quando existir
        DestinoBarra.Perfil -> null     // TODO: Routes.Perfil.route quando existir
    } ?: return

    navigate(rota) {
        popUpTo(Routes.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

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
                onCriarConta = { navController.navigate(Routes.Cadastro.route) },
                onEsqueciSenha = {
                    // TODO: tela de recuperação de senha
                }
            )
        }

        composable(Routes.Cadastro.route) {
            CadastroScreen(
                onBack = { navController.popBackStack() },
                onAvancar = {
                    // Futuro: chamar a API que envia o código SMS antes de navegar
                    navController.navigate(Routes.VerificacaoNumero.route)
                }
            )
        }

        composable(Routes.VerificacaoNumero.route) {
            VerificacaoNumeroScreen(
                onBack = { navController.popBackStack() },
                onVerificado = { navController.navigate(Routes.VerificacaoEmail.route) }
            )
        }

        composable(Routes.VerificacaoEmail.route) {
            VerificacaoEmailScreen(
                onBack = { navController.popBackStack() },
                onVerificado = { navController.navigate(Routes.Endereco.route) }
            )
        }

        composable(Routes.Endereco.route) {
            EnderecoScreen(
                onBack = { navController.popBackStack() },
                onFinalizar = {
                    navController.navigate(Routes.BoasVindas.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.BoasVindas.route) {
            BoasVindasScreen(
                onFinalizar = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.BoasVindas.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) }
            )
        }

        composable(Routes.Loja.route) {
            LojaScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) }
            )
        }
    }
}