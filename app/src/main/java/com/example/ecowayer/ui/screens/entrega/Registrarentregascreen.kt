package com.example.ecowayer.ui.screens.entrega

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.example.ecowayer.model.lerPedidoQr
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.launch

private val FundoPagamento = Color(0xFFEDF0FF)
private val TextoApoio = Color(0xFF565264)

@Composable
fun RegistrarEntregaScreen(
    onNavegar: (DestinoBarra) -> Unit = {},
    onQrCode: () -> Unit = {}
) {
    RegistrarEntregaLayout(onNavegar, onQrCode, leitor = { LeitorQrEntrega() })
}

private data class ResumoPedido(
    val numero: String,
    val subtotal: String,
    val desconto: String,
    val total: String,
    val pontos: Int
)

@Composable
private fun RegistrarEntregaLayout(
    onNavegar: (DestinoBarra) -> Unit,
    onQrCode: () -> Unit,
    leitor: @Composable () -> Unit,
    resumo: ResumoPedido? = null
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BarraInferior(selecionado = null, onSelecionar = onNavegar, onQrCode = onQrCode)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoColors.Verde)) { append("Registrar ") }
                    withStyle(SpanStyle(color = EcoColors.Roxo)) { append("entrega") }
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            leitor()
            Text("A confirmação vale para pedidos pagos no ambiente de teste e exige o código gerado pelo comprador.", color = TextoApoio, fontSize = 14.sp)
        }
    }
}

@Composable
private fun LeitorQrEntrega() {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var leituraAtiva by remember { mutableStateOf(false) }
    var confirmando by remember { mutableStateOf(false) }
    var confirmado by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    val permissao = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        leituraAtiva = concedida
        if (!concedida) erro = "Permita o acesso à câmera nas configurações do app."
    }

    if (leituraAtiva) {
        Box(
            Modifier.fillMaxWidth().heightIn(min = 220.dp).clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF111111)),
            contentAlignment = Alignment.Center
        ) {
            CameraQrEntrega(
                modifier = Modifier.matchParentSize(),
                aoLer = { conteudo ->
                    leituraAtiva = false
                    val pedido = lerPedidoQr(conteudo)
                    if (pedido == null) {
                        erro = "QR Code de entrega inválido."
                    } else {
                        confirmando = true
                        scope.launch {
                            try {
                                val resposta = RetrofitFactory.getEcoWayerApi().confirmarPedido(
                                    pedido.id, mapOf("codigo" to pedido.codigo)
                                )
                                if (!resposta.isSuccessful || resposta.body()?.pedido?.status_entrega != "entregue") {
                                    error(resposta.body()?.message ?: "Não foi possível confirmar esta entrega.")
                                }
                                confirmado = true
                            } catch (falha: Exception) {
                                erro = falha.message ?: "Não foi possível confirmar esta entrega."
                            } finally {
                                confirmando = false
                            }
                        }
                    }
                },
                aoFalhar = {
                    erro = it
                    leituraAtiva = false
                }
            )
            MiraQr(Modifier.align(Alignment.Center))
            TextButton(
                onClick = { leituraAtiva = false },
                modifier = Modifier.align(Alignment.BottomCenter),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) { Text("Fechar câmera") }
        }
    } else {
        PainelQr(
            mensagem = when {
                erro != null -> erro.orEmpty()
                confirmando -> "Confirmando a entrega do pedido..."
                confirmado -> "Entrega confirmada com sucesso."
                else -> "Escaneie o QR Code exibido pelo comprador após entregar o produto."
            },
            acao = if (confirmado || erro != null) "Ler outro QR Code" else "Abrir câmera",
            onAbrir = {
                erro = null
                confirmado = false
                if (!confirmando && ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    leituraAtiva = true
                } else {
                    permissao.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }
}

@Composable
private fun PainelQr(mensagem: String, acao: String, onAbrir: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)
            .clip(RoundedCornerShape(24.dp)).background(Color(0xFF111111))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MiraQr()
        Text(
            mensagem, color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        TextButton(onClick = onAbrir, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF8BDC90))) {
            Text(acao, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MiraQr(modifier: Modifier = Modifier) {
    Box(modifier.size(100.dp), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.QrCodeScanner, null, Modifier.size(88.dp), tint = Color(0xFFDEDEDE))
        Box(Modifier.size(width = 100.dp, height = 2.dp).background(Color(0xFF63C56B)))
    }
}

@Composable
private fun FormaPagamento(icone: ImageVector, titulo: String, detalhe: String, destaque: Boolean = false) {
    val formato = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier.fillMaxWidth().clip(formato)
            .background(if (destaque) FundoPagamento else Color.White)
            .border(1.dp, Color(0xFFF1F0F3), formato)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                .background(if (destaque) EcoColors.Roxo else Color(0xFFF2F2F4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, null, tint = if (destaque) Color.White else TextoApoio)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(detalhe, fontSize = 13.sp, color = TextoApoio)
        }
        Box(Modifier.size(20.dp).border(1.dp, Color(0xFFC1BECA), CircleShape))
    }
}

@Composable
private fun ResumoPedidoCard(resumo: ResumoPedido?) {
    val formato = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF1F0F3), formato).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LinhaResumo("Resumo do pedido", resumo?.numero.orEmpty(), MaterialTheme.colorScheme.onSurface, negrito = true)
        if (resumo == null) {
            Text("Nenhum pedido vinculado a esta entrega.", color = TextoApoio, fontSize = 14.sp)
        } else {
            LinhaResumo("Subtotal da compra", resumo.subtotal, TextoApoio)
            LinhaResumo("Desconto EcoPontos", resumo.desconto, EcoColors.Verde)
        }
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FundoPagamento).padding(12.dp)) {
            LinhaResumo("Total a pagar", resumo?.total ?: "—", EcoColors.Roxo, negrito = true)
        }
    }
}

@Composable
private fun LinhaResumo(rotulo: String, valor: String, cor: Color, negrito: Boolean = false) {
    val peso = if (negrito) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rotulo, Modifier.weight(1f), color = cor, fontWeight = peso, fontSize = 16.sp)
        if (valor.isNotEmpty()) Text(valor, color = cor, fontWeight = peso, fontSize = 15.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrarEntregaScreenPreview() {
    EcowayerTheme {
        RegistrarEntregaLayout(
            onNavegar = {},
            onQrCode = {},
            leitor = { PainelQr("Escaneie o QR Code exibido pelo comprador após entregar o produto.", "Abrir câmera") {} },
            resumo = ResumoPedido("#EW-8924", "R$ 38,50", "− R$ 10,00", "R$ 28,50", 35)
        )
    }
}
