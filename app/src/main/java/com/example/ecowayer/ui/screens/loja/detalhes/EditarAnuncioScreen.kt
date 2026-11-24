package com.example.ecowayer.ui.screens.detalhes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.pertenceA
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.screens.criarAnuncio.CriarAnuncioScreen
import com.example.ecowayer.ui.theme.EcoColors

@Composable
fun EditarAnuncioScreen(
    anuncioId: Int,
    onBack: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    onQrCode: () -> Unit,
    viewModel: DetalhesAnuncioViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current.applicationContext
    val anuncio = estado.anuncio?.takeIf { it.pertenceA(SessionStore.currentUser()?.id) }
    BackHandler(enabled = estado.processando || estado.salvo) {}
    LaunchedEffect(anuncioId) {
        if (estado.anuncio?.id != anuncioId) viewModel.carregar(anuncioId)
    }

    if (anuncio == null) {
        Box(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    when {
                        estado.carregando -> "Carregando anúncio…"
                        estado.anuncio != null -> "Você só pode editar seus próprios anúncios."
                        else -> estado.erro ?: "Não foi possível carregar o anúncio."
                    }
                )
                Button(onClick = onBack) { Text("Voltar") }
            }
        }
        return
    }

    CriarAnuncioScreen(
        onBack = onBack,
        onNavegar = onNavegar,
        onQrCode = onQrCode,
        onPublicar = { viewModel.salvar(contexto, it) },
        publicando = estado.processando,
        erro = estado.erro,
        edicao = anuncio
    )

    if (estado.salvo) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EcoColors.Verde, modifier = Modifier.size(42.dp))
            },
            title = { Text("Edição concluída") },
            text = { Text("As alterações do anúncio foram salvas.") },
            confirmButton = {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoColors.Roxo)
                ) { Text("Concluir") }
            }
        )
    }
}
