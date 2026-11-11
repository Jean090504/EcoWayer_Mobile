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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

/**
 * Modelos temporários. Quando a API estiver pronta, gere estas classes a partir
 * do JSON retornado e mova para a pasta data/model.
 */
data class NivelImpacto(
    val nome: String,              // ex.: "Sustentador"
    val numero: Int,               // ex.: 4
    val progresso: Float,          // 0f..1f
    val textoProximoNivel: String  // ex.: "Próximo nível com mais 150 kg de resíduos evitados."
)

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
    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // Troque os dados abaixo por estados vindos de um InsigniasViewModel:
    //
    // val viewModel: InsigniasViewModel = viewModel()
    // val nivel by viewModel.nivel.collectAsState()           // GET /usuarios/me/nivel
    // val insignias by viewModel.insignias.collectAsState()   // GET /usuarios/me/insignias
    //
    // LaunchedEffect(Unit) { viewModel.carregar() }
    //
    // Trate também carregando e erro (CircularProgressIndicator / "Tentar de novo").
    // ----------------------------------------------------------------------
    val nivel = NivelImpacto(                                         // TODO API
        nome = "Sustentador",
        numero = 4,
        progresso = 0.7f,
        textoProximoNivel = "Próximo nível com mais 150 kg de resíduos evitados."
    )
    val insignias = listOf(                                           // TODO API
        Insignia(1, "Pioneiro", true, cor = Color(0xFF4CAF50), icone = Icons.Outlined.WorkspacePremium),
        Insignia(
            2, "Mestre do Vidro", false, progresso = 0.4f, faltaTexto = "Faltam 12 kg",
            cor = Color(0xFF6B8FDB), icone = Icons.Outlined.TrackChanges
        ),
        Insignia(3, "Sócio-Parceiro", true, cor = Color(0xFF6A4FBF), icone = Icons.Outlined.FavoriteBorder)
    )

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
                .statusBarsPadding()
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
                // Nível de impacto (ocupa a linha toda)
                item(span = { GridItemSpan(maxLineSpan) }) { CardNivel(nivel) }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Insígnias Ativas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
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
private fun CardNivel(nivel: NivelImpacto) {
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

        Box(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE6E6E6))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(nivel.progresso.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(EcoColors.Verde)
            )
        }

        Text(nivel.textoProximoNivel, fontSize = 12.sp, color = Color(0xFF555555))
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