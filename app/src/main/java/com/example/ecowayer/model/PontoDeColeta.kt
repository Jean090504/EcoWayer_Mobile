package com.example.ecowayer.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val status: Boolean,
    val status_code: Int,
    val message: String? = null,
    val response: T? = null
)

data class PontoColetaLista(
    val count: Int = 0,
    @SerializedName("ponto_coleta") val pontos: List<PontoColeta> = emptyList()
)

data class PontoColeta(
    val id: Int = 0,
    val nome: String = "",
    val descricao: String = "",
    val status: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerializedName("id_categoria") val idCategoria: Int = 0,
    val endereco: List<EnderecoPonto> = emptyList(),
    @SerializedName("material_aceito") val materiais: List<MaterialAceito> = emptyList()
)

data class EnderecoPonto(
    val id: Int = 0,
    val cep: String = "",
    val rua: String = "",
    val numero: String = "",
    val complemento: String? = null,
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = "",
    val sigla: String = "",
    @SerializedName("id_externo") val idExterno: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class MaterialAceito(
    val id: Int = 0,
    val nome: String = "",
    val icone: String = ""
)