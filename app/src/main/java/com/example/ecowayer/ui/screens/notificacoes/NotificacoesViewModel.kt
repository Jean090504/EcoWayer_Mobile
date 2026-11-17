package com.example.ecowayer.ui.screens.notificacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.NotificacaoApi
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificacoesUiState(val carregando: Boolean = true, val notificacoes: List<NotificacaoApi> = emptyList(), val erro: String? = null)

class NotificacoesViewModel : ViewModel() {
    private val _estado = MutableStateFlow(NotificacoesUiState())
    val estado: StateFlow<NotificacoesUiState> = _estado

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().listarNotificacoes()
                if (!resposta.isSuccessful && resposta.code() != 404) error("Erro ${resposta.code()}")
                val lista = resposta.body()?.response?.notificacao.orEmpty()
                _estado.value = NotificacoesUiState(false, lista)
                lista.filter { !it.lida }.forEach { RetrofitFactory.getEcoWayerApi().marcarNotificacao(it.id, mapOf("lida" to true)) }
            } catch (erro: Exception) {
                _estado.value = NotificacoesUiState(false, erro = erro.message ?: "Não foi possível carregar as notificações.")
            }
        }
    }
}
