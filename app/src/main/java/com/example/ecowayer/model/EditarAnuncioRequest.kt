package com.example.ecowayer.model

data class EditarAnuncioRequest(
    val nome: String,
    val descricao: String,
    val preco: Double?,
    val quantidade: Double,
    val unidade: String,
    val id_categoria: Int,
    val id_modalidade: Int,
    val id_condicao_item: Int,
    val fotos_mantidas: List<String>,
    val fotos: List<String> = emptyList()
)
