package com.example.ecowayer.ui.screens.configuracoes

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.AtualizarContaRequest
import com.example.ecowayer.model.DadosContaApi
import com.example.ecowayer.service.FotosCloudinaryService
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.SessionStore
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response

data class DadosContaEstado(
    val conta: DadosContaApi? = null,
    val formulario: AtualizarContaRequest = AtualizarContaRequest(),
    val carregando: Boolean = true,
    val enviando: Boolean = false,
    val mensagem: String? = null,
    val sair: Boolean = false,
    val fotoSelecionada: Uri? = null
)

class DadosContaViewModel : ViewModel() {
    private val _estado = MutableStateFlow(DadosContaEstado())
    val estado = _estado.asStateFlow()
    private val api = RetrofitFactory.getEcoWayerApi()
    private var fotoEnviada: Pair<Uri, String>? = null

    init {
        carregar()
    }

    fun carregar() {
        val id = SessionStore.currentUser()?.id
        if (id == null) {
            _estado.value = DadosContaEstado(carregando = false, mensagem = "Entre na sua conta para editar seus dados.")
            return
        }
        _estado.value = DadosContaEstado()
        viewModelScope.launch {
            try {
                val resposta = api.buscarDadosConta(id)
                check(resposta.isSuccessful && resposta.body()?.status == true) {
                    resposta.mensagem("Não foi possível carregar seus dados.")
                }
                preencher(checkNotNull(resposta.body()?.response?.usuario))
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: Exception) {
                _estado.value = DadosContaEstado(
                    carregando = false,
                    mensagem = if (erro is IllegalStateException) erro.message else "Não foi possível carregar seus dados."
                )
            }
        }
    }

    fun editar(alterar: AtualizarContaRequest.() -> AtualizarContaRequest) {
        _estado.update { it.copy(formulario = it.formulario.alterar(), mensagem = null) }
    }

    fun selecionarFoto(uri: Uri) {
        if (_estado.value.enviando) return
        fotoEnviada = null
        _estado.update { it.copy(fotoSelecionada = uri, mensagem = null) }
    }

    fun descartar() {
        _estado.value.conta?.let { preencher(it) }
    }

    fun salvar(contexto: Context) {
        val atual = _estado.value
        val conta = atual.conta ?: return
        if (atual.enviando) return
        val dados = atual.formulario
        val trocouCredencial = !dados.email.trim().equals(conta.email, ignoreCase = true) || !dados.senha.isNullOrEmpty()
        val pedido = dados.copy(
            endereco = dados.endereco?.takeUnless { it == conta.enderecos.firstOrNull() },
            senha_atual = dados.senha_atual?.takeIf { trocouCredencial },
            senha = dados.senha?.takeIf { it.isNotEmpty() },
            confirmar_senha = dados.confirmar_senha?.takeIf { !dados.senha.isNullOrEmpty() }
        )
        executar("Não foi possível salvar seus dados.") {
            val fotoUrl = atual.fotoSelecionada?.let { uri ->
                fotoEnviada?.takeIf { it.first == uri }?.second
                    ?: FotosCloudinaryService.enviar(contexto, uri).also { fotoEnviada = uri to it }
            }
            val resposta = api.atualizarDadosConta(conta.id, pedido.copy(foto_url = fotoUrl))
            check(resposta.isSuccessful && resposta.body()?.status == true) {
                resposta.mensagem("Não foi possível salvar seus dados.")
            }
            val novaConta = checkNotNull(resposta.body()?.response?.usuario)
            if (trocouCredencial) {
                _estado.value = DadosContaEstado(
                    carregando = false, sair = true, mensagem = "Dados salvos. Entre novamente."
                )
            } else {
                val usuario = SessionStore.currentUser()
                if (usuario?.id == novaConta.id) {
                    SessionStore.saveUser(usuario.copy(nome = novaConta.nome, foto = novaConta.imagem))
                }
                preencher(novaConta, "Dados salvos.")
            }
        }
    }

    fun desativar() {
        val id = _estado.value.conta?.id ?: return
        if (_estado.value.enviando) return
        executar("Não foi possível desativar sua conta.") {
            val resposta = api.desativarConta(id)
            check(resposta.isSuccessful && resposta.body()?.status == true) {
                resposta.mensagem("Não foi possível desativar sua conta.")
            }
            _estado.value = DadosContaEstado(carregando = false, sair = true, mensagem = "Conta desativada.")
        }
    }

    private fun preencher(conta: DadosContaApi, mensagem: String? = null) {
        fotoEnviada = null
        _estado.value = DadosContaEstado(
            conta = conta,
            carregando = false,
            mensagem = mensagem,
            formulario = AtualizarContaRequest(
                nome = conta.nome,
                email = conta.email.orEmpty(),
                confirmar_email = conta.email.orEmpty(),
                telefone = conta.telefone.orEmpty(),
                endereco = conta.enderecos.firstOrNull()
            )
        )
    }

    private fun executar(falha: String, acao: suspend () -> Unit) {
        _estado.update { it.copy(enviando = true, mensagem = null) }
        viewModelScope.launch {
            try {
                acao()
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: Exception) {
                val mensagem = if (erro is IllegalStateException) erro.message ?: falha else falha
                _estado.update { it.copy(mensagem = mensagem) }
            } finally {
                _estado.update { it.copy(enviando = false) }
            }
        }
    }

    private fun Response<*>.mensagem(padrao: String) = runCatching {
        JsonParser.parseString(errorBody()?.string().orEmpty()).asJsonObject.get("message")?.asString
    }.getOrNull() ?: padrao
}
