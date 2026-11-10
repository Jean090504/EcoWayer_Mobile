package com.example.ecowayer.ui.screens.boasvindas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

private enum class Canto { TopoEsq, TopoDir, BaseEsq, BaseDir }

private val VerdeClaro = Color(0xFFEAF2E6)
private val RoxoClaro = Color(0xFFEEE9F5)

/** Desenha uma "mancha" orgânica + uma linha fina em um canto da tela. */
private fun DrawScope.blob(canto: Canto, cor: Color, corLinha: Color) {
    val w = size.width
    val h = size.height
    val espelhaX = if (canto == Canto.TopoDir || canto == Canto.BaseDir) -1f else 1f
    val espelhaY = if (canto == Canto.BaseEsq || canto == Canto.BaseDir) -1f else 1f

    // Forma base desenhada no canto superior esquerdo; as outras são espelhos dela.
    val forma = Path().apply {
        moveTo(0f, 0f)
        lineTo(w * 0.60f, 0f)
        cubicTo(w * 0.55f, h * 0.07f, w * 0.35f, h * 0.06f, w * 0.20f, h * 0.11f)
        cubicTo(w * 0.10f, h * 0.14f, w * 0.03f, h * 0.18f, 0f, h * 0.21f)
        close()
    }
    val linha = Path().apply {
        moveTo(w * 0.30f, 0f)
        cubicTo(w * 0.28f, h * 0.05f, w * 0.18f, h * 0.07f, w * 0.08f, h * 0.10f)
        cubicTo(w * 0.04f, h * 0.11f, w * 0.01f, h * 0.13f, 0f, h * 0.15f)
    }

    scale(espelhaX, espelhaY, pivot = Offset(w / 2f, h / 2f)) {
        drawPath(forma, cor)
        drawPath(linha, corLinha, style = Stroke(width = 1.5.dp.toPx()))
    }
}

private data class PaginaBoasVindas(
    val titulo: @Composable () -> androidx.compose.ui.text.AnnotatedString,
    val texto: String,
    val botao: String,
    val decoracao: DrawScope.() -> Unit
)

@Composable
fun BoasVindasScreen(
    onFinalizar: () -> Unit
) {
    var pagina by remember { mutableIntStateOf(0) }

    val paginas = listOf(
        PaginaBoasVindas(
            titulo = {
                buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Bem-vindo\nao") }
                }
            },
            texto = "Descartando seu lixo de forma inteligente.",
            botao = "Avançar",
            decoracao = {
                blob(Canto.TopoEsq, VerdeClaro, EcoColors.Roxo)
                blob(Canto.BaseDir, RoxoClaro, EcoColors.Roxo)
            }
        ),
        PaginaBoasVindas(
            titulo = {
                buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Venda\nTroca e\n") }
                    withStyle(SpanStyle(color = EcoColors.Verde)) { append("Doações") }
                }
            },
            texto = "Tem algo parado aí?\nnão jogue fora, doe ou desapegue.\nA natureza agradece!",
            botao = "Avançar",
            decoracao = {
                blob(Canto.TopoEsq, VerdeClaro, EcoColors.Verde)
                blob(Canto.BaseEsq, RoxoClaro, EcoColors.Roxo)
            }
        ),
        PaginaBoasVindas(
            titulo = {
                buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Tudo\npronto!") }
                }
            },
            texto = "Pode começar a utilizar nosso app já.",
            botao = "Finalizar",
            decoracao = {
                blob(Canto.TopoDir, VerdeClaro, EcoColors.Roxo)
                blob(Canto.BaseEsq, VerdeClaro, EcoColors.Verde)
            }
        )
    )
    // Animação de "flutuar" da imagem do meio (sobe e desce devagar)
    val flutuar by rememberInfiniteTransition(label = "flutuar").animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flutuarY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Conteúdo da página: formas + título + imagem + texto.
        // O AnimatedContent faz a troca deslizando + esmaecendo entre as páginas.
        AnimatedContent(
            targetState = pagina,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                (slideInHorizontally(tween(450)) { it / 3 } + fadeIn(tween(450))) togetherWith
                        (slideOutHorizontally(tween(450)) { -it / 3 } + fadeOut(tween(300)))
            },
            label = "paginaBoasVindas"
        ) { indice ->
            val atual = paginas[indice]

            Box(Modifier.fillMaxSize()) {
                // Formas decorativas (feitas em código, não precisam de imagem)
                Canvas(Modifier.fillMaxSize()) { atual.decoracao(this) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(start = 32.dp, end = 32.dp, bottom = 140.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = atual.titulo(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 44.sp
                    )

                    // ESPAÇO PARA A IMAGEM DO MEIO -------------------------
                    // Troque o conteúdo do Box por uma Image quando tiver os arquivos
                    // (res/drawable, ex.: ilustracao_logo.png, ilustracao_moveis.png,
                    // ilustracao_pronto.png):
                    //
                    // Image(
                    //     painter = painterResource(R.drawable.ilustracao_logo),
                    //     contentDescription = null,
                    //     modifier = Modifier.size(200.dp)
                    // )
                    Box(
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .size(width = 240.dp, height = 200.dp)
                            .graphicsLayer { translationY = flutuar.dp.toPx() },
                        contentAlignment = Alignment.Center
                    ) {
                        // placeholder temporário (pode apagar)
                        Text("imagem ${indice + 1}", color = Color.LightGray, fontSize = 12.sp)
                    }
                    // -------------------------------------------------------

                    Text(
                        text = atual.texto,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        color = EcoColors.TextoPrimario
                    )
                }
            }
        }

        // Botão + bolinhas ficam fixos embaixo (não animam junto com a página)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (pagina < paginas.lastIndex) pagina++ else onFinalizar()
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoColors.Roxo,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = paginas[pagina].botao,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }

            // Indicador de página: a bolinha ativa alarga e muda de cor suavemente
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(paginas.size) { i ->
                    val ativa = i == pagina
                    val largura by animateDpAsState(if (ativa) 18.dp else 6.dp, tween(300), label = "larguraDot")
                    val cor by animateColorAsState(
                        if (ativa) EcoColors.Roxo else Color(0xFFDDDDDD), tween(300), label = "corDot"
                    )
                    Box(
                        Modifier
                            .size(width = largura, height = 6.dp)
                            .clip(CircleShape)
                            .background(cor)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoasVindasPreview() {
    EcowayerTheme { BoasVindasScreen(onFinalizar = {}) }
}