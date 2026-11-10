package com.example.ecowayer.ui.screens.criarAnuncio

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Dados do formulário. Quando a API estiver pronta, mapeie para o body do POST /anuncios.
 */
data class NovoAnuncio(
    val imagens: List<Uri>,
    val titulo: String,
    val categoria: String,
    val modalidade: Modalidade,
    val valor: String,          // ex.: "15,60" (vazio quando Doação/Troca)
    val condicao: String,
    val descricao: String
)

enum class Modalidade(val rotulo: String) { Doacao("Doação"), Troca("Troca"), Venda("Venda") }

private val listaCategorias = listOf("Material", "Móveis", "Fardos", "Plástico", "Papel")
private val listaCondicoes = listOf("Novo", "Seminovo", "Usado")
private const val LIMITE_DESCRICAO = 500
private val FundoCard = Color(0xFFF6F5F9)
private val RoxoClaro = Color(0xFFEDE9F7)

@Composable
fun CriarAnuncioScreen(
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onPublicar: (NovoAnuncio) -> Unit = {}
) {
    val imagens = remember { mutableStateListOf<Uri>() }
    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(listaCategorias.first()) }
    var modalidade by remember { mutableStateOf(Modalidade.Venda) }
    var valor by remember { mutableStateOf("") }
    var condicao by remember { mutableStateOf(listaCondicoes[1]) }
    var descricao by remember { mutableStateOf("") }

    val seletorImagens = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris -> imagens.addAll(uris.filter { it !in imagens }) }

    fun abrirGaleria() = seletorImagens.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    )

    val valorHabilitado = modalidade == Modalidade.Venda

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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBack() }
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Criar ") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("anúncio") }
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .weight(1f)
                )
                // TODO: trocar pelo drawable da árvore (ex.: painterResource(R.drawable.ic_arvore))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF2F1F6))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Imagens
                SecaoCard(fundo = FundoCard) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (imagens.isEmpty()) {
                            BotaoAdicionarImagens(onClick = ::abrirGaleria)
                        } else {
                            imagens.forEachIndexed { i, uri ->
                                Miniatura(uri = uri, onRemover = { imagens.removeAt(i) })
                            }
                            if (imagens.size < 5) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 96.dp, height = 94.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(RoxoClaro)
                                        .clickable { abrirGaleria() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.Add, null, tint = EcoColors.Roxo)
                                        Text(
                                            "Adicionar\nmais imagens",
                                            color = EcoColors.Roxo,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Título
                SecaoCard {
                    RotuloCampo("Título do Anúncio")
                    CampoArredondado(
                        valor = titulo,
                        onChange = { titulo = it },
                        placeholder = "Ex.: Lote de Papelão Ondulado Grosso"
                    )
                }

                // Categoria + Modalidade
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecaoCard(modifier = Modifier.weight(1f)) {
                        RotuloCampo("Categoria")
                        Dropdown(categoria, listaCategorias) { categoria = it }
                    }
                    SecaoCard(modifier = Modifier.weight(1f)) {
                        RotuloCampo("Modalidade")
                        SeletorModalidade(modalidade) { modalidade = it }
                    }
                }

                // Valor + Condição
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecaoCard(modifier = Modifier.weight(1f)) {
                        RotuloCampo("Valor (R$)")
                        CampoArredondado(
                            valor = if (valorHabilitado) valor else "",
                            onChange = { novo ->
                                // aceita apenas dígitos e uma vírgula
                                if (novo.matches(Regex("""\d*,?\d{0,2}"""))) valor = novo
                            },
                            placeholder = if (valorHabilitado) "0,00" else "—",
                            habilitado = valorHabilitado,
                            teclado = KeyboardType.Decimal,
                            centralizado = true
                        )
                    }
                    SecaoCard(modifier = Modifier.weight(1f)) {
                        RotuloCampo("Condição do material")
                        Dropdown(condicao, listaCondicoes) { condicao = it }
                    }
                }

                // Descrição
                SecaoCard {
                    RotuloCampo("Descrição (opcional)")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = descricao,
                            onValueChange = { if (it.length <= LIMITE_DESCRICAO) descricao = it },
                            textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF333333)),
                            modifier = Modifier.fillMaxSize(),
                            decorationBox = { campo ->
                                if (descricao.isEmpty()) {
                                    Text(
                                        "Conte um pouco mais sobre o seu item...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF888888)
                                    )
                                }
                                campo()
                            }
                        )
                        Text(
                            "${descricao.length}/$LIMITE_DESCRICAO",
                            fontSize = 11.sp,
                            color = Color(0xFF888888),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }

                // Botão publicar
                val formularioValido = titulo.isNotBlank() &&
                        (!valorHabilitado || valor.isNotBlank())
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 8.dp)
                        .fillMaxWidth(0.82f)
                        .align(Alignment.CenterHorizontally)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (formularioValido) EcoColors.Roxo else EcoColors.Roxo.copy(alpha = 0.4f))
                        .clickable(enabled = formularioValido) {
                            // --- INTEGRAÇÃO BACK-END (futuro) ---------------------------
                            // viewModel.publicar(...)  // POST /anuncios (multipart com imagens)
                            // -------------------------------------------------------------
                            onPublicar(
                                NovoAnuncio(
                                    imagens.toList(), titulo.trim(), categoria,
                                    modalidade, if (valorHabilitado) valor else "",
                                    condicao, descricao.trim()
                                )
                            )
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Publicar Anúncio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- componentes

@Composable
private fun SecaoCard(
    modifier: Modifier = Modifier,
    fundo: Color = Color.White,
    conteudo: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(fundo)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) { conteudo() }
}

