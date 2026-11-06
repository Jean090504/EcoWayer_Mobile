package com.example.ecowayer.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecowayer.ui.theme.EcowayerTheme

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Home - Mapa")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EcowayerTheme {
        HomeScreen()
    }
}