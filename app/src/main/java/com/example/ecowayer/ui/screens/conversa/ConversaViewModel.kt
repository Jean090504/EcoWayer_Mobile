package com.example.ecowayer.ui.screens.conversa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.MensagemApi
import com.example.ecowayer.model.MensagemRequest
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ConversaUiState(val carregando: Boolean = true, val mensagens: List<MensagemApi> = emptyList(), val erro: String? = null)

class ConversaViewModel : ViewModel() {
    private val _estado = MutableStateFlow(ConversaUiState())
    val estado: StateFlow<ConversaUiState> = _estado

    fun carregar(id: Int) {
        viewModelScope.launch {
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().listarMensagens(id)
                if (!resposta.isSuccessful && resposta.code() != 404) error("Erro ${resposta.code()}")
                _estado.value = ConversaUiState(false, resposta.body()?.response?.mensagem.orEmpty())
                val ultima = _estado.value.mensagens.maxOfOrNull { it.id } ?: 0
                if (ultima > 0) RetrofitFactory.getEcoWayerApi().marcarMensagensLidas(mapOf("id_conversa" to id, "ate_id" to ultima))
            } catch (erro: Exception) {
                _estado.value = ConversaUiState(false, erro = erro.message ?: "Não foi possível carregar as mensagens.")
            }
        }
    }

    fun enviar(id: Int, texto: String) {
        val atual = _estado.value.mensagens
        viewModelScope.launch {
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().enviarMensagem(MensagemRequest(id, texto))
                if (!resposta.isSuccessful) error("Não foi possível enviar a mensagem.")
                val mensagem = resposta.body()?.response?.mensagem
                if (mensagem != null) _estado.value = _estado.value.copy(mensagens = _estado.value.mensagens + mensagem, erro = null)
            } catch (erro: Exception) {
                _estado.value = _estado.value.copy(mensagens = atual, erro = erro.message ?: "Não foi possível enviar a mensagem.")
            }
        }
    }
}
