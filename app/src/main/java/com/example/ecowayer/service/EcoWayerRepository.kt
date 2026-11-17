package com.example.ecowayer.service

import android.content.Context
import android.net.Uri
import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.model.AnuncioOpcoes
import com.example.ecowayer.model.AnuncioDetalhe
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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
        require(imagens.isNotEmpty()) { "Adicione pelo menos uma foto." }
        val partes = imagens.mapIndexed { indice, uri ->
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Não foi possível ler a imagem selecionada.")
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            MultipartBody.Part.createFormData(
                "fotos", "imagem_$indice.${mime.substringAfter('/', "jpg")}",
                bytes.toRequestBody(mime.toMediaTypeOrNull())
            )
        }
        val resposta = api.publicarAnuncio(
            nome.toRequestBody("text/plain".toMediaTypeOrNull()),
            descricao.toRequestBody("text/plain".toMediaTypeOrNull()),
            categoria.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            modalidade.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            condicao.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            preco.toRequestBody("text/plain".toMediaTypeOrNull()),
            quantidade.toRequestBody("text/plain".toMediaTypeOrNull()),
            unidade.toRequestBody("text/plain".toMediaTypeOrNull()),
            partes
        )
        check(resposta.isSuccessful) { resposta.errorBody()?.string() ?: "Não foi possível publicar o anúncio." }
        return resposta.body()?.response?.anuncio ?: error("A API não retornou o anúncio criado.")
    }
}
