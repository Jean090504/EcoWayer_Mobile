package com.example.ecowayer.ui.screens.ecopontos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.R
import com.example.ecowayer.data.model.PontoColetaDto
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.components.FiltrosState
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions

// Posição usada enquanto o GPS ainda não respondeu (Mapbox usa lng, lat)
private const val LAT_PADRAO = -23.5505
private const val LNG_PADRAO = -46.6333
private const val ZOOM_INICIAL = 15.5
private val ALTURA_CARD_PONTO = 170.dp

// Câmera que segue o usuário SEM inclinação (sem 3D) e com o norte sempre para cima
private val OPCOES_SEGUIR_USUARIO = FollowPuckViewportStateOptions.Builder()
    .pitch(0.0)
    .bearing(FollowPuckViewportStateBearing.Constant(0.0))
    .zoom(ZOOM_INICIAL)
    .build()

// ---------------------------------------------------------------------------
// TELA
// ---------------------------------------------------------------------------

@Composable
fun EcoPontosScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onSelecionarPonto: (PontoColetaDto) -> Unit = {},
    viewModel: EcoPontosViewModel = viewModel()
) {
    val contexto = LocalContext.current

    // Estado da UI
    var busca by remember { mutableStateOf("") }
    var filtros by remember { mutableStateOf(FiltrosState()) }
    var mostrarFiltros by remember { mutableStateOf(false) }
    var selecionado by remember { mutableStateOf<PontoColetaDto?>(null) }
    var posicaoUsuario by remember { mutableStateOf<Point?>(null) }

    // Dados da API (GET /meraki/ecowayer/admin/ponto-coleta)
    val todosPontos by viewModel.pontos.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val pontos = remember(todosPontos, busca, filtros) {
        todosPontos.filter {
            it.coincideComBusca(busca) &&
                    it.aceita(filtros.materiais.map { m -> m.valorApi })   // ajuste ao seu FiltrosState
        }
    }

    // Permissão de localização
    val (temPermissao, pedirPermissao) = rememberPermissaoLocalizacao()

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(LNG_PADRAO, LAT_PADRAO))
            zoom(ZOOM_INICIAL)
            pitch(0.0)
            bearing(0.0)
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            TituloEcoPontos()

            CampoBusca(
                valor = busca,
                onValorChange = { busca = it },
                onAbrirFiltros = { mostrarFiltros = true }
            )

            erro?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color(0xFFB00020),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { viewModel.carregar() }   // toque para tentar de novo
                )
            }

            MapaEcoPontos(
                pontos = pontos,
                selecionado = selecionado,
                posicaoUsuario = posicaoUsuario,
                temPermissao = temPermissao,
                mapViewportState = mapViewportState,
                onPosicaoUsuario = { posicaoUsuario = it },
                onSelecionar = {
                    selecionado = it
                    onSelecionarPonto(it)
                },
                onFecharCard = { selecionado = null },
                onRecentralizar = {
                    if (temPermissao) {
                        mapViewportState.transitionToFollowPuckState(OPCOES_SEGUIR_USUARIO)
                    } else {
                        pedirPermissao()
                    }
                },
                onTracarRota = { abrirRotaNoMaps(contexto, it) },
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .weight(1f)
            )
        }

        if (mostrarFiltros) {
            FiltrosBottomSheet(
                filtrosAtuais = filtros,
                onFechar = { mostrarFiltros = false },
                onAplicar = { novos ->
                    filtros = novos
                    selecionado = null
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// PERMISSÃO
// ---------------------------------------------------------------------------

/** Retorna (temPermissao, função para pedir a permissão). Já pede ao entrar na tela. */
@Composable
private fun rememberPermissaoLocalizacao(): Pair<Boolean, () -> Unit> {
    val contexto = LocalContext.current
    var temPermissao by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                contexto, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { temPermissao = it }

    LaunchedEffect(Unit) {
        if (!temPermissao) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    return temPermissao to { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
}

private fun abrirRotaNoMaps(contexto: Context, ponto: PontoColetaDto) {
    val uri = Uri.parse("google.navigation:q=${ponto.latitude},${ponto.longitude}")
    val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
    contexto.startActivity(intent)
}

// ---------------------------------------------------------------------------
// MAPA
// ---------------------------------------------------------------------------

@Composable
private fun MapaEcoPontos(
    pontos: List<PontoColetaDto>,
    selecionado: PontoColetaDto?,
    posicaoUsuario: Point?,
    temPermissao: Boolean,
    mapViewportState: MapViewportState,
    onPosicaoUsuario: (Point) -> Unit,
    onSelecionar: (PontoColetaDto) -> Unit,
    onFecharCard: () -> Unit,
    onRecentralizar: () -> Unit,
    onTracarRota: (PontoColetaDto) -> Unit,
    modifier: Modifier = Modifier
) {
    // Listener único: evita adicionar um novo a cada vez que o MapEffect roda
    val onPosicaoAtual by rememberUpdatedState(onPosicaoUsuario)
    val listenerPosicao = remember {
        OnIndicatorPositionChangedListener { onPosicaoAtual(it) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { GenericStyle(style = Style.LIGHT) },   // mapa claro (veja nota sobre estilo branco)
            logo = {},
            scaleBar = {}
        ) {
            // Configuração 2D: sem inclinação, sem rotação, sem prédios em 3D
            MapEffect(Unit) { mapView ->
                mapView.gestures.pitchEnabled = false
                mapView.gestures.rotateEnabled = false

                mapView.mapboxMap.getStyle { style ->
                    listOf("building-extrusion", "3d-buildings").forEach { id ->
                        if (style.styleLayerExists(id)) style.removeStyleLayer(id)
                    }
                }
            }

            // Bolinha azul + câmera na localização do usuário
            MapEffect(temPermissao) { mapView ->
                if (temPermissao) {
                    mapView.location.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                        locationPuck = createDefault2DPuck(withBearing = false)  // bolinha 2D
                    }
                    mapView.location.removeOnIndicatorPositionChangedListener(listenerPosicao)
                    mapView.location.addOnIndicatorPositionChangedListener(listenerPosicao)
                    mapViewportState.transitionToFollowPuckState(OPCOES_SEGUIR_USUARIO)
                }
            }

            MarcadoresEcoPontos(
                pontos = pontos,
                onClick = { ponto ->
                    onSelecionar(ponto)
                    mapViewportState.easeTo(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(ponto.longitude, ponto.latitude))
                            .pitch(0.0)
                            .bearing(0.0)
                            .build()
                    )
                }
            )
        }

        BotaoMinhaLocalizacao(
            onClick = onRecentralizar,
            elevado = selecionado != null
        )

        selecionado?.let { ponto ->
            val origem = posicaoUsuario
            val distancia = distanciaMetros(
                origem?.latitude() ?: LAT_PADRAO,
                origem?.longitude() ?: LNG_PADRAO,
                ponto.latitude,
                ponto.longitude
            ).formatarDistancia()

            CardPontoSelecionado(
                ponto = ponto,
                distancia = distancia,
                onFechar = onFecharCard,
                onTracarRota = { onTracarRota(ponto) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
@MapboxMapComposable
private fun MarcadoresEcoPontos(
    pontos: List<PontoColetaDto>,
    onClick: (PontoColetaDto) -> Unit
) {
    // Drawables em res/drawable: marcador_verde, marcador_azul, marcador_laranja, marcador_cinza
    val verde = rememberIconImage("marcador_verde", painterResource(R.drawable.marcador_verde))
    val azul = rememberIconImage("marcador_azul", painterResource(R.drawable.marcador_azul))
    val laranja = rememberIconImage("marcador_laranja", painterResource(R.drawable.marcador_laranja))
    val cinza = rememberIconImage("marcador_cinza", painterResource(R.drawable.marcador_cinza))

    pontos.forEach { ponto ->
        val icone = when (ponto.categoria) {
            "Plástico" -> verde
            "Vidro" -> azul
            "Papel" -> laranja
            else -> cinza   // Metal, Eletrônicos, Óleo de Cozinha...
        }
        PointAnnotation(point = Point.fromLngLat(ponto.longitude, ponto.latitude)) {
            iconImage = icone
            interactionsState.onClicked {
                onClick(ponto)
                true
            }
        }
    }
}

@Composable
private fun BoxScope.BotaoMinhaLocalizacao(onClick: () -> Unit, elevado: Boolean) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 14.dp,
                bottom = if (elevado) ALTURA_CARD_PONTO else 14.dp   // sobe quando o card aparece
            )
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFEEF2EC))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Outlined.NearMe,
            contentDescription = "Minha localização",
            tint = EcoColors.Verde
        )
    }
}

// ---------------------------------------------------------------------------
// COMPONENTES
// ---------------------------------------------------------------------------

@Composable
private fun TituloEcoPontos() {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = EcoColors.Verde)) { append("Eco") }
            withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Pontos") }
        },
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )
}

