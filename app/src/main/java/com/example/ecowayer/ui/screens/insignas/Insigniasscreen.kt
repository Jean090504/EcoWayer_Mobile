package com.example.ecowayer.ui.screens.insignias

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.NivelImpactoApi
import com.example.ecowayer.model.ResumoCarteiraApi
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.screens.carteira.CarteiraViewModel
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import java.text.NumberFormat
import java.util.Locale

data class Insignia(
    val id: Int,
    val nome: String,
    val concluida: Boolean,
    val progresso: Float = 1f,     // usado quando não concluída
    val faltaTexto: String? = null,// ex.: "Faltam 12 kg"
    val cor: Color,                // a API pode devolver em hex (ex.: "#4CAF50")
    val icone: ImageVector         // a API pode devolver um código (ex.: "medalha") e você mapeia
)

@Composable
fun InsigniasScreen(
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onAbrirInsignia: (Insignia) -> Unit = {}
) {
    val carteiraViewModel: CarteiraViewModel? = if (LocalInspectionMode.current) null else viewModel()
    val carteira = carteiraViewModel?.estado?.collectAsState()?.value
    val resumo = carteira?.carteira?.resumo
    val nivel = resumo?.nivel_atual
    val insigniasViewModel: InsigniasViewModel? = if (LocalInspectionMode.current) null else viewModel()
    val estadoInsignias = insigniasViewModel?.estado?.collectAsState()?.value
    val progressoPorId = estadoInsignias?.progresso?.associateBy { it.id_insignia }.orEmpty()
    val cores = listOf(Color(0xFF4CAF50), Color(0xFF6B8FDB), Color(0xFF6A4FBF))
    val icones = listOf(Icons.Outlined.WorkspacePremium, Icons.Outlined.TrackChanges, Icons.Outlined.FavoriteBorder)
    val insignias = estadoInsignias?.catalogo.orEmpty().mapIndexed { indice, item ->
        val progresso = progressoPorId[item.id]
        val concluida = progresso?.status.equals("concluido", ignoreCase = true)
        val percentual = (progresso?.progresso ?: 0f).coerceIn(0f, 100f)
        Insignia(item.id, item.nome, concluida, percentual / 100f,
            if (concluida) null else "Progresso: ${percentual.toInt()}%",
            cores[indice % cores.size], icones[indice % icones.size])
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            // Continua no Perfil, de onde esta tela foi aberta
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
        ) {
            // Cabeçalho com voltar
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
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Minhas ") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Insígnias") }
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    when {
                        nivel != null -> CardNivel(nivel, resumo)
                        carteira?.carregando == true -> {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                            }
                        }
                        carteira?.erro != null -> {
                            Column {
                                Text("Não foi possível carregar seu nível de impacto.")
                                TextButton(onClick = { carteiraViewModel?.carregar() }) { Text("Tentar novamente") }
                            }
                        }
                        else -> Text("Nível de impacto indisponível.")
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Insígnias",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }

                if (estadoInsignias?.carregando == true) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        }
                    }
                } else if (estadoInsignias?.erro != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Text(estadoInsignias?.erro.orEmpty())
                            TextButton(onClick = { insigniasViewModel?.carregar() }) { Text("Tentar novamente") }
                        }
                    }
                } else if (insignias.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Text("Nenhuma insígnia disponível.") }
                }

                // Insígnias (3 por linha)
                items(insignias) { insignia ->
                    CardInsignia(insignia, onClick = { onAbrirInsignia(insignia) })
                }
            }
        }
    }
}

@Composable
private fun CardNivel(nivel: NivelImpactoApi, resumo: ResumoCarteiraApi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EcoColors.Verde, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Nível de Impacto: ${nivel.nome}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Verde
            )
            Text("Nível ${nivel.numero}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        val proximo = resumo.proximo_nivel
        if (proximo == null) {
            Text("Você atingiu o nível máximo.", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        } else {
            val unidade = proximo.unidade.trim()
            val meta = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).format(proximo.meta)
            Text(
                "Próximo nível: ${proximo.nome} · Meta: $meta $unidade",
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
                color = Color(0xFF555555)
            )
            if (unidade.lowercase() in setOf("pt", "pts", "ponto", "pontos")) {
                Box(
                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth().height(8.dp)
                        .clip(RoundedCornerShape(50)).background(Color(0xFFE6E6E6))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(resumo.progresso.coerceIn(0f, 1f)).fillMaxHeight()
                            .background(EcoColors.Verde)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardInsignia(insignia: Insignia, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(insignia.cor)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ícone em círculo branco
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(insignia.icone, null, tint = insignia.cor, modifier = Modifier.size(24.dp))
        }

        // Nome (altura fixa para alinhar os cards)
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                insignia.nome,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        // Estado: concluída ou em andamento
        if (insignia.concluida) {
            Text("Concluído", fontSize = 10.sp, color = Color.White)
        } else {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 4.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.7f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(insignia.progresso.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(EcoColors.Verde)
                )
            }
            Text(insignia.faltaTexto ?: "", fontSize = 10.sp, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InsigniasScreenPreview() {
    EcowayerTheme { InsigniasScreen() }
}
