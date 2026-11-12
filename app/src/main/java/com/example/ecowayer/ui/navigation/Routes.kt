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
    object EcoPontos : Routes("ecopontos")
    object CriarAnuncio : Routes("criar_anuncio")

    object DetalhesAnuncio : Routes("detalhes_anuncio/{id}") {
        fun criar(id: Int) = "detalhes_anuncio/$id"
    }

    object RegistrarEntrega : Routes("registrar_entrega")

    object Carteira : Routes("carteira")

    object Perfil : Routes("perfil")

    object Insignias : Routes("insignias")

    object Notificacoes : Routes("notificacoes")   // NOVO

    object MeusAnuncios : Routes("meus_anuncios")

    object Chat : Routes("chat")

    object Conversa : Routes("conversa/{id}") {
        fun criar(id: Int) = "conversa/$id"
    }
}