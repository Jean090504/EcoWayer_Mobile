package com.example.ecowayer

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.navigation.Routes
import com.example.ecowayer.ui.navigation.navegarPelaBarra
import org.junit.Rule
import org.junit.Test

class NavegacaoCarteiraTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun barraDaCarteiraAbreInicioLojaEPerfilInclusivePeloHistorico() {
        compose.setContent {
            val nav = rememberNavController()
            val selecionar: (DestinoBarra) -> Unit = { nav.navegarPelaBarra(it) }
            NavHost(nav, startDestination = Routes.Home.route) {
                composable(Routes.Home.route) {
                    Column {
                        Text("Tela inicial")
                        BarraInferior(DestinoBarra.Inicio, selecionar, {})
                    }
                }
                composable(Routes.Loja.route) {
                    Column {
                        Text("Anúncios disponíveis")
                        BarraInferior(DestinoBarra.Loja, selecionar, {})
                    }
                }
                composable(Routes.Carteira.route) {
                    Column {
                        Text("Minha Carteira")
                        TextButton(onClick = { nav.navigate(Routes.ExtratoPontos.route) }) {
                            Text("Abrir extrato")
                        }
                        BarraInferior(DestinoBarra.Carteira, selecionar, {})
                    }
                }
                composable(Routes.ExtratoPontos.route) {
                    Column {
                        Text("Extrato recente")
                        BarraInferior(DestinoBarra.Carteira, selecionar, {})
                    }
                }
                composable(Routes.Perfil.route) {
                    Column {
                        Text("Meu Perfil")
                        TextButton(onClick = { nav.navigate(Routes.Carteira.route) }) {
                            Text("Abrir histórico de trocas")
                        }
                        BarraInferior(DestinoBarra.Perfil, selecionar, {})
                    }
                }
            }
        }

        tocar("Carteira")
        verificar("Minha Carteira")
        tocar("Loja")
        verificar("Anúncios disponíveis")
        tocar("Carteira")
        verificar("Minha Carteira")
        tocar("Perfil")
        verificar("Meu Perfil")
        tocar("Abrir histórico de trocas")
        verificar("Minha Carteira")
        tocar("Perfil")
        verificar("Meu Perfil")
        tocar("Abrir histórico de trocas")
        tocar("Abrir extrato")
        verificar("Extrato recente")
        tocar("Início")
        verificar("Tela inicial")
        tocar("Carteira")
        tocar("Início")
        verificar("Tela inicial")
    }

    private fun tocar(rotulo: String) {
        compose.onNodeWithText(rotulo).performClick()
    }

    private fun verificar(titulo: String) {
        compose.onNodeWithText(titulo).assertExists()
    }
}
