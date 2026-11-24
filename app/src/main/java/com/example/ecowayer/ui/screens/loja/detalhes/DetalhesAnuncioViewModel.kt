package com.example.ecowayer.ui.screens.detalhes

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.model.EditarAnuncioRequest
import com.example.ecowayer.model.donoId
import com.example.ecowayer.model.pertenceA
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.service.FotosCloudinaryService
import com.example.ecowayer.ui.screens.criarAnuncio.NovoAnuncio
import com.example.ecowayer.ui.screens.criarAnuncio.normalizarOpcaoAnuncio
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetalhesUiState(
    val carregando: Boolean = true,
    val anuncio: AnuncioApi? = null,
    val erro: String? = null,
    val processando: Boolean = false,
    val conversaId: Int? = null,
    val salvo: Boolean = false
)

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
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: Exception) {
                _estado.value = DetalhesUiState(false, erro = erro.message ?: "Não foi possível carregar o anúncio.")
            }
        }
    }
    fun iniciarConversa() {
        val anuncio = _estado.value.anuncio ?: return
        val usuario = SessionStore.currentUser()?.id
        if (usuario == null || anuncio.donoId() == null ||
            anuncio.pertenceA(usuario) || !anuncio.status.equals("ativo", true)
        ) return

        executar("Não foi possível abrir a conversa.") {
            val resposta = RetrofitFactory.getEcoWayerApi().criarConversa(mapOf("id_anuncio" to anuncio.id))
            val conversa = resposta.body()?.response?.conversa
            check(resposta.isSuccessful && conversa != null && conversa.id > 0) {
                resposta.erroApi("Não foi possível abrir a conversa.")
            }
            _estado.update { it.copy(conversaId = conversa.id) }
        }
    }

    fun consumirConversa() {
        _estado.update { it.copy(conversaId = null) }
    }

    private val fotosEnviadas = mutableMapOf<Uri, String>()

    fun salvar(contexto: Context, formulario: NovoAnuncio) {
        val anuncio = _estado.value.anuncio ?: return
        if (_estado.value.salvo || !anuncio.pertenceA(SessionStore.currentUser()?.id)) return
        executar("Não foi possível salvar o anúncio.") {
            val api = RetrofitFactory.getEcoWayerApi()
            val opcoes = api.opcoesAnuncio().body()?.response
                ?: error("Não foi possível carregar as opções do anúncio.")
            val categoria = opcoes.categorias.firstOrNull {
                normalizarOpcaoAnuncio(it.nome) == normalizarOpcaoAnuncio(formulario.categoria)
            } ?: error("Selecione uma categoria válida.")
            val modalidade = opcoes.modalidades.firstOrNull {
                normalizarOpcaoAnuncio(it.nome) == normalizarOpcaoAnuncio(formulario.modalidade.rotulo)
            } ?: error("Selecione uma modalidade válida.")
            val condicao = opcoes.condicoes.firstOrNull {
                normalizarOpcaoAnuncio(it.nome) == normalizarOpcaoAnuncio(formulario.condicao)
            } ?: error("Selecione uma condição válida.")
            val quantidade = formulario.quantidade.toDoubleOrNull()
                ?.takeIf { it.isFinite() && it > 0 } ?: error("Informe uma quantidade válida.")
            val fotosNovas = formulario.imagens.map { uri ->
                fotosEnviadas[uri] ?: FotosCloudinaryService.enviar(contexto, uri).also { fotosEnviadas[uri] = it }
            }
            val resposta = api.editarAnuncioPublicado(
                anuncio.id,
                EditarAnuncioRequest(
                    nome = formulario.titulo,
                    descricao = formulario.descricao,
                    preco = formulario.valor.replace(',', '.').toDoubleOrNull(),
                    quantidade = quantidade,
                    unidade = formulario.unidade,
                    id_categoria = categoria.id,
                    id_modalidade = modalidade.id,
                    id_condicao_item = condicao.id,
                    fotos_mantidas = formulario.fotosMantidas,
                    fotos = fotosNovas
                )
            )
            check(resposta.isSuccessful && resposta.body()?.response?.anuncio?.id == anuncio.id) {
                resposta.erroApi("Não foi possível salvar o anúncio.")
            }
            _estado.update { it.copy(salvo = true) }
        }
    }

    private fun executar(falha: String, operacao: suspend () -> Unit) {
        if (_estado.value.processando) return
        _estado.update { it.copy(processando = true, erro = null) }
        viewModelScope.launch {
            try {
                operacao()
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: Exception) {
                _estado.update { it.copy(erro = (erro as? IllegalStateException)?.message ?: falha) }
            } finally {
                _estado.update { it.copy(processando = false) }
            }
        }
    }

    private fun retrofit2.Response<*>.erroApi(padrao: String): String {
        if (code() !in 400..499) return padrao
        return runCatching {
            val corpo = JsonParser.parseString(errorBody()?.string().orEmpty()).asJsonObject
            corpo.get("field")?.asString ?: corpo.get("message")?.asString
        }.getOrNull() ?: padrao
    }

}
