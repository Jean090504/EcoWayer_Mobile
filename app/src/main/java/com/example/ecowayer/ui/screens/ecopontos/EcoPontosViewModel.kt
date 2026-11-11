package com.example.ecowayer.ui.screens.ecopontos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.data.api.RetrofitClient
import com.example.ecowayer.data.model.PontoColetaDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EcoPontosViewModel : ViewModel() {
    private val _pontos = MutableStateFlow<List<PontoColetaDto>>(emptyList())
    val pontos = _pontos.asStateFlow()

    private val _carregando = MutableStateFlow(false)
    val carregando = _carregando.asStateFlow()

    private val _erro = MutableStateFlow<String?>(null)
    val erro = _erro.asStateFlow()

    init { carregar() }

    fun carregar() {
        viewModelScope.launch {
            _carregando.value = true
            _erro.value = null
            try {
                val r = RetrofitClient.api.listarPontos()
                if (r.status) {
                    _pontos.value = r.response.pontoColeta.filter { it.status == "ativo" }
                } else {
                    _erro.value = "Erro ${r.statusCode}"
                }
            } catch (e: Exception) {
                android.util.Log.e("EcoPontos", "Falha ao carregar", e)
                _erro.value = "Erro: ${e.javaClass.simpleName} - ${e.message}"
            } finally {
                _carregando.value = false
            }
        }
    }
}