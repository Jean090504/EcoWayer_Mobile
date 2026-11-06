package com.example.ecowayer.ui.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Home : Routes("home")
    object Cadastro : Routes("cadastro")   // <- nova
}