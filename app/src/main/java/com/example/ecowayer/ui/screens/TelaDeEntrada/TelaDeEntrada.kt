package com.example.ecowayer.ui.screens.home

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

/**
 * Modelo temporário. Quando a API estiver pronta, crie esta classe a partir do
 * JSON retornado (ex.: data class EcoPontoDto) e mova para a pasta data/model.
 */
data class EcoPonto(
    val nome: String,
    val materiais: List<String>,
    val aberto: Boolean,
    val distancia: String
)

private val filtros = listOf("Todos", "PET", "Vidro", "Papel", "Metal")

@Composable
fun HomeScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onBuscarEcoPontos: () -> Unit = {},
    onVerExtrato: () -> Unit = {},
    onTrocarPontos: () -> Unit = {},
    onIrParaEcoPonto: (EcoPonto) -> Unit = {}
) {
    var filtroSelecionado by remember { mutableStateOf("Todos") }

    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // Troque os dados abaixo por estados vindos de um HomeViewModel:
    //
    // val viewModel: HomeViewModel = viewModel()
    // val usuario by viewModel.usuario.collectAsState()       // GET /usuarios/me
    // val saldo by viewModel.saldo.collectAsState()           // GET /usuarios/me/saldo
    // val ecoPontos by viewModel.ecoPontos.collectAsState()   // GET /ecopontos
    //
    // LaunchedEffect(filtroSelecionado) {
    //     // GET /ecopontos?material=PET&lat=...&lng=...   ("Todos" = sem filtro)
    //     viewModel.carregarEcoPontos(filtroSelecionado)
    // }
    // ----------------------------------------------------------------------
    val nomeUsuario = "Lucas Mendes"   // TODO API
    val saldo = "3.480"                // TODO API
    val ecoPontosMock = listOf(        // TODO API
        EcoPonto("EcoPonto Jardim Sul", listOf("PET", "Vidro", "Papel"), true, "450 m")
    )
    val ecoPontos = ecoPontosMock.filter {
        filtroSelecionado == "Todos" || filtroSelecionado in it.materiais
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraInferior(
                selecionado = DestinoBarra.Inicio,
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
            // Cabeçalho
            item { Cabecalho(nomeUsuario) }

            // Saldo
            item { CardSaldo(saldo, onVerExtrato, onTrocarPontos) }

            // Mapa (Mapbox)
            item { MapaEcoPontos(onBuscarEcoPontos) }

            // Filtros
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtros) { filtro ->
                        ChipFiltro(
                            texto = filtro,
                            selecionado = filtro == filtroSelecionado,
                            onClick = { filtroSelecionado = filtro }
                        )
                    }
                }
            }

            // Título da lista
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pontos disponíveis", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, Color(0xFFDDDDDD), CircleShape)
                            .clip(CircleShape)
                            .clickable { /* TODO: abrir busca */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Search, "Buscar", tint = EcoColors.TextoPrimario)
                    }
                }
            }

            // Cards dos EcoPontos (vindos da API)
            items(ecoPontos) { ponto ->
                CardEcoPonto(ponto, onIr = { onIrParaEcoPonto(ponto) })
            }
        }
    }
}

@Composable
private fun Cabecalho(nome: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // FOTO DO USUÁRIO ---------------------------------------------------
        // Quando a API devolver a URL da foto (usando Coil: io.coil-kt:coil-compose):
        //
        // AsyncImage(
        //     model = usuario.fotoUrl,
        //     contentDescription = "Foto de perfil",
        //     modifier = Modifier.size(56.dp).clip(CircleShape)
        // )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        )
        // -------------------------------------------------------------------
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text("Olá, bem-vindo!", fontSize = 13.sp, color = Color(0xFF555555))
            Text(nome, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CardSaldo(
    saldo: String,
    onVerExtrato: () -> Unit,
    onTrocarPontos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(EcoColors.Roxo)
            .padding(18.dp)
    ) {
        Text("Saldo ecológico disponível", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        ) {
            Text(saldo, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                " pts",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotaoSaldo("Ver Extrato", onVerExtrato, Modifier.weight(1f))
            BotaoSaldo("Trocar pontos", onTrocarPontos, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BotaoSaldo(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun MapaEcoPontos(onBuscarEcoPontos: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE5ECE4))
    ) {
        // MAPA (Mapbox) ----------------------------------------------------
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(-46.6333, -23.5505))  // TODO: localização real
                    zoom(16.0)
                }
            },
            logo = {},       // esconde a logo do Mapbox
            scaleBar = {}    // esconde a escala
        )

        // Camada transparente: o mapa vira uma prévia clicável e não briga
        // com a rolagem da tela
        Box(
            Modifier
                .matchParentSize()
                .clickable { onBuscarEcoPontos() }
        )
        // ------------------------------------------------------------------

        // Card "Buscar EcoPontos" sobre o mapa
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { onBuscarEcoPontos() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Buscar EcoPontos", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EcoColors.Verde),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Search, null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun ChipFiltro(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selecionado) EcoColors.Verde else Color.White)
            .border(1.dp, if (selecionado) EcoColors.Verde else Color(0xFFDDDDDD), CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selecionado) Color.White else Color(0xFF444444)
        )
    }
}

@Composable
private fun CardEcoPonto(ponto: EcoPonto, onIr: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EcoColors.Verde),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Recycling, null, tint = Color.White, modifier = Modifier.size(26.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(ponto.nome, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                ponto.materiais.joinToString(" · "),
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (ponto.aberto) "Aberto agora" else "Fechado",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (ponto.aberto) EcoColors.Verde else Color(0xFF9E9E9E))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
                Text(ponto.distancia, fontSize = 12.sp, color = Color(0xFF666666))
            }
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(EcoColors.Verde)
                .clickable { onIr() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.NearMe, "Ir até o EcoPonto", tint = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EcowayerTheme { HomeScreen() }
}