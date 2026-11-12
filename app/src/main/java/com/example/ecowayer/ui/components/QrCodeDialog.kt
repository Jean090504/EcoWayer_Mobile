package com.example.ecowayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ecowayer.ui.theme.EcoColors
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/** Modelo temporário. Quando a API estiver pronta, gere a partir do JSON e mova para data/model. */
data class VoucherQr(
    val codigo: String,      // ex.: "VERDE-8371-ECO"
    val validoAte: String,   // ex.: "15/05/2026"
    val conteudoQr: String   // texto que vai dentro do QR (normalmente o próprio código/token)
)

private val VerdeClaro = Color(0xFFEAF5E6)
private val Vermelho = Color(0xFFD9534F)

/**
 * Popup de QR Code com dois estados:
 *  - voucher == null  -> "Deseja gerar esse QR Code?"
 *  - voucher != null  -> código do voucher + QR
 */
@Composable
fun QrCodeDialog(
    voucher: VoucherQr?,
    onGerar: () -> Unit,
    onFechar: () -> Unit
) {
    Dialog(onDismissRequest = onFechar) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            if (voucher == null) ConteudoConfirmar(onGerar) else ConteudoVoucher(voucher)

            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Fechar",
                tint = Vermelho,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp)
                    .border(1.5.dp, Vermelho, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .clickable { onFechar() }
            )
        }
    }
}

@Composable
private fun ConteudoConfirmar(onGerar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Deseja gerar esse",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Text(
            "QR Code?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = EcoColors.Roxo,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .padding(top = 28.dp)
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .background(EcoColors.Roxo)
                .clickable { onGerar() },
            contentAlignment = Alignment.Center
        ) {
            Text("Gerar Qr code", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConteudoVoucher(voucher: VoucherQr) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CÓDIGO DO VOUCHER", fontSize = 11.sp, color = Color(0xFF777777), letterSpacing = 0.5.sp)

        Text(
            text = voucher.codigo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = EcoColors.Verde,
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VerdeClaro)
                .padding(horizontal = 18.dp, vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            QrCodeImagem(conteudo = voucher.conteudoQr, modifier = Modifier.size(150.dp))
        }

        Text(
            buildValidade(voucher.validoAte),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF444444),
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Uso único • Apresente na boca do caixa",
            fontSize = 11.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun buildValidade(data: String) = androidx.compose.ui.text.buildAnnotatedString {
    append("Válido até ")
    pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
    append(data)
    pop()
}

/** Desenha o QR direto no Canvas (sem bitmap). */
@Composable
fun QrCodeImagem(conteudo: String, modifier: Modifier = Modifier) {
    val matriz = remember(conteudo) {
        QRCodeWriter().encode(
            conteudo, BarcodeFormat.QR_CODE, 1, 1,
            mapOf(EncodeHintType.MARGIN to 0)
        )
    }
    Canvas(modifier = modifier) {
        val modulo = size.width / matriz.width
        for (x in 0 until matriz.width) {
            for (y in 0 until matriz.height) {
                if (matriz[x, y]) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x * modulo, y * modulo),
                        size = Size(modulo + 0.5f, modulo + 0.5f) // +0.5 evita frestas entre módulos
                    )
                }
            }
        }
    }
}