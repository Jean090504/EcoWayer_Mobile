package com.example.ecowayer.ui.screens.carteira

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.MovimentoCarteiraApi
import com.example.ecowayer.model.ResgateExtratoApi
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExtratoPontosEstado(
    val carregando: Boolean = true,
    val pontos: List<MovimentoCarteiraApi>? = null,
    val resgates: List<ResgateExtratoApi>? = null
)

class ExtratoPontosViewModel : ViewModel() {
    private val _estado = MutableStateFlow(ExtratoPontosEstado())
    val estado = _estado.asStateFlow()
    private val api = RetrofitFactory.getEcoWayerApi()

    init { carregar() }

    fun carregar() {
        _estado.value = _estado.value.copy(carregando = true)
        viewModelScope.launch {
            val pontos = async {
                consultar {
                    val resposta = api.buscarCarteira()
                    check(resposta.isSuccessful)
                    checkNotNull(resposta.body()?.response).extrato
                }
            }
            val resgates = async {
                consultar {
                    val resposta = api.listarResgates()
                    if (resposta.code() == 404) emptyList()
                    else {
                        check(resposta.isSuccessful)
                        checkNotNull(resposta.body()?.response).voucher
                    }
                }
            }
            _estado.value = ExtratoPontosEstado(false, pontos.await(), resgates.await())
        }
    }

    private suspend fun <T> consultar(buscar: suspend () -> T): T? = try {
        buscar()
    } catch (erro: CancellationException) {
        throw erro
    } catch (_: Exception) {
        null
    }
}
