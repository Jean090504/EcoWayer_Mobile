package com.example.ecowayer.ui.screens.notificacoes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

/** Tipo da notificação: define o ícone e as cores do círculo. */
enum class TipoNotificacao(
    val icone: ImageVector,
    val corIcone: Color,
    val corFundo: Color
) {
    CHAT(Icons.Outlined.ChatBubbleOutline, Color(0xFF5E9E4D), Color(0xFFEAF3E8)),
    PONTOS(Icons.Outlined.WorkspacePremium, Color(0xFF5E9E4D), Color(0xFFEAF3E8)),
    PROPOSTA(Icons.Outlined.SwapHoriz, Color(0xFF8E4A9E), Color(0xFFF3E8F5)),
    DENUNCIA(Icons.Outlined.WarningAmber, Color(0xFFE05252), Color(0xFFFDE8E8))
}

/**
 * Modelo temporário. Quando a API estiver pronta, gere esta classe a partir do
 * JSON retornado e mova para a pasta data/model.
 */
data class Notificacao(
    val id: Int,
    val titulo: String,
    val tempo: String,          // ex.: "Agora mesmo", "Há 2h", "Ontem"
    val grupo: String,          // ex.: "Hoje", "Ontem"
    val tipo: TipoNotificacao
)

@Composable
fun NotificacoesScreen(
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onAbrirNotificacao: (Notificacao) -> Unit = {}
) {
    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // val viewModel: NotificacoesViewModel = viewModel()
    // val notificacoes by viewModel.notificacoes.collectAsState()  // GET /notificacoes
    //
    // LaunchedEffect(Unit) { viewModel.carregar() }
    //
    // Ao abrir a tela, marcar como lidas:
    //   PATCH /notificacoes/lidas
    // O back-end pode devolver o campo "grupo" (Hoje/Ontem) ou você calcula
    // no ViewModel a partir da data (ex.: createdAt).
    // Trate também carregando e erro (CircularProgressIndicator / "Tentar de novo").
    // ----------------------------------------------------------------------
    val notificacoes = listOf(                                  // TODO API
        Notificacao(1, "Carlos enviou uma mensagem no chat.", "Agora mesmo", "Hoje", TipoNotificacao.CHAT),
        Notificacao(2, "+50 pontos creditados por reciclagem de Vidro.", "Há 2h", "Hoje", TipoNotificacao.PONTOS),
        Notificacao(3, "Você recebeu uma proposta em \"Papelão para Mudança\".", "Ontem", "Ontem", TipoNotificacao.PROPOSTA),
        Notificacao(4, "Sua denúncia foi processada e enviada para revisão.", "Ontem", "Ontem", TipoNotificacao.DENUNCIA)
    )

    // Mantém a ordem em que os grupos aparecem (Hoje, depois Ontem)
    val grupos = notificacoes.groupBy { it.grupo }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraInferior(
                selecionado = DestinoBarra.Perfil,
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
            // Cabeçalho com seta de voltar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.Black
                    )
                }
                Text(
                    "Notificações",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.Verde
                )
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grupos.forEach { (titulo, lista) ->
                    item(key = "grupo_$titulo") {
                        Text(
                            titulo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF777777),
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                    items(lista, key = { it.id }) { notificacao ->
                        ItemNotificacao(
                            notificacao = notificacao,
                            onClick = {
                                // FLUXO (futuro): abrir o destino de cada tipo
                                //   CHAT     -> navController.navigate(Routes.Chat.route)
                                //   PONTOS   -> navegarPelaBarra(DestinoBarra.Carteira)
                                //   PROPOSTA -> navController.navigate(Routes.DetalhesAnuncio.criar(id))
                                //   DENUNCIA -> tela de denúncias
                                onAbrirNotificacao(notificacao)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemNotificacao(
    notificacao: Notificacao,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8E8EC), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(notificacao.tipo.corFundo),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                notificacao.tipo.icone,
                contentDescription = null,
                tint = notificacao.tipo.corIcone,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                notificacao.titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = EcoColors.TextoPrimario
            )
            Text(
                notificacao.tempo,
                fontSize = 11.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificacoesScreenPreview() {
    EcowayerTheme { NotificacoesScreen() }
}