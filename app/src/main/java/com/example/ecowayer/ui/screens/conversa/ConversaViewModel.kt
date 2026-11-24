package com.example.ecowayer.ui.screens.conversa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.MensagemApi
import com.example.ecowayer.model.MensagemRequest
import com.example.ecowayer.model.Usuario
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

data class ConversaUiState(val carregando: Boolean = true, val mensagens: List<MensagemApi> = emptyList(), val erro: String? = null)

class ConversaViewModel : ViewModel() {
    private val _estado = MutableStateFlow(ConversaUiState())
    val estado: StateFlow<ConversaUiState> = _estado

    private val _contato = MutableStateFlow<Usuario?>(null)
    val contato: StateFlow<Usuario?> = _contato

    suspend fun carregarContato(id: Int) {
        _contato.value = null
        if (id <= 0) return
        try {
            val resposta = RetrofitFactory.getEcoWayerApi().buscarConversa(id)
            _contato.value = resposta.body()?.response?.conversa
                ?.outroParticipante(SessionStore.currentUser()?.id)
        } catch (erro: CancellationException) {
            throw erro
        } catch (_: Exception) {
            // O histórico pode ser carregado mesmo se o contato estiver indisponível.
        }
    }

    fun carregar(id: Int) {
        if (id <= 0 || carregandoHistorico) return
        carregandoHistorico = true
        _estado.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().listarMensagens(id)
                check(resposta.isSuccessful || resposta.code() == 404) {
                    "Não foi possível carregar as mensagens."
                }
                val historico = resposta.body()?.response?.mensagem.orEmpty()
                _estado.update { estado ->
                    ConversaUiState(
                        carregando = false,
                        mensagens = (historico + estado.mensagens).distinctBy { it.id }.sortedBy { it.id }
                    )
                }
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                _estado.update { it.copy(carregando = false, erro = "Não foi possível carregar as mensagens.") }
                return@launch
            } finally {
                carregandoHistorico = false
            }

            val ultima = _estado.value.mensagens.lastOrNull() ?: return@launch
            try {
                RetrofitFactory.getEcoWayerApi().marcarMensagensLidas(
                    mapOf("id_conversa" to id, "ate_id" to ultima.id)
                )
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                // A confirmação de leitura é independente do histórico exibido.
            }
        }
    }

    private var carregandoHistorico = false

    fun enviar(id: Int, texto: String) {
        viewModelScope.launch {
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().enviarMensagem(MensagemRequest(id, texto))
                check(resposta.isSuccessful)
                val mensagem = resposta.body()?.response?.mensagem
                if (mensagem != null) {
                    _estado.update { estado ->
                        estado.copy(
                            mensagens = (estado.mensagens + mensagem).distinctBy { it.id }.sortedBy { it.id },
                            erro = null
                        )
                    }
                } else {
                    carregar(id)
                }
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                _estado.update { it.copy(erro = "Não foi possível enviar a mensagem.") }
            }
        }
    }
}
