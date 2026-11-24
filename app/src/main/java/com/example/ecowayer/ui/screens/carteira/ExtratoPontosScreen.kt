package com.example.ecowayer.ui.screens.carteira

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.MovimentoCarteiraApi
import com.example.ecowayer.model.ResgateExtratoApi
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import java.text.NumberFormat
import java.util.Locale

private enum class FiltroExtrato(val rotulo: String) {
    Todos("Todos"), Entradas("Entradas"), Resgates("Resgates"), Marketplace("Marketplace")
}

private data class LinhaExtrato(
    val chave: String,
    val titulo: String,
    val data: String?,
    val pontos: Int,
    val tipo: FiltroExtrato,
    val detalhe: String? = null
)

@Composable
internal fun BotaoExtratoPontos(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 46.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = EcoColors.Roxo)
    ) {
        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(19.dp))
        Text("Extrato de pontos", Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ExtratoPontosScreen(
    onBack: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    onQrCode: () -> Unit,
    viewModel: ExtratoPontosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var filtro by rememberSaveable { mutableStateOf(FiltroExtrato.Todos) }
    val movimentos = estado.pontos.orEmpty().map { it.toLinha() } +
        estado.resgates.orEmpty().filter { it.pontos_utilizados > 0 }.map { it.toLinha() }
    val exibidos = movimentos.filter { filtro == FiltroExtrato.Todos || filtro == it.tipo }
        .sortedByDescending { it.data.orEmpty().replace('T', ' ') }
    val falhaVisivel = when (filtro) {
        FiltroExtrato.Resgates -> estado.resgates == null
        FiltroExtrato.Entradas, FiltroExtrato.Marketplace -> estado.pontos == null
        FiltroExtrato.Todos -> estado.pontos == null || estado.resgates == null
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F8),
        topBar = {
            Column(Modifier.fillMaxWidth().background(Color.White).statusBarsPadding()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = EcoColors.TextoPrimario)
                    }
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = EcoColors.Verde)) { append("Extrato de ") }
                            withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Pontos") }
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(color = Color(0xFFE5E5EB))
            }
        },
        bottomBar = { BarraInferior(DestinoBarra.Carteira, onNavegar, onQrCode) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FiltroExtrato.entries) { opcao ->
                        FiltroChip(opcao.rotulo, filtro == opcao) { filtro = opcao }
                    }
                }
            }
            item {
                Text(
                    "Extrato recente de pontos",
                    fontSize = 18.sp,
                    color = EcoColors.TextoPrimario,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
            items(exibidos, key = { it.chave }) { item -> CardMovimento(item) }
            if (estado.carregando && exibidos.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (!estado.carregando && falhaVisivel) {
                item {
                    Text(
                        if (exibidos.isEmpty()) "Não foi possível carregar o extrato."
                        else "Algumas movimentações não puderam ser carregadas."
                    )
                    TextButton(onClick = viewModel::carregar) { Text("Tentar novamente") }
                }
            } else if (!estado.carregando && exibidos.isEmpty()) {
                item { Text("Nenhuma movimentação encontrada.", color = Color(0xFF55515E)) }
            }
        }
    }
}

@Composable
private fun FiltroChip(rotulo: String, selecionado: Boolean, onClick: () -> Unit) {
    Text(
        rotulo,
        modifier = Modifier.clip(CircleShape)
            .background(if (selecionado) EcoColors.Roxo else Color.White)
            .selectable(selected = selecionado, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        color = if (selecionado) Color.White else Color(0xFF494453),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
}

@Composable
private fun CardMovimento(item: LinhaExtrato) {
    val (icone, cor) = when (item.tipo) {
        FiltroExtrato.Resgates -> Icons.Outlined.CardGiftcard to EcoColors.Roxo
        FiltroExtrato.Marketplace -> Icons.Outlined.Storefront to EcoColors.Verde
        else -> Icons.Outlined.Recycling to EcoColors.Verde
    }
    val forma = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth().clip(forma).background(Color.White)
            .border(1.dp, Color(0xFFF0EFF4), forma).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(cor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoColors.TextoPrimario)
            val detalhes = listOfNotNull(item.detalhe?.takeIf(String::isNotBlank), formatarData(item.data))
            if (detalhes.isNotEmpty()) {
                Text(detalhes.joinToString(" • "), fontSize = 12.sp, color = Color(0xFF55515E))
            }
        }
        val valor = NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(item.pontos)
        Text("${if (item.pontos > 0) "+" else ""}$valor pts", color = cor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

private fun MovimentoCarteiraApi.toLinha(): LinhaExtrato {
    val categoria = categoria_nome?.takeIf { it.isNotBlank() }
    val marketplace = categoria?.lowercase() in setOf("marketplace", "comércio", "comercio")
    val titulo = when {
        marketplace -> "Movimentação no Marketplace"
        categoria != null -> "Reciclagem de $categoria"
        id_entrega != null -> "Entrega #$id_entrega"
        qntd_pontos < 0 -> "Débito de pontos"
        else -> "Crédito de pontos"
    }
    return LinhaExtrato(
        "ponto_$id", titulo, data_registro, qntd_pontos,
        if (marketplace) FiltroExtrato.Marketplace else if (qntd_pontos >= 0) FiltroExtrato.Entradas else FiltroExtrato.Todos
    )
}

private fun ResgateExtratoApi.toLinha() = LinhaExtrato(
    "resgate_$id", beneficio?.takeIf { it.isNotBlank() } ?: "Resgate de benefício",
    data_resgate, -pontos_utilizados, FiltroExtrato.Resgates, nome_parceiro
)

private fun formatarData(valor: String?): String? {
    val instante = valor.orEmpty().replace('T', ' ').take(16)
    val data = instante.take(10).split('-')
    if (data.size != 3 || data[0].length != 4) return null
    return data.asReversed().joinToString("/") + instante.drop(10)
}
