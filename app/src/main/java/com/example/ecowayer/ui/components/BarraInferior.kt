package com.example.ecowayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

/** Destinos da barra inferior (o botão do QR Code, no centro, é tratado à parte). */
enum class DestinoBarra(val rotulo: String, val icone: ImageVector) {
    Inicio("Início", Icons.Outlined.Home),
    Loja("Loja", Icons.Outlined.Storefront),
    Carteira("Carteira", Icons.Outlined.AccountBalanceWallet),
    Perfil("Perfil", Icons.Outlined.Person)
}

/**
 * Barra de navegação inferior reutilizável.
 * Use em qualquer tela passando qual item está selecionado:
 *
 * Scaffold(bottomBar = {
 *     BarraInferior(
 *         selecionado = DestinoBarra.Loja,
 *         onSelecionar = { destino -> /* navController.navigate(...) */ },
 *         onQrCode = { /* abrir leitor de QR Code */ }
 *     )
 * })
 */
@Composable
fun BarraInferior(
    selecionado: DestinoBarra,
    onSelecionar: (DestinoBarra) -> Unit,
    onQrCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemBarra(DestinoBarra.Inicio, selecionado, onSelecionar, Modifier.weight(1f))
            ItemBarra(DestinoBarra.Loja, selecionado, onSelecionar, Modifier.weight(1f))

            // Botão central (QR Code), um pouco acima da barra
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = (-12).dp)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(EcoColors.Verde)
                        .clickable { onQrCode() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = "Ler QR Code",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            ItemBarra(DestinoBarra.Carteira, selecionado, onSelecionar, Modifier.weight(1f))
            ItemBarra(DestinoBarra.Perfil, selecionado, onSelecionar, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ItemBarra(
    destino: DestinoBarra,
    selecionado: DestinoBarra,
    onSelecionar: (DestinoBarra) -> Unit,
    modifier: Modifier = Modifier
) {
    val ativo = destino == selecionado
    val cor = if (ativo) EcoColors.Verde else Color(0xFF666666)

    Column(
        modifier = modifier
            .clickable { onSelecionar(destino) }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = destino.icone,
            contentDescription = destino.rotulo,
            tint = cor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = destino.rotulo,
            fontSize = 10.sp,
            color = cor,
            fontWeight = if (ativo) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BarraInferiorPreview() {
    EcowayerTheme {
        BarraInferior(
            selecionado = DestinoBarra.Inicio,
            onSelecionar = {},
            onQrCode = {}
        )
    }
}