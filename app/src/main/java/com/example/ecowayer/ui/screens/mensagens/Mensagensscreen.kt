package com.example.ecowayer.ui.screens.mensagens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.ConversaApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

/**
 * Modelo temporário. Quando a API estiver pronta, gere esta classe a partir do
 * JSON retornado e mova para a pasta data/model.
 */
data class Conversa(
    val id: Int,
    val nome: String,
    val ultimaMensagem: String,
    val tempo: String,           // ex.: "há 2 min" (formate a partir da data que vier da API)
    val naoLidas: Int = 0,
    val fotoUrl: String? = null
)

private fun ConversaApi.toUi() = Conversa(
    id = id,
    nome = nome_contato ?: contato?.nome ?: "Conversa #$id",
    ultimaMensagem = ultima_mensagem.orEmpty().ifBlank { "Nenhuma mensagem ainda" },
    tempo = data_ultima_mensagem.orEmpty(),
    naoLidas = nao_lidas,
    fotoUrl = contato?.foto
)

@Composable
fun MensagensScreen(
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onAbrirConversa: (Conversa) -> Unit = {}
) {
    val viewModel: MensagensViewModel = viewModel()
    val estado by viewModel.estado.collectAsState()
    val conversas = estado.conversas.map { it.toUi() }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            // Nenhum item selecionado, como no protótipo
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
                    "Mensagens",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.Verde
                )
            }

            if (estado.carregando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Carregando conversas...", color = Color.Gray) }
            } else if (estado.erro != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(estado.erro!!, color = Color(0xFFB3261E)) }
            } else if (conversas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Você ainda não tem conversas.",
                        fontSize = 14.sp,
                        color = Color(0xFF777777)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    items(conversas, key = { it.id }) { conversa ->
                        ItemConversa(
                            conversa = conversa,
                            onClick = {
                                // FLUXO (futuro): abrir a conversa (tela de chat com o histórico)
                                //   navController.navigate(Routes.Conversa.criar(conversa.id))
                                //   dados: GET /conversas/{id}/mensagens
                                //          POST /conversas/{id}/mensagens   (enviar)
                                //          PATCH /conversas/{id}/lida       (zerar não lidas)
                                onAbrirConversa(conversa)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemConversa(conversa: Conversa, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FOTO DO CONTATO ---------------------------------------------------
        // Quando a API devolver a URL (Coil: io.coil-kt:coil-compose):
        //
        // AsyncImage(
        //     model = conversa.fotoUrl,
        //     contentDescription = conversa.nome,
        //     contentScale = ContentScale.Crop,
        //     modifier = Modifier.size(52.dp).clip(CircleShape)
        // )
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        )
        // -------------------------------------------------------------------

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                conversa.nome,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                conversa.ultimaMensagem,
                fontSize = 13.sp,
                color = Color(0xFF555555),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(conversa.tempo, fontSize = 12.sp, color = Color(0xFF666666))
            if (conversa.naoLidas > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(EcoColors.Verde),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conversa.naoLidas.toString(),
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MensagensScreenPreview() {
    EcowayerTheme { MensagensScreen() }
}
