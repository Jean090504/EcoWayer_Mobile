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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.QrCodeScanner
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.MensagemApi
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.ui.components.QrCodeDialog
import com.example.ecowayer.ui.components.VoucherQr
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelos temporários. Quando a API estiver pronta, gere estas classes a partir
 * do JSON retornado e mova para a pasta data/model.
 */
data class Mensagem(
    val id: Int,
    val texto: String,
    val hora: String,        // ex.: "09:25" (formate a partir da data que vier da API)
    val minha: Boolean       // true = enviada por mim, false = recebida
)

private fun MensagemApi.toUi() = Mensagem(
    id = id,
    texto = texto,
    hora = criado_em.orEmpty().replace('T', ' ').takeLast(8).take(5),
    minha = id_remetente == SessionStore.currentUser()?.id
)

private val FundoTela = Color(0xFFFAFAFA)
private val BolhaRecebida = Color(0xFFF1F5F9)

@Composable
fun ConversaScreen(
    conversaId: Int = 0,
    onBack: () -> Unit = {}
) {
    val viewModel: ConversaViewModel = viewModel()
    val estado by viewModel.estado.collectAsState()
    LaunchedEffect(conversaId) { viewModel.carregar(conversaId) }
    val mensagens = estado.mensagens.map { it.toUi() }
    val nomeContato = "Conversa #$conversaId"
    val statusContato = if (estado.carregando) "Carregando..." else "Mensagens da conversa"
    val contatoOnline = false

    var texto by remember { mutableStateOf("") }
    val estadoLista = rememberLazyListState()

    var mostrarQr by remember { mutableStateOf(false) }
    var voucher by remember { mutableStateOf<VoucherQr?>(null) }

    // Rola para a última mensagem ao abrir e quando chegar/enviar uma nova
    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) estadoLista.animateScrollToItem(mensagens.lastIndex)
    }

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

            // FOTO DO CONTATO (com indicador de online) -----------------------
            // Quando a API devolver a URL (Coil: io.coil-kt:coil-compose):
            //
            // AsyncImage(
            //     model = contato.fotoUrl,
            //     contentDescription = nomeContato,
            //     contentScale = ContentScale.Crop,
            //     modifier = Modifier.size(52.dp).clip(CircleShape)
            // )
            Box {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                )
                if (contatoOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CD137))
                    )
                }
            }
            // -----------------------------------------------------------------

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(nomeContato, fontSize = 17.sp, fontWeight = FontWeight.Bold)
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

        // Mensagens
        LazyColumn(
            state = estadoLista,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rótulo do dia (agrupar por dia quando vier da API)
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Hoje",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF444444),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F1F1))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }

            items(mensagens, key = { it.id }) { mensagem ->
                BolhaMensagem(mensagem)
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
private fun BolhaMensagem(mensagem: Mensagem) {
    val minha = mensagem.minha

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (minha) Alignment.End else Alignment.Start
    ) {
        Text(
            text = mensagem.texto,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = if (minha) Color.White else EcoColors.TextoPrimario,
            modifier = Modifier
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
