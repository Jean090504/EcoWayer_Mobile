package com.example.ecowayer.ui.screens.conversa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.MensagemApi
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.ui.components.ImagemRemota
import com.example.ecowayer.ui.components.QrCodeDialog
import com.example.ecowayer.ui.components.VoucherQr
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import kotlinx.coroutines.CancellationException

private data class Mensagem(
    val id: Int,
    val texto: String,
    val data: String,
    val hora: String,
    val minha: Boolean
)

private fun MensagemApi.toUi() = Mensagem(
    id = id,
    texto = texto,
    data = criado_em.orEmpty().take(10).split('-').reversed().joinToString("/"),
    hora = criado_em.orEmpty().drop(11).take(5),
    minha = id_remetente == SessionStore.currentUser()?.id
)

private val FundoTela = Color(0xFFFAFAFA)
private val BolhaRecebida = Color(0xFFF1F5F9)

@Composable
fun ConversaScreen(
    conversaId: Int = 0,
    onBack: () -> Unit = {},
    onDenunciarMensagem: (Int) -> Unit = {}
) {
    val viewModel: ConversaViewModel = viewModel()
    val estado by viewModel.estado.collectAsState()
    LaunchedEffect(conversaId) { viewModel.carregar(conversaId) }
    val mensagens = estado.mensagens.map { it.toUi() }
    val contato by viewModel.contato.collectAsState()
    LaunchedEffect(conversaId) { viewModel.carregarContato(conversaId) }
    val nomeContato = contato?.nome.orEmpty().ifBlank { "Conversa" }
    val statusContato = if (estado.carregando) "Carregando..." else "Mensagens da conversa"

    var texto by remember { mutableStateOf("") }
    val estadoLista = rememberLazyListState()
    val linhas = mensagens.asReversed()

    LaunchedEffect(linhas.firstOrNull()?.id) {
        if (linhas.isNotEmpty()) estadoLista.scrollToItem(0)
    }

    var mostrarQr by remember { mutableStateOf(false) }
    var voucher by remember { mutableStateOf<VoucherQr?>(null) }

    fun enviar() {
        val conteudo = texto.trim()
        if (conteudo.isEmpty()) return

        viewModel.enviar(conversaId, conteudo)
        texto = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoTela)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = EcoColors.TextoPrimario
                )
            }

            ImagemRemota(
                url = contato?.foto,
                descricao = nomeContato,
                modifier = Modifier.size(52.dp).clip(CircleShape)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    nomeContato,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(statusContato, fontSize = 12.sp, color = Color(0xFF555555))
            }

            IconButton(onClick = { mostrarQr = true }) {
                Icon(
                    Icons.Outlined.QrCodeScanner,
                    contentDescription = "Gerar QR Code",
                    tint = EcoColors.Verde
                )
            }
        }

        estado.erro?.let { erro ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(erro, Modifier.weight(1f).padding(start = 16.dp))
                TextButton(onClick = { viewModel.carregar(conversaId) }) {
                    Text("Recarregar")
                }
            }
        }

        LazyColumn(
            state = estadoLista,
            reverseLayout = true,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(linhas.size, key = { linhas[it].id }) { index ->
                val mensagem = linhas[index]
                Column {
                    if (mensagem.data.isNotBlank() && mensagem.data != linhas.getOrNull(index + 1)?.data) {
                        Text(
                            mensagem.data,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF555555)
                        )
                    }
                    BolhaMensagem(mensagem) { onDenunciarMensagem(mensagem.id) }
                }
            }
            if (mensagens.isEmpty() && !estado.carregando && estado.erro == null) {
                item { Text("Nenhuma mensagem nesta conversa.") }
            }
        }

        // Campo de digitação
        Column(
            modifier = Modifier
                .background(Color.White)
                .navigationBarsPadding()
        ) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    textStyle = TextStyle(fontSize = 14.sp, color = EcoColors.TextoPrimario),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    modifier = Modifier.weight(1f),
                    decorationBox = { campo ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFF4F5F7))
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            if (texto.isEmpty()) {
                                Text(
                                    "Digite sua mensagem...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                            campo()
                        }
                    }
                )

                val podeEnviar = texto.isNotBlank()
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EcoColors.Verde.copy(alpha = if (podeEnviar) 1f else 0.5f))
                        .clickable(enabled = podeEnviar) { enviar() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    // Popup do QR Code
    if (mostrarQr) {
        QrCodeDialog(
            voucher = voucher,
            onGerar = {
                // INTEGRAÇÃO BACK-END (futuro): POST /conversas/{id}/voucher
                voucher = VoucherQr(
                    codigo = "VERDE-8371-ECO",      // TODO API
                    validoAte = "15/05/2026",       // TODO API
                    conteudoQr = "VERDE-8371-ECO"   // TODO API
                )
            },
            onFechar = {
                mostrarQr = false
                voucher = null
            }
        )
    }
}

@Composable
private fun BolhaMensagem(mensagem: Mensagem, onDenunciar: () -> Unit) {
    val minha = mensagem.minha

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (minha) Alignment.End else Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = mensagem.texto,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = if (minha) Color.White else EcoColors.TextoPrimario,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 280.dp)
                    .clip(
                        if (minha) {
                            RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                        } else {
                            RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                        }
                    )
                    .background(if (minha) EcoColors.Roxo else BolhaRecebida)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
            if (!minha) {
                IconButton(onClick = onDenunciar) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Denunciar mensagem")
                }
            }
        }
        Text(
            text = mensagem.hora,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConversaScreenPreview() {
    EcowayerTheme { ConversaScreen() }
}