@Composable
private fun CardPontoSelecionado(
    ponto: PontoColetaDto,
    distancia: String,
    onFechar: () -> Unit,
    onTracarRota: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ponto.nome, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(ponto.enderecoCurto(), fontSize = 12.sp, color = Color(0xFF666666))
            }
            Text(distancia, fontSize = 11.sp, color = Color(0xFF666666))
            Text(
                text = "✕",
                fontSize = 16.sp,
                color = Color(0xFF777777),
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable { onFechar() }
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ponto.materialAceito.take(4).forEach { material ->
                ChipMaterial(material.nome)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EcoColors.Verde)
                .clickable { onTracarRota() },
            contentAlignment = Alignment.Center
        ) {
            Text("Traçar rota", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChipMaterial(texto: String) {
    Text(
        text = texto,
        fontSize = 10.sp,
        color = EcoColors.Verde,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(EcoColors.Verde.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun CampoBusca(
    valor: String,
    onValorChange: (String) -> Unit,
    onAbrirFiltros: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, null, tint = Color(0xFF777777), modifier = Modifier.size(22.dp))

        BasicTextField(
            value = valor,
            onValueChange = onValorChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = EcoColors.TextoPrimario),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            decorationBox = { campo ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (valor.isEmpty()) {
                        Text(
                            "Buscar ponto de coleta, bairro...",
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )
                    }
                    campo()
                }
            }
        )

        Box(
            Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color(0xFFE0E0E0))
        )
        Icon(
            Icons.Outlined.Tune,
            contentDescription = "Filtros",
            tint = EcoColors.TextoPrimario,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(22.dp)
                .clickable { onAbrirFiltros() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EcoPontosScreenPreview() {
    EcowayerTheme { EcoPontosScreen() }
}