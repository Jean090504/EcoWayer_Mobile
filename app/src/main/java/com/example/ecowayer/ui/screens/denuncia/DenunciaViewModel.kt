package com.example.ecowayer.ui.screens.denuncia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.DenunciaRequest
import com.example.ecowayer.model.TipoDenuncia
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.SessionStore
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DenunciaEstado(
    val carregandoMotivos: Boolean = false,
    val motivos: List<TipoDenuncia> = emptyList(),
    val mensagem: String? = null,
    val erroMotivos: String? = null,
    val erroEnvio: String? = null,
    val enviando: Boolean = false,
    val enviada: Boolean = false
)

class DenunciaViewModel(savedState: SavedStateHandle) : ViewModel() {
    private val mensagemId = savedState.get<Int>("mensagemId")
    private val anuncioId = savedState.get<Int>("id")
    val mensagemSelecionada = mensagemId != null
    private val api = RetrofitFactory.getEcoWayerApi()
    private val _estado = MutableStateFlow(DenunciaEstado())
    val estado = _estado.asStateFlow()

    init {
        carregarMotivos()
        mensagemId?.let { id ->
            viewModelScope.launch {
                try {
                    val resposta = api.buscarMensagem(id)
                    val texto = resposta.body()?.response?.mensagem?.texto
                    if (resposta.isSuccessful) _estado.update { it.copy(mensagem = texto) }
                } catch (erro: CancellationException) {
                    throw erro
                } catch (_: Exception) {
                    // A API valida o vínculo pelo ID ao receber a denúncia.
                }
            }
        }
    }

    fun carregarMotivos() {
        if (_estado.value.carregandoMotivos) return
        _estado.update { it.copy(carregandoMotivos = true, erroMotivos = null) }
        viewModelScope.launch {
            try {
                val resposta = api.listarTiposDenuncia()
                check(resposta.isSuccessful || resposta.code() == 404)
                val motivos = resposta.body()?.response?.tipo_denuncias.orEmpty()
                    .filter { it.id > 0 && it.rotulo().isNotBlank() }
                _estado.update { it.copy(motivos = motivos) }
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                _estado.update { it.copy(erroMotivos = "Não foi possível carregar os motivos da denúncia.") }
            } finally {
                _estado.update { it.copy(carregandoMotivos = false) }
            }
        }
    }

    fun enviar(motivoId: Int, detalhes: String) {
        val atual = _estado.value
        if (atual.enviando || atual.enviada) return
        val motivo = atual.motivos.firstOrNull { it.id == motivoId } ?: return
        _estado.update { it.copy(enviando = true, erroEnvio = null) }
        viewModelScope.launch {
            try {
                val usuario = checkNotNull(SessionStore.currentUser()) { "Entre na sua conta para denunciar." }
                val alvo = if (mensagemId != null) "mensagem" else "anúncio"
                check((mensagemId ?: anuncioId ?: 0) > 0) { "Não foi possível identificar o item denunciado." }
                val resposta = api.criarDenuncia(
                    DenunciaRequest(
                        descricao = detalhes.ifBlank { "Denúncia de $alvo: ${motivo.rotulo()}." },
                        id_tipo_denuncias = motivo.id,
                        id_denunciante = usuario.id,
                        mensagens = listOfNotNull(mensagemId),
                        id_anuncio = anuncioId
                    )
                )
                check(resposta.isSuccessful && resposta.body()?.status == true) {
                    val erroApi = runCatching {
                        val corpo = JsonParser.parseString(resposta.errorBody()?.string().orEmpty()).asJsonObject
                        corpo.get("field")?.asString ?: corpo.get("message")?.asString
                    }.getOrNull()
                    if (resposta.code() in 400..499 && !erroApi.isNullOrBlank()) {
                        erroApi
                    } else {
                        "Não foi possível registrar a denúncia (HTTP ${resposta.code()})."
                    }
                }
                _estado.update { it.copy(enviada = true) }
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: Exception) {
                val mensagem = (erro as? IllegalStateException)?.message ?: "Não foi possível enviar a denúncia."
                _estado.update { it.copy(erroEnvio = mensagem) }
            } finally {
                _estado.update { it.copy(enviando = false) }
            }
        }
    }
}

internal fun TipoDenuncia.rotulo() = tipo.ifBlank { nome }
