package com.example.ecowayer.ui.screens.trocapontos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.BeneficioCatalogo
import com.example.ecowayer.model.VoucherCarteiraApi
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.components.ImagemRemota
import com.example.ecowayer.ui.theme.EcoColors
import java.text.NumberFormat
import java.util.Locale

private val FundoTroca = Color(0xFFF7F6FC)
private val VerdeTroca = Color(0xFF347938)
private val BordaFiltro = Color(0xFFDBE2DC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrocarPontosScreen(
    onVoltar: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    onQrCode: () -> Unit,
    viewModel: TrocarPontosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var filtroSelecionado by rememberSaveable { mutableStateOf<String?>(null) }
    var beneficioId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selecionado = estado.beneficios.firstOrNull { it.id == beneficioId }
    val filtros = (estado.beneficios.map { it.nome_parceiro } + estado.giftCards.map { it.categoria })
        .filter(String::isNotBlank).distinct()
    val filtroAtivo = filtroSelecionado?.takeIf { it in filtros }
    val beneficios = estado.beneficios.filter { filtroAtivo == null || it.nome_parceiro == filtroAtivo }
    val giftCards = estado.giftCards.filter { filtroAtivo == null || it.categoria == filtroAtivo }

    BackHandler(enabled = estado.enviando) {}
    Scaffold(
        containerColor = FundoTroca,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = VerdeTroca)) { append("Trocar ") }
                                withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Pontos") }
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                    navigationIcon = {
                        IconButton(onClick = onVoltar, enabled = !estado.enviando) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                        }
                    }
                )
                HorizontalDivider(color = Color(0xFFECEAF3))
            }
        },
        bottomBar = {
            BarraInferior(
                selecionado = null,
                onSelecionar = { if (!estado.enviando) onNavegar(it) },
                onQrCode = { if (!estado.enviando) onQrCode() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            estado.saldo?.let { saldo ->
                item {
                    Text(
                        "Saldo disponível: ${saldo.formatarPontos()} pts",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 14.sp,
                        color = VerdeTroca,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (filtros.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { FiltroTroca("Tudo", filtroAtivo == null) { filtroSelecionado = null } }
                        items(filtros, key = { it }) { nome ->
                            FiltroTroca(nome, nome == filtroAtivo) { filtroSelecionado = nome }
                        }
                    }
                }
            }
            if (estado.carregando && beneficios.isEmpty() && giftCards.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            estado.erroCatalogo?.let { erro ->
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(erro)
                        TextButton(onClick = viewModel::carregar) { Text("Tentar novamente") }
                    }
                }
            }
            if (estado.erroGiftCards) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text("Não foi possível carregar os gift cards.")
                        if (estado.erroCatalogo == null) {
                            TextButton(onClick = viewModel::carregar) { Text("Tentar novamente") }
                        }
                    }
                }
            }
            if (!estado.carregando && estado.erroCatalogo == null && !estado.erroGiftCards &&
                beneficios.isEmpty() && giftCards.isEmpty()
            ) {
                item { Text("Nenhum benefício disponível agora.", Modifier.padding(horizontal = 16.dp)) }
            }
            items(beneficios, key = { "beneficio_${it.id}" }) { item ->
                val podeResgatar = !estado.carregando && !estado.enviando &&
                    estado.saldo?.let { it >= item.pontos_necessarios } == true
                CardBeneficio(item, podeResgatar) {
                    viewModel.fecharAviso()
                    beneficioId = item.id
                }
            }
            items(giftCards, key = { "gift_card_${it.id}" }) { item ->
                CardGiftCard(item)
            }
        }
    }

    val voucher = estado.voucher
    if (voucher != null) {
        AlertDialog(
            onDismissRequest = { beneficioId = null; viewModel.fecharAviso() },
            title = { Text("Benefício resgatado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Código do voucher", fontSize = 14.sp)
                    SelectionContainer {
                        Text(voucher.codigo, fontWeight = FontWeight.Bold)
                    }
                    voucher.data_expiracao?.take(10)?.split('-')?.takeIf { it.size == 3 }?.let {
                        Text("Válido até ${it.asReversed().joinToString("/")}")
                    }
                    estado.saldo?.let { Text("Saldo restante: ${it.formatarPontos()} pts") }
                }
            },
            confirmButton = {
                TextButton(onClick = { beneficioId = null; viewModel.fecharAviso() }) { Text("Concluir") }
            }
        )
    } else if (selecionado != null) {
        val saldo = estado.saldo
        val saldoInsuficiente = saldo != null && saldo < selecionado.pontos_necessarios
        AlertDialog(
            onDismissRequest = { if (!estado.enviando) beneficioId = null },
            title = { Text("Confirmar resgate") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(selecionado.beneficio, fontWeight = FontWeight.SemiBold)
                    Text("Custo: ${selecionado.pontos_necessarios.formatarPontos()} pts")
                    Text("Válido por ${selecionado.validade_dias} dias após o resgate.")
                    if (saldoInsuficiente) Text("Saldo insuficiente para resgatar este benefício.")
                    estado.erroResgate?.let { Text(it, color = Color(0xFFBA1A1A)) }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resgatar(selecionado) },
                    enabled = saldo != null && !saldoInsuficiente && !estado.carregando && !estado.enviando
                ) {
                    Text(if (estado.enviando) "Resgatando…" else "Confirmar resgate")
                }
            },
            dismissButton = {
                TextButton(onClick = { beneficioId = null }, enabled = !estado.enviando) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FiltroTroca(rotulo: String, ativo: Boolean, onClick: () -> Unit) {
    Text(
        text = rotulo,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (ativo) VerdeTroca else Color.White)
            .border(1.dp, if (ativo) VerdeTroca else BordaFiltro, CircleShape)
            .selectable(selected = ativo, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = if (ativo) Color.White else Color(0xFF546258),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun CardBeneficio(item: BeneficioCatalogo, habilitado: Boolean, onResgatar: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF0EEF4), RoundedCornerShape(22.dp))
            .padding(start = 14.dp, top = 14.dp, end = 10.dp, bottom = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE9F4E7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CardGiftcard, null, tint = VerdeTroca, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.nome_parceiro, fontSize = 12.sp, color = Color(0xFF595363))
                Text(item.beneficio, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoColors.TextoPrimario)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Schedule, null, Modifier.size(13.dp), tint = Color(0xFF595363))
                    Text("${item.validade_dias} dias", fontSize = 12.sp, color = Color(0xFF595363))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("${item.pontos_necessarios.formatarPontos()} pts", color = VerdeTroca, fontWeight = FontWeight.Bold)
            BotaoResgate("Resgatar", habilitado, onResgatar)
        }
    }
}

