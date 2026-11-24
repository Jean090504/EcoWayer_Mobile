package com.example.ecowayer.ui.screens.ecopontos

import android.Manifest
import android.location.Geocoder
import android.content.ActivityNotFoundException
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.Coordenada
import com.example.ecowayer.model.BuscaLocaisRequest
import com.example.ecowayer.model.EnderecoPonto
import com.example.ecowayer.model.LocalReciclagem
import com.example.ecowayer.model.PontoColeta
import com.example.ecowayer.model.RotaCalculada
import com.example.ecowayer.model.RotaRequest
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.components.FiltrosState
import com.example.ecowayer.ui.ecopontos.EcoPontosViewModel
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

// Centro de Barueri-SP. Usado como posição inicial da câmera
// e como origem da distância enquanto o GPS ainda não respondeu.
private const val LAT_PADRAO = -23.5111
private const val LNG_PADRAO = -46.8763
private const val ZOOM_INICIAL = 14.5
private val ALTURA_CARD_PONTO = 170.dp

// ---------------------------------------------------------------------------
// TELA
// ---------------------------------------------------------------------------

@Composable
fun EcoPontosScreen(
    onVoltar: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onSelecionarPonto: (PontoColeta) -> Unit = {},
    viewModel: EcoPontosViewModel = viewModel(),
    pontoInicialId: Int = 0
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    val localizacaoClient = remember(contexto) {
        LocationServices.getFusedLocationProviderClient(contexto)
    }

    // Estado da UI
    var busca by remember { mutableStateOf("") }
    var filtros by remember { mutableStateOf(FiltrosState()) }
    var mostrarFiltros by remember { mutableStateOf(false) }
    var selecionado by remember { mutableStateOf<PontoColeta?>(null) }
    var posicaoUsuario by remember { mutableStateOf<LatLng?>(null) }
    var rota by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var resumoRota by remember { mutableStateOf<RotaCalculada?>(null) }
    var carregandoRota by remember { mutableStateOf(false) }
    var erroRota by remember { mutableStateOf<String?>(null) }
    var erroBusca by remember { mutableStateOf<String?>(null) }
    var locaisReciclagem by remember { mutableStateOf<List<LocalReciclagem>>(emptyList()) }
    var carregandoLocais by remember { mutableStateOf(false) }
    var erroLocais by remember { mutableStateOf<String?>(null) }


    // Dados da API (GET /meraki/ecowayer/user/ponto_coleta)
    val todosPontos by viewModel.pontos.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val mapViewportState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(LAT_PADRAO, LNG_PADRAO), ZOOM_INICIAL.toFloat())
    }
    var cameraCentralizadaNaLocalizacao by remember { mutableStateOf(false) }

    fun selecionarPonto(ponto: PontoColeta) {
        selecionado = ponto
        onSelecionarPonto(ponto)
    }

    LaunchedEffect(selecionado) {
        rota = emptyList()
        resumoRota = null
        erroRota = null
        val ponto = selecionado ?: return@LaunchedEffect
        if (posicaoUsuario == null) {
            erroRota = "Aguardando sua localização para calcular a rota."
        }
        val origem = snapshotFlow { posicaoUsuario }.filterNotNull().first()
        erroRota = null
        carregandoRota = true
        try {
            val resposta = RetrofitFactory.getEcoWayerApi().calcularRota(
                RotaRequest(
                    origem = Coordenada(origem.latitude, origem.longitude),
                    destino = Coordenada(ponto.latitude, ponto.longitude)
                )
            )
            val dados = resposta.body()?.response
            if (!resposta.isSuccessful || resposta.body()?.status != true || dados == null) {
                error(resposta.body()?.message ?: "Não foi possível calcular a rota.")
            }
            resumoRota = dados
            rota = decodificarPolyline(dados.polyline)
        } catch (erro: CancellationException) {
            throw erro
        } catch (erro: Exception) {
            erroRota = erro.message ?: "Não foi possível calcular a rota."
        } finally {
            carregandoRota = false
        }
    }

    val pontoInicial = todosPontos.firstOrNull { pontoInicialId > 0 && it.id == pontoInicialId }
    LaunchedEffect(pontoInicial) {
        pontoInicial?.let { ponto ->
            mapViewportState.position = CameraPosition.fromLatLngZoom(LatLng(ponto.latitude, ponto.longitude), 15f)
            cameraCentralizadaNaLocalizacao = true
            selecionarPonto(ponto)
        }
    }

    fun buscarLocaisReciclagem(centro: LatLng) {
        scope.launch {
            carregandoLocais = true
            erroLocais = null
            try {
                val resposta = RetrofitFactory.getEcoWayerApi()
                    .buscarLocaisReciclagem(BuscaLocaisRequest(centro.latitude, centro.longitude))
                if (!resposta.isSuccessful || resposta.body()?.status != true) {
                    error(resposta.body()?.message ?: "Não foi possível buscar locais próximos.")
                }
                val dados = resposta.body()?.response
                    ?: error("A API não retornou os locais próximos.")
                locaisReciclagem = dados.locais
            } catch (erro: Exception) {
                erroLocais = erro.message ?: "Não foi possível buscar locais próximos."
            } finally {
                carregandoLocais = false
            }
        }
    }
    val pontos = remember(todosPontos, busca, filtros, posicaoUsuario) {
        val ativos = todosPontos.filter { it.status.equals("ativo", ignoreCase = true) }
        val correspondentes = ativos.filter { it.coincideComBusca(busca) }
        val base = if (busca.isBlank() || correspondentes.isNotEmpty()) correspondentes else ativos
        base.filter {
            it.aceita(filtros.materiais.map { m -> m.valorApi })
        }.sortedBy {
            val origem = posicaoUsuario ?: LatLng(LAT_PADRAO, LNG_PADRAO)
            distanciaMetros(origem.latitude, origem.longitude, it.latitude, it.longitude)
        }
    }

    fun pesquisarLocalizacao(consulta: String) {
        val termo = consulta.trim()
        if (termo.length < 2) return
        scope.launch {
            erroBusca = null
            try {
                val resposta = RetrofitFactory.getEcoWayerApi()
                    .geocodificar(com.example.ecowayer.model.GeocodificarRequest(termo))
                val localizacao = resposta.body()?.response
                if (resposta.isSuccessful && resposta.body()?.status == true && localizacao != null) {
                    mapViewportState.position = CameraPosition.fromLatLngZoom(
                        LatLng(localizacao.latitude, localizacao.longitude),
                        12.5f
                    )
                    buscarLocaisReciclagem(LatLng(localizacao.latitude, localizacao.longitude))
                    return@launch
                }

                val resultadoLocal = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    Geocoder(contexto, Locale("pt", "BR"))
                        .getFromLocationName("$termo, Brasil", 1)
                        ?.firstOrNull()
                }
                if (resultadoLocal == null) {
                    error(resposta.body()?.message ?: "Cidade ou endereço não encontrado.")
                }
                mapViewportState.position = CameraPosition.fromLatLngZoom(
                    LatLng(resultadoLocal.latitude, resultadoLocal.longitude),
                    12.5f
                )
                buscarLocaisReciclagem(LatLng(resultadoLocal.latitude, resultadoLocal.longitude))
            } catch (erro: Exception) {
                erroBusca = erro.message ?: "Não foi possível pesquisar essa localização."
            }
        }
    }

    LaunchedEffect(busca) {
        val termo = busca.trim()
        if (termo.length >= 3 && todosPontos.none { it.coincideComBusca(termo) }) {
            delay(650)
            pesquisarLocalizacao(termo)
        }
    }

    // Permissão de localização
    val (temPermissao, pedirPermissao) = rememberPermissaoLocalizacao()

    LaunchedEffect(temPermissao) {
        if (temPermissao) {
            localizacaoClient.lastLocation.addOnSuccessListener { localizacao ->
                if (localizacao != null) {
                    val ponto = LatLng(localizacao.latitude, localizacao.longitude)
                    posicaoUsuario = ponto
                    if (!cameraCentralizadaNaLocalizacao) {
                        mapViewportState.position = CameraPosition.fromLatLngZoom(ponto, ZOOM_INICIAL.toFloat())
                        cameraCentralizadaNaLocalizacao = true
                        buscarLocaisReciclagem(ponto)
                    }
                }
            }
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
            TituloEcoPontos(onVoltar)

            CampoBusca(
                valor = busca,
                onValorChange = { busca = it },
                onAbrirFiltros = { mostrarFiltros = true },
                onBuscar = { pesquisarLocalizacao(busca) }
            )

            erroBusca?.let {
                Text(it, fontSize = 12.sp, color = Color(0xFFB00020))
            }

            if (carregandoLocais) {
                Text(
                    "Buscando reciclagem, descarte e ferro-velho próximos...",
                    fontSize = 12.sp,
                    color = EcoColors.TextoPrimario,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            erroLocais?.let {
                Text(it, fontSize = 12.sp, color = Color(0xFFB00020))
            }

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
                locaisReciclagem = locaisReciclagem,
                selecionado = selecionado,
                posicaoUsuario = posicaoUsuario,
                temPermissao = temPermissao,
                mapViewportState = mapViewportState,
                onPosicaoUsuario = { posicaoUsuario = it },
                onSelecionar = ::selecionarPonto,
                onFecharCard = {
                    selecionado = null
                    rota = emptyList()
                    resumoRota = null
                    erroRota = null
                },
                onRecentralizar = {
                    if (temPermissao) {
                        val centro = posicaoUsuario ?: LatLng(LAT_PADRAO, LNG_PADRAO)
                        mapViewportState.position = CameraPosition.fromLatLngZoom(centro, ZOOM_INICIAL.toFloat())
                    } else {
                        pedirPermissao()
                    }
                },
                rota = rota,
                resumoRota = resumoRota,
                carregandoRota = carregandoRota,
                erroRota = erroRota,
                onSeguirGoogleMaps = { abrirRotaNoMaps(contexto, it) },
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
                    rota = emptyList()
                    resumoRota = null
                    erroRota = null
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

private fun abrirRotaNoMaps(contexto: Context, ponto: PontoColeta) {
    val uri = Uri.parse("google.navigation:q=${ponto.latitude},${ponto.longitude}")
    val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
    try {
        contexto.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val web = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${ponto.latitude},${ponto.longitude}&travelmode=driving&dir_action=navigate")
        try {
            contexto.startActivity(Intent(Intent.ACTION_VIEW, web))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(contexto, "Nenhum aplicativo disponível para abrir a rota.", Toast.LENGTH_LONG).show()
        }
    }
}

// ---------------------------------------------------------------------------
// MAPA
// ---------------------------------------------------------------------------

@Composable
private fun MapaEcoPontos(
    pontos: List<PontoColeta>,
    locaisReciclagem: List<LocalReciclagem>,
    selecionado: PontoColeta?,
    posicaoUsuario: LatLng?,
    temPermissao: Boolean,
    mapViewportState: CameraPositionState,
    onPosicaoUsuario: (LatLng) -> Unit,
    onSelecionar: (PontoColeta) -> Unit,
    onFecharCard: () -> Unit,
    onRecentralizar: () -> Unit,
    rota: List<LatLng>,
    resumoRota: RotaCalculada?,
    carregandoRota: Boolean,
    erroRota: String?,
    onSeguirGoogleMaps: (PontoColeta) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = mapViewportState,
            properties = MapProperties(isMyLocationEnabled = temPermissao),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            MapEffect(temPermissao) { googleMap ->
                googleMap.setOnMyLocationChangeListener { location ->
                    if (location != null) {
                        onPosicaoUsuario(LatLng(location.latitude, location.longitude))
                    }
                }
            }

            if (rota.size >= 2) {
                Polyline(points = rota, color = EcoColors.Verde, width = 5f)
            }

            pontos.forEach { ponto ->
                val position = LatLng(ponto.latitude, ponto.longitude)
                Marker(
                    state = MarkerState(position = position),
                    title = ponto.nome,
                    snippet = ponto.enderecoCurto(),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    onClick = {
                        onSelecionar(ponto)
                        mapViewportState.position = CameraPosition.fromLatLngZoom(position, ZOOM_INICIAL.toFloat())
                        true
                    }
                )
            }

            locaisReciclagem.forEach { local ->
                val position = LatLng(local.latitude, local.longitude)
                Marker(
                    state = MarkerState(position = position),
                    title = local.nome,
                    snippet = local.endereco,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                    onClick = {
                        onSelecionar(
                            PontoColeta(
                                id = local.id.hashCode(),
                                nome = local.nome,
                                descricao = "Local encontrado pelo Google Places",
                                status = "ativo",
                                latitude = local.latitude,
                                longitude = local.longitude,
                                endereco = listOf(EnderecoPonto(rua = local.endereco))
                            )
                        )
                        mapViewportState.position = CameraPosition.fromLatLngZoom(position, ZOOM_INICIAL.toFloat())
                        true
                    }
                )
            }
        }

        BotaoMinhaLocalizacao(
            onClick = onRecentralizar,
            elevado = selecionado != null
        )

        selecionado?.let { ponto ->
            val origem = posicaoUsuario
            val distancia = origem?.let {
                distanciaMetros(it.latitude, it.longitude, ponto.latitude, ponto.longitude).formatarDistancia()
            }.orEmpty()

            CardPontoSelecionado(
                ponto = ponto,
                distancia = distancia,
                onFechar = onFecharCard,
                resumoRota = resumoRota,
                carregandoRota = carregandoRota,
                erroRota = erroRota,
                onSeguirGoogleMaps = { onSeguirGoogleMaps(ponto) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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
private fun TituloEcoPontos(onVoltar: () -> Unit) {
    Row(
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Voltar para o início",
            modifier = Modifier
                .size(30.dp)
                .clickable { onVoltar() },
            tint = EcoColors.TextoPrimario
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EcoColors.Verde)) { append("Eco") }
                withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Pontos") }
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun CardPontoSelecionado(
    ponto: PontoColeta,
    distancia: String,
    onFechar: () -> Unit,
    resumoRota: RotaCalculada?,
    carregandoRota: Boolean,
    erroRota: String?,
    onSeguirGoogleMaps: () -> Unit,
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
            ponto.materiais.take(4).forEach { material ->
                ChipMaterial(material.nome)
            }
        }

        erroRota?.let {
            Text(it, color = Color(0xFFB00020), fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp))
        }

        when {
            carregandoRota -> Text(
                "Calculando rota...",
                color = Color(0xFF666666),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            resumoRota != null -> Text(
                "Rota: ${resumoRota.distancia_texto} • ${resumoRota.duracao_texto}",
                color = EcoColors.TextoPrimario,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EcoColors.Verde)
                .clickable(enabled = !carregandoRota) { onSeguirGoogleMaps() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (resumoRota == null) "Abrir rota no Google Maps" else "Iniciar navegação",
                color = Color.White, fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun decodificarPolyline(encoded: String): List<LatLng> {
    if (encoded.isBlank()) return emptyList()
    val resultado = mutableListOf<LatLng>()
    var indice = 0
    var latitude = 0
    var longitude = 0

    while (indice < encoded.length) {
        var deslocamento = 0
        var valorLatitude = 0
        var caractere: Int
        do {
            caractere = encoded[indice++].code - 63
            valorLatitude = valorLatitude or ((caractere and 0x1f) shl deslocamento)
            deslocamento += 5
        } while (caractere >= 0x20 && indice < encoded.length)
        latitude += if ((valorLatitude and 1) != 0) (valorLatitude shr 1).inv() else valorLatitude shr 1

        deslocamento = 0
        var valorLongitude = 0
        do {
            caractere = encoded[indice++].code - 63
            valorLongitude = valorLongitude or ((caractere and 0x1f) shl deslocamento)
            deslocamento += 5
        } while (caractere >= 0x20 && indice < encoded.length)
        longitude += if ((valorLongitude and 1) != 0) (valorLongitude shr 1).inv() else valorLongitude shr 1
        resultado += LatLng(latitude / 1e5, longitude / 1e5)
    }
    return resultado
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
    onAbrirFiltros: () -> Unit,
    onBuscar: () -> Unit
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onBuscar() }),
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
