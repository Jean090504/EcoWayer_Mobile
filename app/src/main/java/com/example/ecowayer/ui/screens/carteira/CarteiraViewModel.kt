package com.example.ecowayer.ui.screens.carteira

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.CarteiraDados
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CarteiraUiState(
    val carregando: Boolean = true,
    val carteira: CarteiraDados? = null,
    val erro: String? = null
)

class CarteiraViewModel : ViewModel() {
    private val _estado = MutableStateFlow(CarteiraUiState())
    val estado: StateFlow<CarteiraUiState> = _estado

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().buscarCarteira()
                if (!resposta.isSuccessful || resposta.body()?.status != true) {
                    error(resposta.body()?.message ?: "Erro ${resposta.code()}")
                }
                _estado.value = CarteiraUiState(false, resposta.body()?.response)
            } catch (erro: Exception) {
                _estado.value = CarteiraUiState(false, erro = erro.message ?: "Não foi possível carregar sua carteira.")
            }
        }
    }
}
