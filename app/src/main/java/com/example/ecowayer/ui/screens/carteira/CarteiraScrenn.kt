package com.example.ecowayer.ui.screens.carteira

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.text.NumberFormat
import java.util.Locale

/**
 * Modelos temporários. Quando a API estiver pronta, gere estas classes a partir
 * do JSON retornado e mova para a pasta data/model.
 */
data class ResumoCarteira(
    val pontos: Int,
    val nivelAtual: String,        // ex.: "Guardião"
    val proximoNivel: String,      // ex.: "Floresta"
    val progresso: Float,          // 0f..1f
    val pontosParaProximoNivel: Int
)

data class Missao(
    val titulo: String,
    val andamento: String,         // ex.: "2 de 3"
    val pontos: Int
)

data class Voucher(
    val id: Int,
    val titulo: String,
    val custoPontos: Int,
    val validadeDias: Int,
    val imagemUrl: String? = null
)

data class MovimentoExtrato(
    val titulo: String,
    val data: String,              // ex.: "Hoje, 09:14"
    val pontos: Int                // positivo = ganho, negativo = gasto
)

private fun Int.formatado(): String =
    NumberFormat.getIntegerInstance(Locale("pt", "BR")).format(this)

@Composable
fun CarteiraScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onVerTodasMissoes: () -> Unit = {},
    onTrocarVoucher: (Voucher) -> Unit = {}
) {
    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // Troque os dados abaixo por estados vindos de um CarteiraViewModel:
    //
    // val viewModel: CarteiraViewModel = viewModel()
    // val resumo by viewModel.resumo.collectAsState()       // GET /usuarios/me/carteira
    // val missoes by viewModel.missoes.collectAsState()     // GET /missoes?periodo=semanal
    // val vouchers by viewModel.vouchers.collectAsState()   // GET /vouchers
    // val extrato by viewModel.extrato.collectAsState()     // GET /usuarios/me/extrato
    //
    // LaunchedEffect(Unit) { viewModel.carregarTudo() }
    //
    // Trate também carregando, erro e listas vazias.
    // Trocar voucher:  viewModel.trocarVoucher(voucher.id)  // POST /vouchers/{id}/resgatar
    // ----------------------------------------------------------------------
    val resumo = ResumoCarteira(3480, "Guardião", "Floresta", 0.7f, 520)          // TODO API
    val missoes = listOf(                                                           // TODO API
        Missao("3 entregas em 7 dias", "2 de 3", 300),
        Missao("Separar Vidro", "0 de 1", 150)
    )
    val vouchers = listOf(                                                          // TODO API
        Voucher(1, "R$ 10 no Mercado Verde", 1000, 30)
    )
    val extrato = listOf(                                                           // TODO API
        MovimentoExtrato("Entrega de PET · 12 kg", "Hoje, 09:14", 240)
    )

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraInferior(
                selecionado = DestinoBarra.Carteira,
                onSelecionar = onNavegar,
                onQrCode = onQrCode
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Minha ") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Carteira") }
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Pontos acumulados
            item { CardPontos(resumo) }

            // Missões semanais
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TituloSecao("Missões semanais")
                    Text(
                        "Ver todas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoColors.Verde,
                        modifier = Modifier.clickable { onVerTodasMissoes() }
                    )
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(missoes) { missao -> CardMissao(missao) }
                }
            }

            // Vouchers
            item { TituloSecao("Vouchers") }
            items(vouchers) { voucher ->
                CardVoucher(voucher, onTrocar = { onTrocarVoucher(voucher) })
            }

            // Extrato
            item { TituloSecao("Extrato") }
            items(extrato) { movimento -> ItemExtrato(movimento) }
        }
    }
}

@Composable
private fun TituloSecao(texto: String) {
    Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun CardPontos(resumo: ResumoCarteira) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pontos Acumulados", fontSize = 14.sp, color = Color(0xFF555555))
            Icon(
                Icons.Outlined.AccountBalanceWallet, null,
                tint = EcoColors.Verde,
                modifier = Modifier.size(22.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
        ) {
            Text(
                resumo.pontos.formatado(),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                " pts",
                fontSize = 16.sp,
                color = Color(0xFF555555),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Nível ${resumo.nivelAtual}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Verde
            )
            Text(
                "Próx: Nível ${resumo.proximoNivel}",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }

        // Barra de progresso do nível
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE6E6E6))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(resumo.progresso.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(EcoColors.Verde)
            )
        }

        Text(
            "Faltam ${resumo.pontosParaProximoNivel.formatado()} pts para subir de nível",
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun CardMissao(missao: Missao) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(missao.titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(missao.andamento, fontSize = 12.sp, color = Color(0xFF666666))
        Text(
            "+${missao.pontos} pts",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(EcoColors.Verde)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun CardVoucher(voucher: Voucher, onTrocar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // IMAGEM DO VOUCHER -------------------------------------------------
        // AsyncImage(
        //     model = voucher.imagemUrl,
        //     contentDescription = voucher.titulo,
        //     contentScale = ContentScale.Crop,
        //     modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
        // )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE0E0E0))
        )
        // -------------------------------------------------------------------

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(voucher.titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                "${voucher.custoPontos.formatado()} pts · válido ${voucher.validadeDias} dias",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }

        Text(
            "Trocar",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(EcoColors.Verde)
                .clickable { onTrocar() }
                .padding(horizontal = 18.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun ItemExtrato(movimento: MovimentoExtrato) {
    val ganho = movimento.pontos >= 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (ganho) EcoColors.Verde else Color(0xFF9E9E9E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.ArrowDownward, null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(movimento.titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(movimento.data, fontSize = 12.sp, color = Color(0xFF666666))
        }

        Text(
            text = (if (ganho) "+" else "") + movimento.pontos,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (ganho) EcoColors.Verde else Color(0xFF666666)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CarteiraScreenPreview() {
    EcowayerTheme { CarteiraScreen() }
}