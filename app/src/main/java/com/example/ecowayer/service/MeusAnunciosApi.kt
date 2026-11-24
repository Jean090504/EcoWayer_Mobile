package com.example.ecowayer.service

import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.model.pertenceA

internal suspend fun EcoWayerApiService.buscarAnunciosDoUsuario(id: Int): List<AnuncioApi> {
    val resposta = listarAnuncios()
    if (resposta.code() == 404) return emptyList()
    check(resposta.isSuccessful) { "Não foi possível carregar seus anúncios." }
    return resposta.body()?.response?.anuncio.orEmpty().filter { it.pertenceA(id) }
}
