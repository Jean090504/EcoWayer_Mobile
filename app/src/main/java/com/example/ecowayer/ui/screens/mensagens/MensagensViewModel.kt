package com.example.ecowayer.ui.screens.mensagens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.ConversaApi
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MensagensUiState(val carregando: Boolean = true, val conversas: List<ConversaApi> = emptyList(), val erro: String? = null)

class MensagensViewModel : ViewModel() {
    private val _estado = MutableStateFlow(MensagensUiState())
    val estado: StateFlow<MensagensUiState> = _estado

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().listarConversas()
                if (!resposta.isSuccessful && resposta.code() != 404) error("Erro ${resposta.code()}")
                _estado.value = MensagensUiState(false, resposta.body()?.response?.conversa.orEmpty())
            } catch (erro: Exception) {
                _estado.value = MensagensUiState(false, erro = erro.message ?: "Não foi possível carregar as conversas.")
            }
        }
    }
}
