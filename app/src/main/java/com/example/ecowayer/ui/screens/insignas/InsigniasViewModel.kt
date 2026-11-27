package com.example.ecowayer.ui.screens.insignias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.InsigniaCatalogoApi
import com.example.ecowayer.model.InsigniaProgressoApi
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InsigniasUiState(
    val carregando: Boolean = true,
    val catalogo: List<InsigniaCatalogoApi> = emptyList(),
    val progresso: List<InsigniaProgressoApi> = emptyList(),
    val erro: String? = null
)

class InsigniasViewModel : ViewModel() {
    private val _estado = MutableStateFlow(InsigniasUiState())
    val estado: StateFlow<InsigniasUiState> = _estado

    init { carregar() }

    fun carregar() {
        _estado.value = _estado.value.copy(carregando = true, erro = null)
        viewModelScope.launch {
            try {
                val api = RetrofitFactory.getEcoWayerApi()
                val catalogo = api.listarInsignias()
                if ((catalogo.code() != 404 && !catalogo.isSuccessful) ||
                    (catalogo.isSuccessful && catalogo.body()?.status != true)) {
                    error(catalogo.body()?.message ?: "Erro ${catalogo.code()} ao carregar insígnias.")
                }
                val minhas = api.listarMinhasInsignias()
                if ((minhas.code() != 404 && !minhas.isSuccessful) ||
                    (minhas.isSuccessful && minhas.body()?.status != true)) {
                    error(minhas.body()?.message ?: "Erro ${minhas.code()} ao carregar progresso.")
                }
                _estado.value = InsigniasUiState(
                    carregando = false,
                    catalogo = catalogo.body()?.response?.insignia.orEmpty(),
                    progresso = minhas.body()?.response?.usuario_insignia.orEmpty()
                )
            } catch (erro: Exception) {
                _estado.value = InsigniasUiState(carregando = false,
                    erro = erro.message ?: "Não foi possível carregar as insígnias.")
            }
        }
    }
}
