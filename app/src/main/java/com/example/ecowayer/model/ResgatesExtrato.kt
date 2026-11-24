package com.example.ecowayer.model

data class ResgatesExtratoApi(val voucher: List<ResgateExtratoApi> = emptyList())

data class ResgateExtratoApi(
    val id: Int,
    val nome_parceiro: String?,
    val beneficio: String?,
    val pontos_utilizados: Int,
    val data_resgate: String?
)
