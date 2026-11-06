package com.example.ecowayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ecowayer.ui.navigation.AppNavGraph
import com.example.ecowayer.ui.theme.EcowayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcowayerTheme {
                AppNavGraph()
            }
        }
    }
}