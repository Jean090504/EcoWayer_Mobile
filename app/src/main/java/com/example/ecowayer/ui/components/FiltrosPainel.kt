package com.example.ecowayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.theme.EcoColors
import kotlin.math.roundToInt

// ============================================================
// MODELOS
// ============================================================

enum class TipoMaterial(
    val rotulo: String,
    val valorApi: String,
    val icone: ImageVector,
    val cor: Color
) {
    PLASTICO("Plástico", "PLASTICO", Icons.Outlined.WaterDrop, Color(0xFF4A7BE8)),
    PAPEL("Papel", "PAPEL", Icons.Outlined.Description, Color(0xFFEBA23B)),
    VIDRO("Vidro", "VIDRO", Icons.Outlined.Explore, Color(0xFF4DB38A)),
    METAL("Metal", "METAL", Icons.Outlined.Shield, Color(0xFF6B7280)),
    ELETRONICO("Eletrônico", "ELETRONICO", Icons.Outlined.Memory, Color(0xFF8E4A9E)),
    PILHAS("Pilhas & Baterias", "PILHAS_BATERIAS", Icons.Outlined.Bolt, Color(0xFFE05252))
}

data class FiltrosState(
    val materiais: Set<TipoMaterial> = emptySet(),
    val distanciaKm: Int = 5,
    val horario: String = "Aberto agora"
)

private val opcoesHorario = listOf("Aberto agora", "Qualquer horário", "Aberto 24 horas")

// ============================================================
// PAINEL COMPLETO
// ============================================================

@Composable
fun FiltrosPainel(
    filtros: FiltrosState,
    onFiltrosChange: (FiltrosState) -> Unit,
    onFechar: () -> Unit,
    onAplicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth().navigationBarsPadding()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filtrar pontos de coleta",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.TextoPrimario
                )
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Fechar",
                    tint = EcoColors.TextoPrimario,
                    modifier = Modifier.size(28.dp).clickable { onFechar() }
                )
            }

            // Tipos de material
            TituloSecao("Tipos de Material")
            Spacer(Modifier.height(10.dp))

            // --- INTEGRAÇÃO BACK-END (futuro) ---------------------------
            // Se as categorias vierem da API, receba a lista por parâmetro:
            // categorias: List<Categoria>   // GET /categorias -> [{ id, nome }]
            // e faça o forEach em cima dela no lugar de TipoMaterial.entries.
            // ------------------------------------------------------------
            TipoMaterial.entries.forEach { material ->
                val marcado = material in filtros.materiais
                FiltroMaterialItem(
                    rotulo = material.rotulo,
                    icone = material.icone,
                    corIcone = material.cor,
                    marcado = marcado,
                    onClick = {
                        onFiltrosChange(
                            filtros.copy(
                                materiais = if (marcado) filtros.materiais - material
                                else filtros.materiais + material
                            )
                        )
                    }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(10.dp))

            // Distância máxima
            FiltroDistancia(
                valorKm = filtros.distanciaKm,
                onValorChange = { onFiltrosChange(filtros.copy(distanciaKm = it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Horário de funcionamento
            TituloSecao("Horário de Funcionamento")
            Spacer(Modifier.height(8.dp))
            FiltroDropdown(
                selecionado = filtros.horario,
                opcoes = opcoesHorario,
                onSelecionar = { onFiltrosChange(filtros.copy(horario = it)) }
            )

            Spacer(Modifier.height(20.dp))
        }

        // Botão fixo no rodapé
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            BotaoPrimario(texto = "Aplicar filtros", onClick = onAplicar)
        }
    }
}

// ============================================================
// PEÇAS INTERNAS (private: só existem dentro deste arquivo)
// ============================================================

@Composable
private fun TituloSecao(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF555555)
    )
}

@Composable
private fun FiltroMaterialItem(
    rotulo: String,
    icone: ImageVector,
    corIcone: Color,
    marcado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color(0xFFE8E8EC), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(corIcone),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Text(
            text = rotulo,
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (marcado) Modifier.background(EcoColors.Verde)
                    else Modifier.border(2.dp, Color(0xFFE3E3E8), RoundedCornerShape(8.dp))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (marcado) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FiltroDistancia(
    valorKm: Int,
    onValorChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minKm: Int = 2,
    maxKm: Int = 10,
    marcas: List<Int> = listOf(2, 5, 10)
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TituloSecao("Distância Máxima")
            Text(
                text = "$valorKm km",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Verde
            )
        }

        Slider(
            value = valorKm.toFloat(),
            onValueChange = { onValorChange(it.roundToInt()) },
            valueRange = minKm.toFloat()..maxKm.toFloat(),
            steps = maxKm - minKm - 1,
            colors = SliderDefaults.colors(
                thumbColor = EcoColors.Verde,
                activeTrackColor = EcoColors.Verde,
                inactiveTrackColor = Color(0xFFE6E6EA),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            marcas.forEach { Text("${it}km", fontSize = 12.sp, color = Color(0xFF666666)) }
        }
    }
}

@Composable
private fun FiltroDropdown(
    selecionado: String,
    opcoes: List<String>,
    onSelecionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var aberto by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { aberto = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(selecionado, fontSize = 17.sp, color = Color(0xFF888888))
            Icon(Icons.Outlined.KeyboardArrowDown, null, tint = Color(0xFF555555))
        }

        DropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false },
            containerColor = Color.White
        ) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = {
                        onSelecionar(opcao)
                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BotaoPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(EcoColors.Verde)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}