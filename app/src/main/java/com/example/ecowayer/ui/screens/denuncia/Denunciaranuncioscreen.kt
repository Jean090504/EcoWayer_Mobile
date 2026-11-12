package com.example.ecowayer.ui.screens.denuncia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

private val Vermelho = Color(0xFFC93B36)

/** Motivos de denúncia. O `codigo` é o que deve ir para a API. */
enum class MotivoDenuncia(val rotulo: String, val codigo: String) {
    CONTEUDO_IMPROPRIO("Conteúdo impróprio", "conteudo_improprio"),
    ITEM_DIVERGENTE("Item divergente", "item_divergente"),
    GOLPE("Golpe", "golpe"),
    OUTRO("Outro", "outro")
}

@Composable
fun DenunciarAnuncioScreen(
    anuncioId: Int = 0,
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onEnviar: (motivo: MotivoDenuncia, detalhes: String) -> Unit = { _, _ -> }
) {
    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // No onEnviar, chame o ViewModel:
    //
    // viewModel.denunciar(anuncioId, motivo.codigo, detalhes)   // POST /anuncios/{id}/denuncias
    //   body: { "motivo": "golpe", "detalhes": "..." }
    //
    // Trate carregando (desabilitar o botão) e erro (mensagem + tentar de novo)
    // e só volte para a tela anterior quando a API responder com sucesso.
    // ----------------------------------------------------------------------
    var motivo by remember { mutableStateOf<MotivoDenuncia?>(null) }
    var detalhes by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraInferior(
                selecionado = DestinoBarra.Loja,
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
                .imePadding()
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = EcoColors.TextoPrimario
                    )
                }
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Denunciar ") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Anúncio") }
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    "Selecione o motivo da denúncia para nos ajudar a manter a comunidade segura.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF444444)
                )

                Column(modifier = Modifier.padding(top = 12.dp)) {
                    MotivoDenuncia.values().forEach { opcao ->
                        OpcaoMotivo(
                            rotulo = opcao.rotulo,
                            selecionado = motivo == opcao,
                            onClick = { motivo = opcao }
                        )
                    }
                }

                Text(
                    "Detalhes da denúncia (Opcional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                BasicTextField(
                    value = detalhes,
                    onValueChange = { detalhes = it },
                    textStyle = TextStyle(fontSize = 14.sp, color = EcoColors.TextoPrimario),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    decorationBox = { campo ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            if (detalhes.isEmpty()) {
                                Text(
                                    "Descreva brevemente o que aconteceu...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                            campo()
                        }
                    }
                )

                val podeEnviar = motivo != null
                Box(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Vermelho.copy(alpha = if (podeEnviar) 1f else 0.5f))
                        .clickable(enabled = podeEnviar) {
                            motivo?.let { onEnviar(it, detalhes.trim()) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Enviar denúncia",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OpcaoMotivo(rotulo: String, selecionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selecionado,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Vermelho,
                unselectedColor = Color(0xFF8A8A8A)
            )
        )
        Text(
            rotulo,
            fontSize = 16.sp,
            fontWeight = if (selecionado) FontWeight.Medium else FontWeight.Normal,
            color = EcoColors.TextoPrimario,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DenunciarAnuncioScreenPreview() {
    EcowayerTheme { DenunciarAnuncioScreen() }
}