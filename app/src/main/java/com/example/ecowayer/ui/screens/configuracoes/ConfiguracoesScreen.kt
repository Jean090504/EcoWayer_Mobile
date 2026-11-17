package com.example.ecowayer.ui.screens.configuracoes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onQrCode: () -> Unit
) {
    val usuario = SessionStore.currentUser()
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BarraInferior(DestinoBarra.Perfil, onNavegar, onQrCode)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                }
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = EcoColors.Verde)) { append("Confi") }
                        withStyle(SpanStyle(color = EcoColors.Roxo)) { append("gurações") }
                    },
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Conta", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            ConfiguracaoItem(Icons.Outlined.AccountCircle, "Dados da conta", usuario?.email ?: "")
            ConfiguracaoItem(Icons.Outlined.Lock, "Segurança", "Senha e acesso")
            ConfiguracaoItem(Icons.Outlined.Notifications, "Notificações", "Preferências de avisos")
            Text(
                "As alterações de conta são salvas pelo backend quando a edição estiver disponível.",
                color = Color(0xFF777777),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun ConfiguracaoItem(icone: ImageVector, titulo: String, detalhe: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, null, tint = EcoColors.Verde)
        Column(Modifier.padding(start = 14.dp)) {
            Text(titulo, fontWeight = FontWeight.SemiBold)
            Text(detalhe, color = Color(0xFF777777), fontSize = 12.sp)
        }
    }
}
