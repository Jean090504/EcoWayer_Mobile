package com.example.ecowayer.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Verified
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
import androidx.compose.ui.text.SpanStyle
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

/**
 * Modelo temporário. Quando a API estiver pronta, gere esta classe a partir do
 * JSON retornado e mova para a pasta data/model.
 */
data class PerfilUsuario(
    val nome: String,
    val verificado: Boolean,
    val membroDesde: String,        // ex.: "mar/2024"
    val nota: String,               // ex.: "4.9"
    val totalEntregas: Int,
    val conquistas: Int,
    val anunciosPublicados: Int,
    val fotoUrl: String? = null
)

@Composable
fun PerfilScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onNotificacoes: () -> Unit = {},
    onTrocasPontos: () -> Unit = {},
    onInsignias: () -> Unit = {},
    onMeusAnuncios: () -> Unit = {},
    onConfiguracoes: () -> Unit = {},
    onChat: () -> Unit = {},
    onSair: () -> Unit = {}
) {
    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // Troque o mock abaixo por um estado vindo do ViewModel:
    //
    // val viewModel: PerfilViewModel = viewModel()
    // val perfil by viewModel.perfil.collectAsState()      // GET /usuarios/me/perfil
    //
    // LaunchedEffect(Unit) { viewModel.carregar() }
    //
    // Trate também carregando e erro (CircularProgressIndicator / "Tentar de novo").
    // ----------------------------------------------------------------------
    val perfil = PerfilUsuario(                              // TODO API
        nome = "Lucas Mendes",
        verificado = true,
        membroDesde = "mar/2024",
        nota = "4.9",
        totalEntregas = 48,
        conquistas = 12,
        anunciosPublicados = 3
    )

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
            // Cabeçalho com sino de notificações
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Meu ") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Perfil") }
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    // FLUXO (futuro): abrir a tela de notificações
                    //   navController.navigate(Routes.Notificacoes.route)
                    //   dados: GET /notificacoes
                    onNotificacoes()
                }) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notificações",
                        tint = EcoColors.Verde
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dados do usuário
                item { CabecalhoUsuario(perfil) }

                item { Spacer(Modifier.padding(top = 4.dp)) }

                // Trocas de pontos
                item {
                    ItemMenu(
                        icone = Icons.Outlined.ReceiptLong,
                        titulo = "Trocas de pontos",
                        detalhe = "Ver histórico",
                        onClick = {
                            // FLUXO (futuro): histórico de trocas/resgates de vouchers
                            //   navController.navigate(Routes.TrocasPontos.route)
                            //   dados: GET /usuarios/me/trocas
                            onTrocasPontos()
                        }
                    )
                }

                // Minhas insígnias
                item {
                    ItemMenu(
                        icone = Icons.Outlined.WorkspacePremium,
                        titulo = "Minhas insígnias",
                        detalhe = "${perfil.conquistas} conquistas",
                        onClick = {
                            // FLUXO (futuro): lista de insígnias/conquistas e níveis
                            //   navController.navigate(Routes.Insignias.route)
                            //   dados: GET /usuarios/me/insignias
                            onInsignias()
                        }
                    )
                }

                // Meus anúncios
                item {
                    ItemMenu(
                        icone = Icons.Outlined.ListAlt,
                        titulo = "Meus anúncios",
                        detalhe = "${perfil.anunciosPublicados} anúncios publicados",
                        onClick = {
                            // FLUXO (futuro): anúncios do usuário (editar, pausar, excluir)
                            //   navController.navigate(Routes.MeusAnuncios.route)
                            //   dados: GET /usuarios/me/anuncios
                            onMeusAnuncios()
                        }
                    )
                }

                // Configurações
                item {
                    ItemMenu(
                        icone = Icons.Outlined.Settings,
                        titulo = "Configurações",
                        onClick = {
                            // FLUXO (futuro): dados da conta, endereço, senha, notificações
                            //   navController.navigate(Routes.Configuracoes.route)
                            //   dados: GET /usuarios/me  /  PUT /usuarios/me
                            onConfiguracoes()
                        }
                    )
                }

                // Chat
                item {
                    ItemMenu(
                        icone = Icons.Outlined.ChatBubbleOutline,
                        titulo = "Chat",
                        onClick = {
                            // FLUXO (futuro): lista de conversas com anunciantes/compradores
                            //   navController.navigate(Routes.Chat.route)
                            //   dados: GET /conversas  (e WebSocket para mensagens novas)
                            onChat()
                        }
                    )
                }

                // Sair
                item {
                    ItemMenu(
                        icone = Icons.AutoMirrored.Outlined.Logout,
                        titulo = "sair",
                        onClick = {
                            // FLUXO (futuro) -----------------------------------
                            // 1) (opcional) confirmar com um AlertDialog "Deseja sair?"
                            // 2) viewModel.sair()
                            //      - POST /auth/logout (invalida o token no servidor)
                            //      - apagar o token salvo (DataStore/SharedPreferences)
                            // 3) voltar para a tela inicial limpando a pilha:
                            //      navController.navigate(Routes.Splash.route) {
                            //          popUpTo(0) { inclusive = true }
                            //      }
                            // ---------------------------------------------------
                            onSair()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CabecalhoUsuario(perfil: PerfilUsuario) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // FOTO DO USUÁRIO ---------------------------------------------------
        // Quando a API devolver a URL (Coil: io.coil-kt:coil-compose):
        //
        // AsyncImage(
        //     model = perfil.fotoUrl,
        //     contentDescription = "Foto de perfil",
        //     contentScale = ContentScale.Crop,
        //     modifier = Modifier.size(72.dp).clip(CircleShape)
        // )
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        )
        // -------------------------------------------------------------------

        Column(
            modifier = Modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(perfil.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (perfil.verificado) {
                    Icon(
                        Icons.Outlined.Verified,
                        contentDescription = "Perfil verificado",
                        tint = EcoColors.Verde,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(18.dp)
                    )
                }
            }
            Text(
                "Membro desde ${perfil.membroDesde}",
                fontSize = 12.sp,
                color = Color(0xFF777777)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.StarBorder, null,
                    tint = Color(0xFFF5B301),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    "${perfil.nota} (${perfil.totalEntregas} entregas)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ItemMenu(
    icone: ImageVector,
    titulo: String,
    onClick: () -> Unit,
    detalhe: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, null, tint = EcoColors.Verde, modifier = Modifier.size(22.dp))
        Text(
            titulo,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp)
        )
        Spacer(Modifier.weight(1f))
        if (detalhe != null) {
            Text(detalhe, fontSize = 11.sp, color = Color(0xFF888888))
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = Color(0xFF888888),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilScreenPreview() {
    EcowayerTheme { PerfilScreen() }
}