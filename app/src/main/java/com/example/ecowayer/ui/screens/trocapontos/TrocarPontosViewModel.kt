package com.example.ecowayer.ui.screens.trocapontos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.BeneficioCatalogo
import com.example.ecowayer.model.ResgateRequest
import com.example.ecowayer.model.VoucherResgatado
import com.example.ecowayer.service.RetrofitFactory
import com.google.gson.JsonParser
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrocarPontosEstado(
    val carregando: Boolean = false,
    val beneficios: List<BeneficioCatalogo> = emptyList(),
    val saldo: Int? = null,
    val erroCatalogo: String? = null,
    val enviando: Boolean = false,
    val erroResgate: String? = null,
    val voucher: VoucherResgatado? = null
)

class TrocarPontosViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _estado = MutableStateFlow(TrocarPontosEstado())
    val estado = _estado.asStateFlow()
    private val api = RetrofitFactory.getEcoWayerApi()

    init {
        carregar()
    }

    fun carregar() {
        if (_estado.value.carregando || _estado.value.enviando) return
        _estado.update { it.copy(carregando = true, erroCatalogo = null) }
        viewModelScope.launch {
            val saldo = async {
                try {
                    api.consultarSaldoResgate().body()?.response?.saldo
                } catch (erro: CancellationException) {
                    throw erro
                } catch (_: Exception) {
                    null
                }
            }
            try {
                val resposta = api.listarVouchers()
                val catalogo = if (resposta.code() == 404) {
                    emptyList()
                } else {
                    check(resposta.isSuccessful)
                    checkNotNull(resposta.body()?.response).voucher_catalogo
                }
                _estado.update { it.copy(beneficios = catalogo.filter { item -> item.ativo == 1 }) }
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                _estado.update { it.copy(erroCatalogo = "Não foi possível carregar os benefícios.") }
            }
            val saldoAtual = saldo.await()
            _estado.update { it.copy(saldo = saldoAtual, carregando = false) }
        }
    }

    fun resgatar(item: BeneficioCatalogo) {
        if (_estado.value.enviando || _estado.value.carregando) return
        val chave = "solicitacao_${item.id}_${item.pontos_necessarios}"
        val solicitacao = savedState.get<String>(chave) ?: UUID.randomUUID().toString()
        savedState[chave] = solicitacao
        _estado.update { it.copy(enviando = true, erroResgate = null) }

        viewModelScope.launch {
            try {
                val resposta = api.resgatarVoucher(ResgateRequest(item.id, solicitacao, item.pontos_necessarios))
                if (resposta.isSuccessful) {
                    val resultado = checkNotNull(resposta.body()?.response)
                    val voucher = checkNotNull(resultado.voucher)
                    savedState.remove<String>(chave)
                    _estado.update { it.copy(voucher = voucher, saldo = resultado.saldo) }
                } else {
                    val mensagem = runCatching {
                        JsonParser.parseString(resposta.errorBody()?.string().orEmpty())
                            .asJsonObject.get("message")?.asString
                    }.getOrNull()
                    if (resposta.code() in listOf(400, 404, 409)) savedState.remove<String>(chave)
                    _estado.update { it.copy(erroResgate = mensagem ?: "Não foi possível resgatar o benefício.") }
                }
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                _estado.update {
                    it.copy(erroResgate = "O resgate não pôde ser confirmado. Tente novamente para verificar.")
                }
            } finally {
                _estado.update { it.copy(enviando = false) }
            }
        }
    }

    fun fecharAviso() {
        _estado.update { it.copy(voucher = null, erroResgate = null) }
    }
}
