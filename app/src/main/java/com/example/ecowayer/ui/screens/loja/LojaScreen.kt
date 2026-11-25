package com.example.ecowayer.ui.screens.loja

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.AnuncioApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.components.ImagemRemota
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

/**
 * Modelo temporário do anúncio. Quando a API estiver pronta, gere esta classe
 * a partir do JSON retornado e mova para a pasta data/model.
 */
data class Anuncio(
    val id: Int,
    val titulo: String,
    val preco: String,
    val nota: String,
    val distancia: String,
    val categoria: String,
    val imagemUrl: String? = null
)

private fun AnuncioApi.toUi(): Anuncio {
    val modalidade = modalidade_nome ?: ""
    val precoTexto = when {
        modalidade.contains("doa", true) -> "Grátis"
        modalidade.contains("troca", true) -> "Troca"
        preco != null -> "R$ %.2f".format(java.util.Locale("pt", "BR"), preco)
        else -> "Preço não informado"
    }
    return Anuncio(
        id = id,
        titulo = nome,
        preco = precoTexto,
        nota = "—",
        distancia = "",
        categoria = categoria_nome ?: categoria ?: "Material",
        imagemUrl = imagem
    )
}

private val categorias = listOf("Tudo", "Móveis", "Fardos", "Plástico", "Papel")

@Composable
fun LojaScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onCriarAnuncio: () -> Unit = {},
    onAbrirAnuncio: (Anuncio) -> Unit = {}
) {
    var categoriaSelecionada by remember { mutableStateOf("Tudo") }
    val viewModel: LojaViewModel = viewModel()
    val estado by viewModel.estado.collectAsState()
    val anuncios = estado.anuncios.map { it.toUi() }.filter {
        categoriaSelecionada == "Tudo" || it.categoria == categoriaSelecionada
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            // Loja fica verde na barra (e o Início deixa de ficar)
            BarraInferior(
                selecionado = DestinoBarra.Loja,
                onSelecionar = onNavegar,
                onQrCode = onQrCode
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Eco") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("market") }
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Categorias
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categorias) { categoria ->
                        ChipCategoria(
                            texto = categoria,
                            selecionado = categoria == categoriaSelecionada,
                            onClick = { categoriaSelecionada = categoria }
                        )
                    }
                }
            }

            // Botão criar anúncio
            item {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EcoColors.Roxo)
                        .clickable { onCriarAnuncio() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Criar anúncio",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                if (estado.carregando) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = EcoColors.Roxo,
                            strokeWidth = 2.dp
                        )
                        Text("Carregando anúncios…", modifier = Modifier.padding(start = 10.dp), color = Color.Gray)
                    }
                } else if (estado.erro == null && anuncios.isEmpty()) {
                    Text(
                        "Nenhum anúncio nesta categoria ainda.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        color = Color.Gray
                    )
                }
                estado.erro?.let { erro ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Não foi possível carregar os anúncios.", color = Color(0xFFB3261E))
                        Text("Tentar novamente", color = EcoColors.Roxo, modifier = Modifier.clickable { viewModel.carregar() })
                    }
                }
            }
            // Anúncios reais da API
            if (estado.erro == null) {
                items(anuncios, key = { it.id }) { anuncio ->
                    CardAnuncio(anuncio, onClick = { onAbrirAnuncio(anuncio) })
                }
            }
        }
    }
}

@Composable
private fun ChipCategoria(texto: String, selecionado: Boolean, onClick: () -> Unit) {
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
private fun CardAnuncio(anuncio: Anuncio, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FOTO DO ANÚNCIO ---------------------------------------------------
        // Quando a API devolver a URL da imagem (Coil: io.coil-kt:coil-compose):
        //
        // AsyncImage(
        //     model = anuncio.imagemUrl,
        //     contentDescription = anuncio.titulo,
        //     contentScale = ContentScale.Crop,
        //     modifier = Modifier.size(86.dp).clip(RoundedCornerShape(12.dp))
        // )
        ImagemRemota(
            url = anuncio.imagemUrl,
            descricao = anuncio.titulo,
            modifier = Modifier
                .size(86.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        // -------------------------------------------------------------------

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                anuncio.titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                anuncio.preco,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Verde
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.StarBorder, null,
                    tint = Color(0xFFF5B301),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    anuncio.nota,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 3.dp, end = 10.dp)
                )
                Icon(
                    Icons.Outlined.LocationOn, null,
                    tint = Color(0xFF555555),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    anuncio.distancia,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(start = 3.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LojaScreenPreview() {
    EcowayerTheme { LojaScreen() }
}