@Composable
private fun RotuloCampo(texto: String) {
    Text(texto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333))
}

@Composable
private fun CampoArredondado(
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    habilitado: Boolean = true,
    teclado: KeyboardType = KeyboardType.Text,
    centralizado: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(14.dp))
            .background(if (habilitado) Color.White else Color(0xFFF0F0F0), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = if (centralizado) Alignment.Center else Alignment.CenterStart
    ) {
        BasicTextField(
            value = valor,
            onValueChange = onChange,
            enabled = habilitado,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF333333),
                textAlign = if (centralizado) TextAlign.Center else TextAlign.Start
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { campo ->
                if (valor.isEmpty()) {
                    Text(
                        placeholder, fontSize = 14.sp, color = Color(0xFF999999),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (centralizado) TextAlign.Center else TextAlign.Start
                    )
                }
                campo()
            }
        )
    }
}

@Composable
private fun Dropdown(selecionado: String, opcoes: List<String>, onSelecionar: (String) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .clickable { aberto = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(selecionado, fontSize = 14.sp, color = Color(0xFF333333))
            Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color(0xFF555555))
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = { onSelecionar(opcao); aberto = false }
                )
            }
        }
    }
}

@Composable
private fun SeletorModalidade(selecionada: Modalidade, onSelecionar: (Modalidade) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFECECEC))
    ) {
        Modalidade.values().forEach { m ->
            val ativo = m == selecionada
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (ativo) EcoColors.Roxo else Color.Transparent)
                    .clickable { onSelecionar(m) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    m.rotulo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (ativo) Color.White else Color(0xFF555555),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BotaoAdicionarImagens(onClick: () -> Unit) {
    val roxo = EcoColors.Roxo
    Box(
        modifier = Modifier
            .size(width = 150.dp, height = 94.dp)
            .drawBehind {
                drawRoundRect(
                    color = roxo,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                )
            }
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CameraAlt, null, tint = roxo)
            Text("Adicionar Imagens", color = roxo, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                "Toque para enviar\nou escolha da galeria",
                color = Color(0xFF888888),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Miniatura(uri: Uri, onRemover: () -> Unit) {
    val context = LocalContext.current
    // Carrega o bitmap fora da main thread. Quando adicionar o Coil, troque por AsyncImage.
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 4 })
                }?.asImageBitmap()
            }.getOrNull()
        }
    }
    Box(
        modifier = Modifier
            .size(94.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE0E0E0))
            .clickable { onRemover() } // toque remove a imagem
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Imagem do anúncio (toque para remover)",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun CriarAnuncioScreenPreview() {
    EcowayerTheme { CriarAnuncioScreen() }
}