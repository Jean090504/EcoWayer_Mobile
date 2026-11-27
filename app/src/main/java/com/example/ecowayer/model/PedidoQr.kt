package com.example.ecowayer.model

import java.util.UUID

data class PedidoQr(val id: String, val codigo: String)
fun conteudoPedidoQr(id: String, codigo: String): String = "ecowayer:pedido:v1:$id:$codigo"

fun lerPedidoQr(valor: String): PedidoQr? {
    val partes = valor.split(':')
    if (partes.size != 5 || partes[0] != "ecowayer" || partes[1] != "pedido" || partes[2] != "v1") return null
    if (!partes[3].matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))) return null
    val id = runCatching { UUID.fromString(partes[3]).toString() }.getOrNull() ?: return null
    if (!partes[4].matches(Regex("[0-9]{8}"))) return null
    return PedidoQr(id, partes[4])
}

data class PedidoConfirmadoApi(val id: String = "", val status_entrega: String = "")
data class PedidoConfirmacaoResponse(val pedido: PedidoConfirmadoApi? = null, val message: String? = null)

data class PedidoApi(
    val id: String = "",
    val id_anuncio: Int = 0,
    val titulo: String = "",
    val valor: Double = 0.0,
    val papel: String = "",
    val status_pagamento: String = "",
    val status_entrega: String = "",
    val codigo_expira_em: String? = null,
    val entregue_em: String? = null,
    val problema: String? = null
)
data class PedidosResponse(val pedidos: List<PedidoApi> = emptyList(), val message: String? = null)
data class PedidoResponse(val pedido: PedidoApi? = null, val message: String? = null)
data class CodigoPedidoResponse(val codigo: String = "", val expira_em: String = "", val message: String? = null)
data class PreferenciaPedido(val pedido_id: String = "", val init_point: String = "")
data class PreferenciaPedidoResponse(val response: PreferenciaPedido? = null, val message: String? = null)
