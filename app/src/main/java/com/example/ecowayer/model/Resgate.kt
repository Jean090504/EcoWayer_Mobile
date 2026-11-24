package com.example.ecowayer.model

data class BeneficioCatalogo(
    val id: Int,
    val nome_parceiro: String,
    val beneficio: String,
    val pontos_necessarios: Int,
    val validade_dias: Int,
    val ativo: Int
)

data class CatalogoBeneficios(val voucher_catalogo: List<BeneficioCatalogo> = emptyList())

data class ResgateRequest(
    val id_voucher_catalogo: Int,
    val solicitacao: String,
    val pontos_confirmados: Int
)

data class ResgateResposta(
    val voucher: VoucherResgatado? = null,
    val saldo: Int? = null
)

data class VoucherResgatado(val codigo: String, val data_expiracao: String?)

data class SaldoResgate(val saldo: Int)
