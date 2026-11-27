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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.BuildConfig
import com.example.ecowayer.model.PontoColeta
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.components.ImagemRemota
import com.example.ecowayer.ui.ecopontos.EcoPontosViewModel
import com.example.ecowayer.ui.screens.carteira.CarteiraViewModel
import com.example.ecowayer.ui.screens.carteira.BotaoExtratoPontos
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.NumberFormat
import java.util.Locale

private val RoxoHome = Color(0xFF7045BC)
private val VerdeHome = Color(0xFF48A64E)
private val BordaHome = Color(0xFFDCE3DC)
private val TextoSecundario = Color(0xFF58665C)

private val filtros = listOf("Todos", "PET", "Vidro", "Papel", "Metal")

@Composable
fun HomeScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onBuscarEcoPontos: () -> Unit = {},
    onVerExtrato: () -> Unit = {},
    onTrocarPontos: () -> Unit = {},
    onAbrirEco: () -> Unit = {},
    onIrParaEcoPonto: (PontoColeta) -> Unit = {}
) {
    var filtroSelecionado by rememberSaveable { mutableStateOf("Todos") }
    val preview = LocalInspectionMode.current
    val usuario = if (preview) null else SessionStore.currentUser()
    val pontosViewModel: EcoPontosViewModel? = if (preview) null else viewModel()
    val carteiraViewModel: CarteiraViewModel? = if (preview || usuario == null) null else viewModel()
    val pontosApi = pontosViewModel?.pontos?.collectAsState()?.value.orEmpty()
    val carregandoPontos = pontosViewModel?.carregando?.collectAsState()?.value == true
    val erroPontos = pontosViewModel?.erro?.collectAsState()?.value
    val carteira = carteiraViewModel?.estado?.collectAsState()?.value
    val saldo = carteira?.carteira?.resumo?.pontos?.let {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(it)
    }
    val ecoPontos = pontosApi.filter { ponto ->
        ponto.status.equals("ativo", ignoreCase = true) &&
            (filtroSelecionado == "Todos" || ponto.materiais.any {
                it.nome.contains(filtroSelecionado, ignoreCase = true)
            })
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
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Cabecalho(usuario?.nome ?: "Usuário EcoWayer", usuario?.foto, onAbrirEco) }

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

            item {
                CardSaldo(saldo, carteira?.carregando == true, onVerExtrato, onTrocarPontos)
            }

            item { MapaEcoPontos(ecoPontos, onBuscarEcoPontos) }

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
                            .clickable(onClick = onBuscarEcoPontos),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Search, "Buscar", tint = EcoColors.TextoPrimario)
                    }
                }
            }

            if (ecoPontos.isEmpty()) {
                item {
                    Text(
                        when {
                            carregandoPontos -> "Carregando EcoPontos…"
                            erroPontos != null -> "Não foi possível carregar os EcoPontos."
                            pontosApi.isEmpty() -> "Nenhum EcoPonto disponível no momento."
                            else -> "Nenhum EcoPonto encontrado para este material."
                        },
                        color = TextoSecundario,
                        fontSize = 14.sp
                    )
                    if (erroPontos != null) {
                        TextButton(onClick = { pontosViewModel?.carregar() }) { Text("Tentar novamente") }
                    }
                }
            }
            items(ecoPontos, key = { it.id }) { ponto ->
                CardEcoPonto(ponto, onIr = { onIrParaEcoPonto(ponto) })
            }
        }
    }
}

@Composable
private fun Cabecalho(nome: String, foto: String?, onAbrirEco: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        val avatar = Modifier.size(54.dp).clip(CircleShape)
        if (foto.isNullOrBlank()) {
            Box(avatar.background(Color(0xFFEAF4EA)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Person, null, tint = VerdeHome)
            }
        } else {
            ImagemRemota(foto, "Foto de perfil", avatar)
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text("Olá, bem-vindo!", fontSize = 14.sp, color = TextoSecundario)
            Text(nome, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = EcoColors.TextoPrimario)
        }
        }
        IconButton(onClick = onAbrirEco, modifier = Modifier.size(48.dp).background(Color(0xFFF1EBFA), CircleShape)) {
            Icon(Icons.Outlined.SmartToy, contentDescription = "Conversar com a Eco", tint = RoxoHome)
        }
    }
}

@Composable
private fun CardSaldo(
    saldo: String?,
    carregando: Boolean,
    onVerExtrato: () -> Unit,
    onTrocarPontos: () -> Unit
) {
    var mostrarSaldo by rememberSaveable { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFDEFADD), Color.White, Color(0xFFF5EFFF))))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Outlined.AccountBalanceWallet, null, tint = RoxoHome)
                Text("Saldo acumulado", color = RoxoHome, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = { mostrarSaldo = !mostrarSaldo }, enabled = saldo != null) {
                Icon(
                    if (mostrarSaldo) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    if (mostrarSaldo) "Ocultar saldo" else "Mostrar saldo",
                    tint = RoxoHome
                )
            }
        }
        if (saldo == null) {
            Text(
                if (carregando) "Carregando saldo…" else "Saldo indisponível",
                color = TextoSecundario,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 14.dp)) {
                Text(
                    if (mostrarSaldo) saldo else "••••",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.TextoPrimario
                )
                Text(" pts", fontSize = 16.sp, color = VerdeHome, modifier = Modifier.padding(bottom = 5.dp))
            }
        }
        Button(
            onClick = onTrocarPontos,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoxoHome, contentColor = Color.White)
        ) {
            Icon(Icons.Outlined.CardGiftcard, null, modifier = Modifier.size(19.dp))
            Text("Trocar pontos", Modifier.padding(start = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        BotaoExtratoPontos(onVerExtrato, Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun MapaEcoPontos(pontos: List<PontoColeta>, onBuscarEcoPontos: () -> Unit) {
    val preview = LocalInspectionMode.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE5ECE4))
    ) {
        if (preview || BuildConfig.GOOGLE_MAPS_API_KEY.isBlank()) {
            Icon(
                Icons.Outlined.Map,
                contentDescription = null,
                tint = Color(0xFF739079),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 25.dp).size(52.dp)
            )
        } else {
            val ponto = pontos.firstOrNull()
            val centro = ponto?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(-23.5505, -46.6333)
            val camera = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(centro, 13f)
            }
            LaunchedEffect(centro) {
                camera.position = CameraPosition.fromLatLngZoom(centro, 13f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false
                )
            ) {
                pontos.forEach { ponto ->
                    key(ponto.id) {
                        val posicao = LatLng(ponto.latitude, ponto.longitude)
                        Marker(state = remember(posicao) { MarkerState(position = posicao) }, title = ponto.nome)
                    }
                }
            }
        }

        // Camada transparente: o mapa vira uma prévia clicável e não briga
        // com a rolagem da tela
        Box(
            Modifier
                .matchParentSize()
                .clickable { onBuscarEcoPontos() }
        )

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
                    .background(VerdeHome),
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
            .background(if (selecionado) RoxoHome else Color.White)
            .border(1.dp, if (selecionado) RoxoHome else BordaHome, CircleShape)
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
private fun CardEcoPonto(ponto: PontoColeta, onIr: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BordaHome, RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VerdeHome),
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
                ponto.materiais.joinToString(" · ") { it.nome },
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(VerdeHome)
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
