package com.example.ecowayer.data.model

import com.google.gson.annotations.SerializedName

data class PontosResponse(
    val status: Boolean,
    @SerializedName("status_code") val statusCode: Int,
    val response: PontosData
)

data class PontosData(
    val count: Int,
    @SerializedName("ponto_coleta") val pontoColeta: List<PontoColetaDto>
)

data class PontoColetaDto(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val categoria: String,
    val endereco: List<EnderecoDto>,
    @SerializedName("material_aceito") val materialAceito: List<MaterialDto>
)

data class EnderecoDto(
    val id: Int,
    val cep: String?,
    val rua: String?,
    val numero: String?,
    val complemento: String?,
    val bairro: String?,
    val cidade: String?,
    val sigla: String?
)

data class MaterialDto(val id: Int, val nome: String, val icone: String?)