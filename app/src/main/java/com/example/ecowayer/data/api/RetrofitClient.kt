package com.example.ecowayer.data.api

import com.example.ecowayer.data.model.PontosResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface EcoWayerApi {
    @GET("ponto-coleta")
    suspend fun listarPontos(): PontosResponse
}

object RetrofitClient {
    // Emulador: 10.0.2.2 | Aparelho físico: IP do PC (ex.: 192.168.0.10)
    private const val BASE_URL = "http://10.0.2.2:8080/meraki/ecowayer/admin/"

    val api: EcoWayerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EcoWayerApi::class.java)
    }
}