@Composable
private fun CardGiftCard(item: VoucherCarteiraApi) {
    val forma = RoundedCornerShape(22.dp)
    val valor = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(item.valor_reais)
    Column(
        Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(forma)
            .background(Color.White).border(1.dp, Color(0xFFF0EEF4), forma).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val miniatura = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp))
            if (item.imagem_url.isNullOrBlank()) {
                Box(miniatura.background(Color(0xFFE9F4E7)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.CardGiftcard, null, tint = VerdeTroca, modifier = Modifier.size(28.dp))
                }
            } else {
                ImagemRemota(item.imagem_url, "Imagem do gift card", miniatura)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(item.categoria, fontSize = 12.sp, color = Color(0xFF595363))
                Text(item.nome, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoColors.TextoPrimario)
                Text(valor, fontSize = 13.sp, color = Color(0xFF595363))
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${item.pontos_resgate.formatarPontos()} pts", color = VerdeTroca, fontWeight = FontWeight.Bold)
            BotaoResgate("Resgate indisponível", habilitado = false, onClick = {})
        }
    }
}

@Composable
private fun BotaoResgate(rotulo: String, habilitado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        border = BorderStroke(1.dp, EcoColors.Roxo),
        colors = ButtonDefaults.buttonColors(
            containerColor = EcoColors.Roxo,
            contentColor = Color.White,
            disabledContainerColor = Color.White,
            disabledContentColor = EcoColors.Roxo
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(rotulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        if (habilitado) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(start = 6.dp).size(16.dp))
        }
    }
}

private fun Int.formatarPontos() = NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(this)
