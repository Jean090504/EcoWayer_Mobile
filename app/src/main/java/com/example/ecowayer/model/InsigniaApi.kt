package com.example.ecowayer.model

data class InsigniaCatalogoApi(
    val id: Int = 0,
    val nome: String = "",
    val descricao: String? = null,
    val meta: Double? = null,
    val unidade: String? = null
)

data class InsigniaCatalogoLista(val insignia: List<InsigniaCatalogoApi> = emptyList())

data class InsigniaProgressoApi(
    val id_insignia: Int = 0,
    val status: String = "",
    val progresso: Float = 0f
)

data class InsigniaProgressoLista(val usuario_insignia: List<InsigniaProgressoApi> = emptyList())
