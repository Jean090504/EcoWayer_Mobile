package com.example.ecowayer.ui.screens.meusanuncios

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * Modelos temporários. Quando a API estiver pronta, gere estas classes a partir
 * do JSON retornado e mova para a pasta data/model.
 */
enum class StatusAnuncio(val rotulo: String, val cor: Color) {
    Ativo("Ativo", Color(0xFF6A4FBF)),
    Concluido("Concluído", Color(0xFF4CAF50)),
    Pausado("Pausado", Color(0xFF3F6BD6))
}

data class MeuAnuncio(
    val id: Int,
    val titulo: String,
    val bairro: String,
    val status: StatusAnuncio
)

@Composable
fun MeusAnunciosScreen(
    onBack: () -> Unit = {},
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {},
    onCriarAnuncio: () -> Unit = {},
    onAbrirAnuncio: (MeuAnuncio) -> Unit = {},
    onEditarAnuncio: (MeuAnuncio) -> Unit = {}
) {
    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // Troque o estado local abaixo por um MeusAnunciosViewModel:
    //
    // val viewModel: MeusAnunciosViewModel = viewModel()
    // val anuncios by viewModel.anuncios.collectAsState()   // GET /usuarios/me/anuncios
    //
    // LaunchedEffect(Unit) { viewModel.carregar() }
    //
    // Trate também carregando e erro (CircularProgressIndicator / "Tentar de novo").
    // ----------------------------------------------------------------------
    var anuncios by remember {                                   // TODO API
        mutableStateOf(
            listOf(
                MeuAnuncio(1, "Lote de Garrafas PET", "Pinheiros", StatusAnuncio.Ativo),
                MeuAnuncio(2, "Papelão para Mudança", "Vila Madalena", StatusAnuncio.Concluido),
                MeuAnuncio(3, "Paletes de Madeira (4 un)", "Butantã", StatusAnuncio.Pausado)
            )
        )
    }

    // Alterações locais (para a tela já funcionar). Cada uma deve chamar a API:
    fun mudarStatus(anuncio: MeuAnuncio, novo: StatusAnuncio) {
        // viewModel.mudarStatus(anuncio.id, novo)   // PATCH /anuncios/{id}/status
        anuncios = anuncios.map { if (it.id == anuncio.id) it.copy(status = novo) else it }
    }

    fun excluir(anuncio: MeuAnuncio) {
        // (recomendado) confirmar antes com um AlertDialog "Excluir este anúncio?"
        // viewModel.excluir(anuncio.id)             // DELETE /anuncios/{id}
        anuncios = anuncios.filter { it.id != anuncio.id }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        bottomBar = {
            // Loja fica verde na barra, como no protótipo
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
            // Cabeçalho branco (cobre também a área da barra de status)
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
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
                            withStyle(SpanStyle(color = EcoColors.Verde)) { append("Meus ") }
                            withStyle(SpanStyle(color = EcoColors.Roxo)) { append("Anúncios") }
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        // FLUXO (futuro): ao voltar da criação, recarregar a lista
                        //   viewModel.carregar()
                        onCriarAnuncio()
                    }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Criar anúncio",
                            tint = EcoColors.TextoPrimario
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }

            if (anuncios.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Você ainda não publicou anúncios.",
                        fontSize = 14.sp,
                        color = Color(0xFF777777)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(anuncios, key = { it.id }) { anuncio ->
                        CardMeuAnuncio(
                            anuncio = anuncio,
                            onClick = { onAbrirAnuncio(anuncio) },
                            onEditar = {
                                // FLUXO (futuro): abrir o formulário já preenchido
                                //   navController.navigate(Routes.EditarAnuncio.criar(anuncio.id))
                                //   dados: GET /anuncios/{id}  e  PUT /anuncios/{id}
                                onEditarAnuncio(anuncio)
                            },
                            onPausar = { mudarStatus(anuncio, StatusAnuncio.Pausado) },
                            onReativar = { mudarStatus(anuncio, StatusAnuncio.Ativo) },
                            onConcluir = { mudarStatus(anuncio, StatusAnuncio.Concluido) },
                            onExcluir = { excluir(anuncio) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardMeuAnuncio(
    anuncio: MeuAnuncio,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onPausar: () -> Unit,
    onReativar: () -> Unit,
    onConcluir: () -> Unit,
    onExcluir: () -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = anuncio.status.rotulo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(anuncio.status.cor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )

            Box {
                IconButton(onClick = { menuAberto = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Mais opções",
                        tint = Color(0xFF777777)
                    )
                }
                DropdownMenu(
                    expanded = menuAberto,
                    onDismissRequest = { menuAberto = false }
                ) {
                    // Concluído só pode ser excluído
                    if (anuncio.status != StatusAnuncio.Concluido) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = { menuAberto = false; onEditar() }
                        )
                    }
                    when (anuncio.status) {
                        StatusAnuncio.Ativo -> {
                            DropdownMenuItem(
                                text = { Text("Pausar") },
                                onClick = { menuAberto = false; onPausar() }
                            )
                            DropdownMenuItem(
                                text = { Text("Marcar como concluído") },
                                onClick = { menuAberto = false; onConcluir() }
                            )
                        }
                        StatusAnuncio.Pausado -> {
                            DropdownMenuItem(
                                text = { Text("Reativar") },
                                onClick = { menuAberto = false; onReativar() }
                            )
                        }
                        StatusAnuncio.Concluido -> Unit
                    }
                    DropdownMenuItem(
                        text = { Text("Excluir", color = Color(0xFFD32F2F)) },
                        onClick = { menuAberto = false; onExcluir() }
                    )
                }
            }
        }

        Text(
            anuncio.titulo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp, end = 8.dp)
        )
        Text(
            anuncio.bairro,
            fontSize = 14.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MeusAnunciosScreenPreview() {
    EcowayerTheme { MeusAnunciosScreen() }
}