package com.example.ecowayer.ui.screens.pedidos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.ecowayer.model.CodigoPedidoResponse
import com.example.ecowayer.model.PedidoApi
import com.example.ecowayer.model.conteudoPedidoQr
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.ui.components.QrCodeImagem
import com.google.gson.JsonParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private fun expiraEmMillis(valor: String?): Long = runCatching {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).parse(valor.orEmpty())?.time ?: 0L
}.getOrDefault(0L)

private fun mensagemErro(resposta: Response<*>, padrao: String): String = runCatching {
    JsonParser.parseString(resposta.errorBody()?.string()).asJsonObject.get("message")?.asString
}.getOrNull() ?: padrao

@Composable
fun MeusPedidosScreen(onVoltar: () -> Unit, onEscanearQr: () -> Unit) {
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current
    val api = remember { RetrofitFactory.getEcoWayerApi() }
    var pedidos by remember { mutableStateOf<List<PedidoApi>>(emptyList()) }
    var papel by remember { mutableStateOf("comprador") }
    var erro by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(true) }
    var agora by remember { mutableStateOf(System.currentTimeMillis()) }
    val codigos = remember { mutableStateMapOf<String, CodigoPedidoResponse>() }
    val motivos = remember { mutableStateMapOf<String, String>() }

    fun carregar() {
        scope.launch {
            carregando = true
            try {
                val resposta = api.listarPedidos()
                if (!resposta.isSuccessful) error(mensagemErro(resposta, "Não foi possível carregar os pedidos."))
                pedidos = resposta.body()?.pedidos.orEmpty()
                erro = null
            } catch (falha: Exception) { erro = falha.message ?: "Não foi possível carregar os pedidos." }
            finally { carregando = false }
        }
    }
    fun executar(bloco: suspend () -> Unit) {
        if (ocupado) return
        scope.launch {
            ocupado = true
            erro = null
            aviso = null
            try { bloco(); carregar() }
            catch (falha: Exception) { erro = falha.message ?: "Não foi possível atualizar o pedido." }
            finally { ocupado = false }
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { carregar() }
    LaunchedEffect(Unit) { while (true) { delay(1000); agora = System.currentTimeMillis() } }

    Scaffold { espaco ->
        Column(Modifier.fillMaxSize().padding(espaco).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onVoltar) { Text("← Voltar") }
            Text("Meus pedidos", fontWeight = FontWeight.Bold)
            Text("Pedidos de teste do site e do aplicativo aparecem juntos aqui. Após pagar no navegador, volte ao app e consulte o pagamento.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { papel = "comprador" }, enabled = papel != "comprador") { Text("Compras") }
                Button(onClick = { papel = "vendedor" }, enabled = papel != "vendedor") { Text("Vendas") }
            }
            OutlinedButton(onClick = { carregar() }, enabled = !carregando) { Text("Atualizar lista") }
            if (carregando) Text("Carregando pedidos...")
            if (erro != null) Text(erro!!)
            if (aviso != null) Text(aviso!!)
            val lista = pedidos.filter { it.papel == papel }
            if (!carregando && lista.isEmpty()) Text("Nenhum pedido nesta lista.")
            lista.forEach { pedido ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(pedido.titulo, fontWeight = FontWeight.Bold)
                        Text(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(pedido.valor))
                        Text("Pagamento: ${estadoPagamento(pedido.status_pagamento)}")
                        Text("Entrega: ${estadoEntrega(pedido.status_entrega)}")
                        if (pedido.problema != null) Text("Problema relatado: ${pedido.problema}")
                        if (pedido.entregue_em != null) Text("Recebimento confirmado.")
                        OutlinedButton(onClick = { executar {
                            val resposta = api.atualizarPedido(pedido.id)
                            if (!resposta.isSuccessful) error(mensagemErro(resposta, "Não foi possível consultar o pagamento."))
                            aviso = "Pedido atualizado."
                        } }, enabled = !ocupado) { Text("Consultar pagamento") }
                        if (papel == "comprador" && pedido.status_pagamento !in listOf("approved", "refunded", "charged_back") && pedido.entregue_em == null) {
                            Button(onClick = { executar {
                                val resposta = api.iniciarCompraTeste(mapOf("id_anuncio" to pedido.id_anuncio))
                                val url = resposta.body()?.response?.init_point
                                val uri = Uri.parse(url)
                                if (!resposta.isSuccessful || uri.scheme != "https" || uri.host !in listOf("mercadopago.com.br", "www.mercadopago.com.br"))
                                    error(mensagemErro(resposta, "Checkout de teste indisponível."))
                                contexto.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } }, enabled = !ocupado) { Text("Continuar pagamento de teste") }
                        }
                        if (pedido.status_pagamento == "approved" && pedido.status_entrega == "aguardando_entrega") {
                            if (papel == "comprador") {
                                val codigoValido = codigos[pedido.id]?.takeIf { expiraEmMillis(it.expira_em) > agora }
                                if (codigoValido != null) {
                                    Text("Mostre este QR ao vendedor após receber e conferir o produto.")
                                    Box(Modifier.background(Color.White).padding(12.dp)) {
                                        QrCodeImagem(conteudoPedidoQr(pedido.id, codigoValido.codigo), Modifier.size(200.dp))
                                    }
                                    Text("Código: ${codigoValido.codigo}", fontWeight = FontWeight.Bold)
                                } else if (expiraEmMillis(pedido.codigo_expira_em) > agora) {
                                    Text("Já existe um código ativo. Se a tela foi fechada, aguarde sua expiração para gerar outro.")
                                } else {
                                    Button(onClick = { executar {
                                        val resposta = api.gerarCodigoPedido(pedido.id)
                                        val novo = resposta.body()
                                        if (!resposta.isSuccessful || novo?.codigo?.matches(Regex("[0-9]{8}")) != true)
                                            error(mensagemErro(resposta, "Não foi possível gerar o QR de entrega."))
                                        codigos[pedido.id] = novo
                                    } }, enabled = !ocupado) { Text("Gerar QR de recebimento") }
                                }
                                OutlinedTextField(motivos[pedido.id].orEmpty(), { motivos[pedido.id] = it.take(2000) }, label = { Text("Relatar problema na entrega") }, modifier = Modifier.fillMaxWidth())
                                OutlinedButton(onClick = { executar {
                                    val resposta = api.relatarProblemaPedido(pedido.id, mapOf("motivo" to motivos[pedido.id].orEmpty()))
                                    if (!resposta.isSuccessful) error(mensagemErro(resposta, "Não foi possível registrar o problema."))
                                    codigos.remove(pedido.id)
                                    aviso = "Problema registrado. Entrega bloqueada."
                                } }, enabled = !ocupado && motivos[pedido.id].orEmpty().trim().length >= 10) { Text("Registrar problema") }
                            } else {
                                Button(onClick = onEscanearQr) { Text("Escanear QR do comprador") }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun estadoPagamento(valor: String) = when (valor) {
    "approved" -> "Pago"; "pending" -> "Aguardando pagamento"; "in_process" -> "Em análise"
    "refunded" -> "Estornado"; "rejected" -> "Recusado"; "cancelled" -> "Cancelado"; else -> valor
}
private fun estadoEntrega(valor: String) = when (valor) {
    "aguardando_entrega" -> "Aguardando entrega"; "aguardando_pagamento" -> "Aguardando pagamento"
    "entregue" -> "Entregue"; "problema" -> "Problema relatado"; else -> valor
}
