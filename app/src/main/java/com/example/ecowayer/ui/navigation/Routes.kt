package com.example.ecowayer.ui.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Home : Routes("home")
    object Cadastro : Routes("cadastro")
    object VerificacaoNumero : Routes("verificacao_numero")
    object VerificacaoEmail : Routes("verificacao_email")
    object Endereco : Routes("endereco")

    object Loja : Routes("loja")
    object BoasVindas : Routes("boas_vindas")
}