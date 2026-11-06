package com.example.ecowayer.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ecowayer.R
import com.example.ecowayer.ui.components.LoginPanel
import com.example.ecowayer.ui.theme.EcowayerTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_DURATION_MS = 4000

@Composable
fun SplashScreen(
    onEntrar: (email: String, senha: String) -> Unit,
    onCriarConta: () -> Unit,
    onEsqueciSenha: () -> Unit
) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.6f) }
    val bgScale = remember { Animatable(1f) }
    var mostrarLogin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch {
                bgScale.animateTo(1.15f, tween(SPLASH_DURATION_MS + 1500, easing = FastOutSlowInEasing))
            }
            launch { logoAlpha.animateTo(1f, tween(1200)) }
            launch {
                logoScale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                delay(SPLASH_DURATION_MS.toLong())
                mostrarLogin = true // quando a animação termina, o login sobe
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E5E2E))
            .clipToBounds()
    ) {
        Image(
            painter = painterResource(R.drawable.gif_fundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = bgScale.value
                    scaleY = bgScale.value
                }
        )

        Image(
            painter = painterResource(R.drawable.logo_ecowayer),
            contentDescription = "Logo EcoWayer",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
                .size(160.dp)
                .graphicsLayer {
                    alpha = logoAlpha.value
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                }
        )

        AnimatedVisibility(
            visible = mostrarLogin,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(900, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(600)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.6f)
                .imePadding()
        ) {
            LoginPanel(
                onEntrar = onEntrar,
                onCriarConta = onCriarConta,
                onEsqueciSenha = onEsqueciSenha,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    EcowayerTheme {
        SplashScreen(onEntrar = { _, _ -> }, onCriarConta = {}, onEsqueciSenha = {})
    }
}