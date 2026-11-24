package com.example.ecowayer.ui.ecopontos   // use o package real da sua pasta

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.ecowayer.model.ApiResponse
import com.example.ecowayer.model.PontoColeta
import com.example.ecowayer.model.PontoColetaLista
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EcoPontosViewModel : ViewModel() {

    private val _pontos = MutableStateFlow<List<PontoColeta>>(emptyList())
    val pontos: StateFlow<List<PontoColeta>> = _pontos

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _carregando = MutableStateFlow(false)
    val carregando: StateFlow<Boolean> = _carregando

    init {
        carregar()
    }

    fun carregar() {
        if (_carregando.value) return
        _carregando.value = true
        _erro.value = null
        RetrofitFactory.getPontoColetaService().listar()
            .enqueue(object : Callback<ApiResponse<PontoColetaLista>> {
                override fun onResponse(
                    call: Call<ApiResponse<PontoColetaLista>>,
                    response: Response<ApiResponse<PontoColetaLista>>
                ) {
                    if (response.isSuccessful) {
                        _pontos.value = response.body()?.response?.pontos ?: emptyList()
                    } else {
                        _erro.value = "Erro ${response.code()}"
                    }
                    _carregando.value = false
                }

                override fun onFailure(call: Call<ApiResponse<PontoColetaLista>>, t: Throwable) {
                    Log.e("API", "Falha: ${t.javaClass.simpleName} - ${t.message}")
                    _erro.value = "Falha: ${t.message}"
                    _carregando.value = false
                }
            })
    }
}
