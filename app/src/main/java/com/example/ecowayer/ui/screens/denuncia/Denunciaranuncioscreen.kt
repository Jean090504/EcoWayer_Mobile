package com.example.ecowayer.ui.screens.denuncia

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.TipoDenuncia
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

private val Vermelho = Color(0xFFC93B36)

@Composable
fun DenunciarScreen(
    onBack: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    onQrCode: () -> Unit,
    viewModel: DenunciaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current
    LaunchedEffect(estado.enviada) {
        if (estado.enviada) {
            Toast.makeText(contexto, "Denúncia enviada.", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }
    BackHandler(enabled = estado.enviando) {}
    FormularioDenuncia(
        mensagemSelecionada = viewModel.mensagemSelecionada,
        estado = estado,
        onBack = { if (!estado.enviando) onBack() },
        onNavegar = { if (!estado.enviando) onNavegar(it) },
        onQrCode = { if (!estado.enviando) onQrCode() },
        onEnviar = viewModel::enviar,
        onTentarNovamente = viewModel::carregarMotivos
    )
}

@Composable
private fun FormularioDenuncia(
    mensagemSelecionada: Boolean,
    estado: DenunciaEstado,
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onEnviar: (Int, String) -> Unit = { _, _ -> },
    onTentarNovamente: () -> Unit = {}
) {
    var motivo by rememberSaveable { mutableStateOf<Int?>(null) }
    var detalhes by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraInferior(
                selecionado = if (mensagemSelecionada) null else DestinoBarra.Loja,
                onSelecionar = onNavegar,
                onQrCode = onQrCode
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append(if (mensagemSelecionada) "Mensagem" else "Anúncio") }
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

                if (mensagemSelecionada) {
                    Column(
                        Modifier.padding(top = 12.dp).fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)).background(Color(0xFFF4F2F8)).padding(12.dp)
                    ) {
                        Text("Mensagem anexada", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        estado.mensagem?.let { texto ->
                            Text(texto, fontSize = 14.sp, maxLines = 5, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Column(modifier = Modifier.padding(top = 12.dp)) {
                    when {
                        estado.carregandoMotivos -> CircularProgressIndicator()
                        estado.erroMotivos != null -> {
                            Text(estado.erroMotivos, color = Vermelho)
                            TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
                        }
                        estado.motivos.isEmpty() -> Text("Nenhum motivo de denúncia disponível.")
                        else -> estado.motivos.forEach { opcao ->
                            OpcaoMotivo(
                                rotulo = opcao.rotulo(),
                                selecionado = motivo == opcao.id,
                                onClick = { if (!estado.enviando) motivo = opcao.id }
                            )
                        }
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
                    enabled = !estado.enviando,
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

                estado.erroEnvio?.let {
                    Text(it, color = Vermelho, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                }
                val podeEnviar = !estado.enviando && !estado.enviada && estado.motivos.any { it.id == motivo }
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
                        if (estado.enviando) "Enviando..." else "Enviar denúncia",
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
    EcowayerTheme {
        FormularioDenuncia(
            mensagemSelecionada = false,
            estado = DenunciaEstado(
                motivos = listOf("Conteúdo impróprio", "Item divergente", "Golpe", "Outro")
                    .mapIndexed { indice, nome -> TipoDenuncia(id = indice + 1, tipo = nome) }
            )
        )
    }
}
