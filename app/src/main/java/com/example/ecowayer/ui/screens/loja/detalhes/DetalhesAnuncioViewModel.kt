package com.example.ecowayer.ui.screens.detalhes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetalhesUiState(val carregando: Boolean = true, val anuncio: AnuncioApi? = null, val erro: String? = null)

class DetalhesAnuncioViewModel : ViewModel() {
    private val _estado = MutableStateFlow(DetalhesUiState())
    val estado: StateFlow<DetalhesUiState> = _estado

    fun carregar(id: Int) {
        if (id <= 0) { _estado.value = DetalhesUiState(false, erro = "Anúncio inválido."); return }
        viewModelScope.launch {
            _estado.value = DetalhesUiState()
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().buscarAnuncio(id)
                if (!resposta.isSuccessful) error("Não foi possível carregar o anúncio (${resposta.code()}).")
                _estado.value = DetalhesUiState(false, resposta.body()?.response?.anuncio)
            } catch (erro: Exception) {
                _estado.value = DetalhesUiState(false, erro = erro.message ?: "Não foi possível carregar o anúncio.")
            }
        }
    }
}
