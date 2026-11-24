package com.example.ecowayer.model

data class DadosContaResponse(val usuario: DadosContaApi)

data class DadosContaApi(
    val id: Int,
    val nome: String,
    val email: String?,
    val telefone: String?,
    val imagem: String?,
    val enderecos: List<EnderecoConta> = emptyList()
)

data class EnderecoConta(
    val cep: String? = null,
    val rua: String? = null,
    val numero: String? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    val sigla: String? = null
)

data class AtualizarContaRequest(
    val nome: String = "",
    val email: String = "",
    val confirmar_email: String = "",
    val telefone: String = "",
    val senha_atual: String? = null,
    val senha: String? = null,
    val confirmar_senha: String? = null,
    val foto_url: String? = null,
    val endereco: EnderecoConta? = null
)
