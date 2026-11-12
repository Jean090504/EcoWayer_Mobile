package com.example.ecowayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.screens.CadastroScreen.CadastroScreen
import com.example.ecowayer.ui.screens.boasvindas.BoasVindasScreen
import com.example.ecowayer.ui.screens.carteira.CarteiraScreen
import com.example.ecowayer.ui.screens.conversa.ConversaScreen
import com.example.ecowayer.ui.screens.criarAnuncio.CriarAnuncioScreen
import com.example.ecowayer.ui.screens.detalhes.DetalhesAnuncioScreen
import com.example.ecowayer.ui.screens.ecopontos.EcoPontosScreen
import com.example.ecowayer.ui.screens.endereco.EnderecoScreen
import com.example.ecowayer.ui.screens.entrega.RegistrarEntregaScreen
import com.example.ecowayer.ui.screens.home.HomeScreen
import com.example.ecowayer.ui.screens.insignias.InsigniasScreen
import com.example.ecowayer.ui.screens.loja.LojaScreen
import com.example.ecowayer.ui.screens.mensagens.MensagensScreen
import com.example.ecowayer.ui.screens.meusanuncios.MeusAnunciosScreen
import com.example.ecowayer.ui.screens.notificacoes.NotificacoesScreen
import com.example.ecowayer.ui.screens.perfil.PerfilScreen
import com.example.ecowayer.ui.screens.splash.SplashScreen
import com.example.ecowayer.ui.screens.vereficacao.VerificacaoEmailScreen
import com.example.ecowayer.ui.screens.vereficacao.VerificacaoNumeroScreen

private fun NavController.navegarPelaBarra(destino: DestinoBarra) {
    val rota = when (destino) {
        DestinoBarra.Inicio -> Routes.Home.route
        DestinoBarra.Loja -> Routes.Loja.route
        DestinoBarra.Carteira -> Routes.Carteira.route
        DestinoBarra.Perfil -> Routes.Perfil.route
    }

    navigate(rota) {
        popUpTo(Routes.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Botão central (QRCode) da barra inferior: abre "Registrar entrega". */
private fun NavController.abrirRegistrarEntrega() {
    navigate(Routes.RegistrarEntrega.route) {
        launchSingleTop = true   // não abre outra cópia se já estiver nela
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
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onBuscarEcoPontos = { navController.navigate(Routes.EcoPontos.route) },
                onVerExtrato = { navController.navegarPelaBarra(DestinoBarra.Carteira) },
                onTrocarPontos = { navController.navegarPelaBarra(DestinoBarra.Carteira) }
            )
        }

        composable(Routes.EcoPontos.route) {
            EcoPontosScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        // Uma única rota da Loja, com todos os callbacks juntos
        composable(Routes.Loja.route) {
            LojaScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onCriarAnuncio = { navController.navigate(Routes.CriarAnuncio.route) },
                onAbrirAnuncio = { anuncio ->
                    navController.navigate(Routes.DetalhesAnuncio.criar(anuncio.id))
                }
            )
        }

        composable(Routes.CriarAnuncio.route) {
            CriarAnuncioScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onPublicar = { anuncio ->
                    // TODO: enviar para a API
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.DetalhesAnuncio.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            DetalhesAnuncioScreen(
                anuncioId = entrada.arguments?.getInt("id") ?: 0,
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(Routes.RegistrarEntrega.route) {
            RegistrarEntregaScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { /* já está nesta tela */ },
                onConcluido = { navController.popBackStack() }
            )
        }

        composable(Routes.Carteira.route) {
            CarteiraScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(Routes.Perfil.route) {
            PerfilScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onInsignias = { navController.navigate(Routes.Insignias.route) },
                onNotificacoes = { navController.navigate(Routes.Notificacoes.route) },
                // onTrocasPontos = { navController.navigate(Routes.TrocasPontos.route) },
                onMeusAnuncios = { navController.navigate(Routes.MeusAnuncios.route) },
                // onConfiguracoes = { navController.navigate(Routes.Configuracoes.route) },
                onChat = { navController.navigate(Routes.Chat.route) },
                onSair = {
                    // TODO: limpar o token / chamar a API de logout antes de navegar
                    navController.navigate(Routes.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Insignias.route) {
            InsigniasScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(Routes.Notificacoes.route) {
            NotificacoesScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        // Dentro do NavHost (antes estava fora, depois do fechamento do NavHost)
        composable(Routes.MeusAnuncios.route) {
            MeusAnunciosScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onCriarAnuncio = { navController.navigate(Routes.CriarAnuncio.route) },
                onAbrirAnuncio = { anuncio ->
                    navController.navigate(Routes.DetalhesAnuncio.criar(anuncio.id))
                }
            )
        }

        composable(Routes.Chat.route) {
            MensagensScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onAbrirConversa = { conversa ->
                    navController.navigate(Routes.Conversa.criar(conversa.id))
                }
            )
        }

        composable(
            route = Routes.Conversa.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            ConversaScreen(
                conversaId = entrada.arguments?.getInt("id") ?: 0,
                onBack = { navController.popBackStack() },
            )
        }
    }

}