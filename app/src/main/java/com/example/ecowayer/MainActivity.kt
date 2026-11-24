package com.example.ecowayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ecowayer.ui.navigation.AppNavGraph
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.example.ecowayer.ui.theme.ModoTema
import com.example.ecowayer.ui.theme.PreferenciaTema
import com.example.ecowayer.service.RetrofitFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitFactory.inicializar(applicationContext)
        PreferenciaTema.carregar(applicationContext)
        enableEdgeToEdge()
        setContent {
            val modo by PreferenciaTema.modo.collectAsState()
            val escuro = when (modo) {
                ModoTema.Sistema -> isSystemInDarkTheme()
                ModoTema.Claro -> false
                ModoTema.Escuro -> true
            }
            EcowayerTheme(darkTheme = escuro) {
                AppNavGraph()
            }
        }
    }
}
