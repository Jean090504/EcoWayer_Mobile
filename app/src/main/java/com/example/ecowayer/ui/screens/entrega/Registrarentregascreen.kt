package com.example.ecowayer.ui.screens.entrega

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

@Composable
fun RegistrarEntregaScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onConcluido: () -> Unit = {}
) {
    var pesoKg by remember { mutableStateOf("") }
    var fotoAnexada by remember { mutableStateOf(false) }
    // var codigoQr by remember { mutableStateOf<String?>(null) }   // preenchido ao ler o QR

    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // val viewModel: EntregaViewModel = viewModel()
    //
    // Pontos por kg (hoje fixo em 20): pode vir da API junto com o EcoPonto
    //   GET /ecopontos/{id}/regras  ->  { pontosPorKg: 20 }
    // ----------------------------------------------------------------------
    val pontosPorKg = 20   // TODO API

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            // Nenhum item selecionado: esta tela abre pelo botão central (QR Code)
            BarraInferior(
                selecionado = null,
                onSelecionar = onNavegar,
                onQrCode = onQrCode
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Registrar ") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("entrega") }
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                LeitorQr()

                // Etiqueta de pontos
                Text(
                    text = "+$pontosPorKg pts por kg",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(EcoColors.Verde)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )

                // Peso estimado
                CardLinha(
                    icone = {
                        IconeQuadrado(Icons.Outlined.Scale, EcoColors.Verde)
                    },
                    titulo = "Peso estimado"
                ) {
                    BasicTextField(
                        value = pesoKg,
                        onValueChange = { novo ->
                            if (novo.length <= 4 && novo.all { it.isDigit() }) pesoKg = novo
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoColors.Verde,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier.width(52.dp),
                        decorationBox = { campo ->
                            Box(contentAlignment = Alignment.CenterEnd) {
                                if (pesoKg.isEmpty()) {
                                    Text("0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBBBBBB))
                                }
                                campo()
                            }
                        }
                    )
                    Text(
                        " kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoColors.Verde
                    )
                }

                // Foto do material
                CardLinha(
                    icone = {
                        IconeQuadrado(Icons.Outlined.CameraAlt, EcoColors.Roxo)
                    },
                    titulo = "Foto do material"
                ) {
                    Text(
                        text = if (fotoAnexada) "Trocar" else "Anexar",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(EcoColors.Roxo)
                            .clickable {
                                // FOTO (futuro) ------------------------------------
                                // Abrir câmera/galeria:
                                //
                                // val launcher = rememberLauncherForActivityResult(
                                //     ActivityResultContracts.PickVisualMedia()
                                // ) { uri -> fotoUri = uri }
                                // launcher.launch(PickVisualMediaRequest(
                                //     ActivityResultContracts.PickVisualMedia.ImageOnly))
                                //
                                // (para tirar a foto na hora, use TakePicture)
                                // ---------------------------------------------------
                                fotoAnexada = true   // temporário
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Botão fixo, logo acima da barra inferior
            val podeConfirmar = pesoKg.isNotEmpty() && pesoKg.toInt() > 0
            // Quando o QR estiver funcionando: && codigoQr != null
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(50))
                    .background(EcoColors.Verde.copy(alpha = if (podeConfirmar) 1f else 0.4f))
                    .clickable(enabled = podeConfirmar) {
                        // --- INTEGRAÇÃO BACK-END (futuro) -----------------------
                        // viewModel.confirmarColeta(
                        //     codigoQr = codigoQr,
                        //     pesoKg = pesoKg.toInt(),
                        //     foto = fotoUri
                        // )                      // POST /entregas (multipart, com a foto)
                        //   onSuccess -> onConcluido()
                        //   onError   -> mostrar mensagem
                        // --------------------------------------------------------
                        onConcluido()   // temporário
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Confirmar coleta",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Área da câmera com moldura e linha de leitura animada. */
@Composable
private fun LeitorQr() {
    val progresso by rememberInfiniteTransition(label = "leitura").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "linhaLeitura"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF111111)),
        contentAlignment = Alignment.Center
    ) {
        // CÂMERA (futuro) ---------------------------------------------------
        // Para ler o QR Code de verdade, use CameraX + ML Kit:
        //
        // dependências: androidx.camera:camera-camera2, camera-lifecycle, camera-view
        //               com.google.mlkit:barcode-scanning
        // manifest:     <uses-permission android:name="android.permission.CAMERA" />
        //
        // Coloque aqui um AndroidView com PreviewView (fundo) e, no
        // ImageAnalysis, chame o BarcodeScanner. Quando ler um código:
        //   codigoQr = barcode.rawValue
        // e valide na API:  GET /ecopontos/qr/{codigo}
        // -------------------------------------------------------------------

        // Moldura e linha de leitura (visual)
        Canvas(Modifier.fillMaxSize()) {
            val lado = 110.dp.toPx()
            val esquerda = (size.width - lado) / 2f
            val topo = (size.height - lado) / 2f - 10.dp.toPx()
            val canto = lado * 0.3f
            val espessura = 3.dp.toPx()
            val cor = Color(0xFF666666)

            fun linha(x1: Float, y1: Float, x2: Float, y2: Float) =
                drawLine(cor, Offset(x1, y1), Offset(x2, y2), espessura, StrokeCap.Round)

            val dir = esquerda + lado
            val base = topo + lado
            // cantos
            linha(esquerda, topo, esquerda + canto, topo); linha(esquerda, topo, esquerda, topo + canto)
            linha(dir, topo, dir - canto, topo);           linha(dir, topo, dir, topo + canto)
            linha(esquerda, base, esquerda + canto, base); linha(esquerda, base, esquerda, base - canto)
            linha(dir, base, dir - canto, base);           linha(dir, base, dir, base - canto)

            // linha verde de leitura
            val y = topo + lado * progresso
            drawLine(
                color = EcoColors.Verde,
                start = Offset(esquerda - 30.dp.toPx(), y),
                end = Offset(dir + 30.dp.toPx(), y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Text(
            text = "Aponte para o QR Code do EcoPonto",
            fontSize = 12.sp,
            color = Color(0xFF9A9A9A),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        )
    }
}

@Composable
private fun CardLinha(
    icone: @Composable () -> Unit,
    titulo: String,
    conteudoDireita: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icone()
        Text(
            titulo,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp)
        )
        Spacer(Modifier.weight(1f))
        conteudoDireita()
    }
}

@Composable
private fun IconeQuadrado(icone: androidx.compose.ui.graphics.vector.ImageVector, cor: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cor),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrarEntregaScreenPreview() {
    EcowayerTheme { RegistrarEntregaScreen() }
}