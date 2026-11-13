package com.example.ecowayer.service

import com.example.ecowayer.model.ApiResponse
import com.example.ecowayer.model.PontoColetaLista
import retrofit2.Call
import retrofit2.http.GET

interface PontoColetaService {

    @GET("user/ponto_coleta")
    fun listar(): Call<ApiResponse<PontoColetaLista>>
}