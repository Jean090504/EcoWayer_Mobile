package com.example.ecowayer.ui.screens.detalhes

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.model.donoId
import com.example.ecowayer.model.pertenceA
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.components.ImagemRemota
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import kotlinx.coroutines.launch

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

private fun AnuncioApi.toDetalhe(): DetalheAnuncio {
    val modalidade = modalidade_nome ?: "Anúncio"
    val precoTexto = when {
        modalidade.contains("doa", true) -> "Grátis (Doação)"
        modalidade.contains("troca", true) -> "Troca"
        preco != null -> "R$ %.2f".format(java.util.Locale("pt", "BR"), preco)
        else -> "Preço não informado"
    }
    return DetalheAnuncio(
        titulo = nome,
        preco = precoTexto,
        tipo = modalidade,
        condicao = condicao_nome ?: "Não informado",
        descricao = descricao.orEmpty(),
        imagemUrl = imagem,
        anunciante = Anunciante(vendedor?.nome ?: "Usuário EcoWayer", "—", 0, vendedor?.foto)
    )
}

@Composable
fun DetalhesAnuncioScreen(
    anuncioId: Int = 0,
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onAbrirConversa: (Int) -> Unit = {},
    onAbrirPedidos: () -> Unit = {},
    onDenunciar: (Int) -> Unit = {},
    onEditar: (Int) -> Unit = {}
) {
    val viewModel: DetalhesAnuncioViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current
    var comprando by remember { mutableStateOf(false) }
    var erroCompra by remember { mutableStateOf<String?>(null) }
    val estado by viewModel.estado.collectAsState()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.carregar(anuncioId) }
    LaunchedEffect(estado.conversaId) {
        estado.conversaId?.let { id ->
            viewModel.consumirConversa()
            onAbrirConversa(id)
        }
    }
    val anuncioApi = estado.anuncio
    if (anuncioApi == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(estado.erro ?: "Carregando anúncio...", color = if (estado.erro == null) Color.Gray else Color(0xFFB3261E))
        }
        return
    }
    val anuncio = anuncioApi.toDetalhe()
    val usuarioId = SessionStore.currentUser()?.id
    val proprio = anuncioApi.pertenceA(usuarioId)
    val podeInteragir = usuarioId != null && anuncioApi.donoId() != null && !proprio
    val podeConversar = podeInteragir && anuncioApi.status.equals("ativo", ignoreCase = true)
    val podeComprar = podeConversar && anuncioApi.modalidade_nome.equals("venda", ignoreCase = true) && (anuncioApi.preco ?: 0.0) > 0

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

                if (podeInteragir) {
                    IconButton(onClick = { onDenunciar(anuncioApi.id) }) {
                        Icon(
                            Icons.Outlined.Flag,
                            contentDescription = "Denunciar anúncio",
                            tint = Color(0xFFC93B36)
                        )
                    }
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
                ImagemRemota(
                    url = anuncio.imagemUrl,
                    descricao = anuncio.titulo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
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

                if (proprio) {
                    Text("Seu anúncio", fontWeight = FontWeight.SemiBold, color = EcoColors.Verde)
                } else {
                    CardAnunciante(anuncio.anunciante)
                }
                estado.erro?.let { Text(it, color = Color(0xFFB3261E), fontSize = 13.sp) }
                erroCompra?.let { Text(it, color = Color(0xFFB3261E), fontSize = 13.sp) }
            }

            if (podeComprar) {
                Button(
                    onClick = { scope.launch {
                        comprando = true
                        erroCompra = null
                        try {
                            val resposta = RetrofitFactory.getEcoWayerApi().iniciarCompraTeste(mapOf("id_anuncio" to anuncioApi.id))
                            val url = resposta.body()?.response?.init_point
                            val uri = Uri.parse(url)
                            if (!resposta.isSuccessful || uri.scheme != "https" || uri.host !in listOf("mercadopago.com.br", "www.mercadopago.com.br"))
                                error(resposta.body()?.message ?: "Checkout de teste indisponível.")
                            contexto.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            onAbrirPedidos()
                        } catch (falha: Exception) { erroCompra = falha.message ?: "Não foi possível iniciar a compra." }
                        finally { comprando = false }
                    } },
                    enabled = !comprando,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) { Text(if (comprando) "Abrindo checkout..." else "Comprar em ambiente de teste") }
            }

            if (proprio || podeConversar) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EcoColors.Roxo)
                        .clickable(enabled = !estado.processando) {
                            if (proprio) onEditar(anuncioApi.id) else viewModel.iniciarConversa()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            if (proprio) Icons.Outlined.Edit else Icons.Outlined.ChatBubbleOutline, null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            when {
                                proprio -> "Editar anúncio"
                                estado.processando -> "Abrindo conversa…"
                                else -> "Conversar com anunciante"
                            },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
        ImagemRemota(
            url = anunciante.fotoUrl,
            descricao = anunciante.nome,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
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
