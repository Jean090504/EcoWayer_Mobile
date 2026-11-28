package com.example.ecowayer.model

data class CriarAnuncioRequest(
    val nome: String,
    val descricao: String,
    val id_categoria: Int,
    val id_modalidade: Int,
    val id_condicao_item: Int,
    val preco: Double?,
    val quantidade: Double,
    val unidade: String,
    val fotos: List<String>
)
