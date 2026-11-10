package com.example.ecowayer.ui.screens.ecopontos

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.components.FiltrosState          // NOVO
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.locationcomponent.location

/**
 * Modelo temporário de um ponto no mapa. Quando a API estiver pronta, gere esta
 * classe a partir do JSON retornado e mova para a pasta data/model.
 */
data class PontoNoMapa(
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val tipo: String            // define a cor do marcador (ex.: PET, Vidro, Papel, Metal)
)

@Composable
fun EcoPontosScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    // ALTERADO: removi o onAbrirFiltros, quem abre o filtro agora é a própria tela
    onSelecionarPonto: (PontoNoMapa) -> Unit = {}
) {
    var busca by remember { mutableStateOf("") }

    // NOVO: controle do bottom sheet e dos filtros aplicados
    var mostrarFiltros by remember { mutableStateOf(false) }
    var filtros by remember { mutableStateOf(FiltrosState()) }

    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // val viewModel: EcoPontosViewModel = viewModel()
    // val pontos by viewModel.pontos.collectAsState()
    //
    // // Buscar ao digitar (use debounce no ViewModel para não chamar a cada letra):
    // LaunchedEffect(busca) {
    //     viewModel.buscarPontos(busca)  // GET /ecopontos?busca=...&lat=...&lng=...
    // }
    // ----------------------------------------------------------------------
    val pontos = emptyList<PontoNoMapa>()   // TODO API

    // Permissão de localização (necessária para a bolinha azul)
    val contexto = LocalContext.current
    var temPermissao by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                contexto, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val pedirPermissao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida -> temPermissao = concedida }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!temPermissao) pedirPermissao.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            // Atenção: o Mapbox usa (longitude, latitude), nessa ordem
            center(Point.fromLngLat(-46.6333, -23.5505))  // TODO: localização real
            zoom(15.5)
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
            // Título
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoColors.Verde)) { append("Eco") }
                    withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Pontos") }
                },
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            // Barra de busca
            CampoBusca(
                valor = busca,
                onValorChange = { busca = it },
                onAbrirFiltros = { mostrarFiltros = true }       // ALTERADO
            )

            // Mapa ocupando o resto da tela
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 12.dp)
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE5ECE4))
            ) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    logo = {},        // esconde a logo
                    scaleBar = {}     // esconde a escala
                ) {
                    // Bolinha azul da localização do usuário
                    MapEffect(temPermissao) { mapView ->
                        if (temPermissao) {
                            mapView.location.updateSettings {
                                enabled = true
                                pulsingEnabled = true
                            }
                        }
                    }

                    // MARCADORES DOS ECOPONTOS (API) ------------------------
                    // Quando tiver o ícone do marcador em res/drawable
                    // (ex.: marcador_ecoponto.png), desenhe um por ponto:
                    //
                    // val icone = rememberIconImage(
                    //     key = R.drawable.marcador_ecoponto,
                    //     painter = painterResource(R.drawable.marcador_ecoponto)
                    // )
                    // pontos.forEach { ponto ->
                    //     PointAnnotation(
                    //         point = Point.fromLngLat(ponto.longitude, ponto.latitude)
                    //     ) {
                    //         iconImage = icone
                    //         interactionsState.onClicked {
                    //             onSelecionarPonto(ponto)
                    //             true
                    //         }
                    //     }
                    // }
                    // A cor por tipo (PET verde, Vidro azul, Papel laranja,
                    // Metal cinza) pode ser um ícone diferente para cada tipo,
                    // escolhido com um `when (ponto.tipo)`.
                    // -------------------------------------------------------
                }

                // Botão de recentralizar na localização do usuário
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2EC))
                        .clickable {
                            if (temPermissao) {
                                mapViewportState.transitionToFollowPuckState()
                            } else {
                                pedirPermissao.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.NearMe,
                        contentDescription = "Minha localização",
                        tint = EcoColors.Verde
                    )
                }
            }
        }

        // NOVO: bottom sheet de filtros (fica depois do Column, dentro do Scaffold)
        if (mostrarFiltros) {
            FiltrosBottomSheet(
                filtrosAtuais = filtros,
                onFechar = { mostrarFiltros = false },
                onAplicar = { novos ->
                    filtros = novos

                    // --- INTEGRAÇÃO BACK-END (futuro) ---------------------------
                    // viewModel.buscarPontos(
                    //     busca = busca,
                    //     materiais = novos.materiais.map { it.valorApi },
                    //     raioKm = novos.distanciaKm,
                    //     horario = novos.horario
                    // )
                    // GET /ecopontos?busca=...&materiais=PLASTICO,PAPEL
                    //               &raio=5&horario=aberto_agora&lat=...&lng=...
                    // ------------------------------------------------------------
                }
            )
        }
    }
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