package com.example.ecowayer.ui.screens.meusanuncios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.service.buscarAnunciosDoUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MeusAnunciosUiState(val carregando: Boolean = true, val anuncios: List<AnuncioApi> = emptyList(), val erro: String? = null)

class MeusAnunciosViewModel : ViewModel() {
    private val _estado = MutableStateFlow(MeusAnunciosUiState())
    val estado: StateFlow<MeusAnunciosUiState> = _estado

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            try {
                val id = checkNotNull(SessionStore.currentUser()?.id) { "Entre para ver seus anúncios." }
                val anuncios = RetrofitFactory.getEcoWayerApi().buscarAnunciosDoUsuario(id)
                _estado.value = MeusAnunciosUiState(false, anuncios)
            } catch (erro: Exception) {
                _estado.value = MeusAnunciosUiState(false, erro = erro.message ?: "Não foi possível carregar seus anúncios.")
            }
        }
    }

    fun excluir(id: Int) {
        viewModelScope.launch {
            val resposta = RetrofitFactory.getEcoWayerApi().excluirAnuncio(id)
            if (resposta.isSuccessful) carregar()
        }
    }

    fun atualizarStatus(anuncio: AnuncioApi, status: String) {
        viewModelScope.launch {
            val body = mapOf<String, Any?>(
                "nome" to anuncio.nome,
                "descricao" to anuncio.descricao,
                "data_publicacao" to anuncio.data_publicacao,
                "id_usuario" to anuncio.id_usuario,
                "id_categoria" to anuncio.id_categoria,
                "id_modalidade" to anuncio.id_modalidade,
                "id_condicao_item" to anuncio.id_condicao_item,
                "preco" to anuncio.preco,
                "peso_kg" to anuncio.peso_kg,
                "status" to status
            )
            val resposta = RetrofitFactory.getEcoWayerApi().atualizarAnuncio(anuncio.id, body)
            if (resposta.isSuccessful) carregar()
        }
    }
}
