package com.example.ecowayer.ui.screens.configuracoes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors

@Composable
fun ConfiguracoesScreen(
    onBack: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    onQrCode: () -> Unit,
    onDadosConta: () -> Unit
) {
    val usuario = SessionStore.currentUser()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { CabecalhoConfiguracoes("Confi", "gurações", onBack) },
        bottomBar = { BarraInferior(DestinoBarra.Perfil, onNavegar, onQrCode) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Conta", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            ConfiguracaoItem(Icons.Outlined.AccountCircle, "Dados da conta", usuario?.email.orEmpty(), onDadosConta)
            ConfiguracaoItem(Icons.Outlined.Lock, "Segurança", "Senha e acesso", onDadosConta)
            ConfiguracaoItem(Icons.Outlined.Notifications, "Notificações", "Preferências de avisos")
        }
    }
}

@Composable
internal fun CabecalhoConfiguracoes(inicio: String, fim: String, onBack: () -> Unit) {
    val cores = MaterialTheme.colorScheme
    val roxo = if (cores.surface.luminance() > 0.5f) EcoColors.Roxo else cores.primary
    Column(
        Modifier.fillMaxWidth().background(cores.surface).statusBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
            }
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoColors.Verde)) { append(inicio) }
                    withStyle(SpanStyle(color = roxo)) { append(fim) }
                },
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider()
    }
}

@Composable
private fun ConfiguracaoItem(icone: ImageVector, titulo: String, detalhe: String, onClick: (() -> Unit)? = null) {
    val item = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Row(
        modifier = item.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, null, tint = EcoColors.Verde)
        Column(Modifier.padding(start = 14.dp)) {
            Text(titulo, fontWeight = FontWeight.SemiBold)
            Text(detalhe, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}
