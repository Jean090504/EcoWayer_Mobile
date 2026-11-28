package com.example.ecowayer.service

import android.content.Context
import android.net.Uri
import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.model.AnuncioOpcoes
import com.example.ecowayer.model.AnuncioDetalhe
import com.example.ecowayer.model.CriarAnuncioRequest
import com.google.gson.JsonParser

class EcoWayerRepository(private val context: Context) {
    private val api get() = RetrofitFactory.getEcoWayerApi()

    suspend fun carregarOpcoes(): AnuncioOpcoes {
        val resposta = api.opcoesAnuncio()
        check(resposta.isSuccessful) { "Não foi possível carregar as opções do anúncio (${resposta.code()})." }
        return resposta.body()?.response ?: error("A API não retornou as opções do anúncio.")
    }

    suspend fun publicarAnuncio(
        nome: String,
        descricao: String,
        categoria: Int,
        modalidade: Int,
        condicao: Int,
        preco: String,
        quantidade: String,
        unidade: String,
        imagens: List<Uri>
    ): AnuncioApi {
        require(imagens.size in 1..6) { "Adicione de 1 a 6 fotos." }
        val valor = preco.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
        val total = quantidade.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
            ?: error("Informe uma quantidade válida.")
        val fotos = imagens.map { FotosCloudinaryService.enviar(context, it) }
        val resposta = api.publicarAnuncio(CriarAnuncioRequest(
            nome = nome, descricao = descricao, id_categoria = categoria,
            id_modalidade = modalidade, id_condicao_item = condicao,
            preco = valor, quantidade = total, unidade = unidade, fotos = fotos
        ))
        check(resposta.isSuccessful) {
            runCatching {
                val corpo = JsonParser.parseString(resposta.errorBody()?.string().orEmpty()).asJsonObject
                corpo.get("field")?.asString ?: corpo.get("message")?.asString
            }.getOrNull() ?: "Não foi possível publicar o anúncio (${resposta.code()})."
        }
        return resposta.body()?.response?.anuncio ?: error("A API não retornou o anúncio criado.")
    }
}
