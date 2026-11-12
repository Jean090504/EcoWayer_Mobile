package com.example.ecowayer.ui.screens.detalhes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

/**
 * Modelo temporário. Quando a API estiver pronta, gere estas classes a partir
 * do JSON retornado e mova para a pasta data/model.
 */
data class DetalheAnuncio(
    val titulo: String,
    val preco: String,            // ex.: "R$ 15,00"
    val tipo: String,             // ex.: "Venda", "Troca", "Doação"
    val condicao: String,         // ex.: "Seminovo"
    val descricao: String,
    val imagemUrl: String? = null,
    val anunciante: Anunciante
)

data class Anunciante(
    val nome: String,
    val nota: String,             // ex.: "5.0"
    val descartesBemSucedidos: Int,
    val fotoUrl: String? = null
)

@Composable
fun DetalhesAnuncioScreen(
    anuncioId: Int = 0,
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onConversar: () -> Unit = {},
    onDenunciar: () -> Unit = {}
) {
    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // Troque o mock abaixo por um estado vindo do ViewModel:
    //
    // val viewModel: DetalhesAnuncioViewModel = viewModel()
    // val anuncio by viewModel.anuncio.collectAsState()
    //
    // LaunchedEffect(anuncioId) {
    //     viewModel.carregar(anuncioId)    // GET /anuncios/{id}
    // }
    //
    // Trate também carregando e erro (ex.: CircularProgressIndicator /
    // mensagem com botão "Tentar de novo") antes de mostrar o conteúdo.
    // ----------------------------------------------------------------------
    val anuncio = DetalheAnuncio(          // TODO API
        titulo = "Papelão para Mudança",
        preco = "R$ 15,00",
        tipo = "Venda",
        condicao = "Seminovo",
        descricao = "Cerca de 10 caixas desmontadas, ideais para mudança ou artesanato. " +
                "Todas secas, limpas e bem armazenadas no bairro de Pinheiros.",
        anunciante = Anunciante("Fernando Pessoa", "5.0", 12)
    )

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraInferior(
                selecionado = DestinoBarra.Loja,
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
            // Cabeçalho
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
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Detalhes do ") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("anúncio") }
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.weight(1f))

                IconButton(onClick = onDenunciar) {
                    Icon(
                        Icons.Outlined.Flag,
                        contentDescription = "Denunciar anúncio",
                        tint = Color(0xFFC93B36)
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Conteúdo rolável
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // FOTO DO ANÚNCIO ---------------------------------------------
                // Quando a API devolver a URL da imagem (Coil: io.coil-kt:coil-compose):
                //
                // AsyncImage(
                //     model = anuncio.imagemUrl,
                //     contentDescription = anuncio.titulo,
                //     contentScale = ContentScale.Crop,
                //     modifier = Modifier
                //         .fillMaxWidth()
                //         .height(220.dp)
                //         .clip(RoundedCornerShape(16.dp))
                // )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE0E0E0))
                )
                // -------------------------------------------------------------

                // Etiquetas
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Etiqueta(
                        texto = anuncio.tipo,
                        fundo = Color(0xFFEDE7F6),
                        corTexto = EcoColors.Roxo,
                        negrito = true
                    )
                    Etiqueta(
                        texto = anuncio.condicao,
                        fundo = Color(0xFFF2F2F2),
                        corTexto = Color(0xFF444444)
                    )
                }

                Text(anuncio.titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    anuncio.preco,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.Roxo
                )

                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Descrição", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        anuncio.descricao,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF555555)
                    )
                }

                CardAnunciante(anuncio.anunciante)
            }

            // Botão fixo, logo acima da barra inferior
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(EcoColors.Roxo)
                    .clickable { onConversar() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline, null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "conversar com anunciante",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String, fundo: Color, corTexto: Color, negrito: Boolean = false) {
    Text(
        text = texto,
        fontSize = 12.sp,
        fontWeight = if (negrito) FontWeight.Bold else FontWeight.Medium,
        color = corTexto,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun CardAnunciante(anunciante: Anunciante) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F7F9))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FOTO DO ANUNCIANTE ------------------------------------------------
        // AsyncImage(
        //     model = anunciante.fotoUrl,
        //     contentDescription = anunciante.nome,
        //     modifier = Modifier.size(44.dp).clip(CircleShape)
        // )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        )
        // -------------------------------------------------------------------

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(anunciante.nome, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.StarBorder, null,
                    tint = Color(0xFFF5B301),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    "${anunciante.nota} • ${anunciante.descartesBemSucedidos} descarte bem-sucedidos",
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
fun DetalhesAnuncioScreenPreview() {
    EcowayerTheme { DetalhesAnuncioScreen() }
}