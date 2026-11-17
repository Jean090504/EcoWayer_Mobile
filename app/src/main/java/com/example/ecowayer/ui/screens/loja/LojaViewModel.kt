package com.example.ecowayer.ui.screens.loja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LojaUiState(
    val carregando: Boolean = true,
    val anuncios: List<AnuncioApi> = emptyList(),
    val erro: String? = null
)

class LojaViewModel : ViewModel() {
    private val _estado = MutableStateFlow(LojaUiState())
    val estado: StateFlow<LojaUiState> = _estado

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(carregando = true, erro = null)
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().listarAnuncios()
                if (resposta.code() == 404) {
                    _estado.value = LojaUiState(carregando = false)
                    return@launch
                }
                if (!resposta.isSuccessful) throw HttpException(resposta)
                _estado.value = LojaUiState(
                    carregando = false,
                    anuncios = resposta.body()?.response?.anuncio.orEmpty()
                )
            } catch (erro: Exception) {
                _estado.value = LojaUiState(false, erro = erro.message ?: "Não foi possível carregar os anúncios.")
            }
        }
    }
}
