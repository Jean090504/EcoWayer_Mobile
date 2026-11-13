package com.example.ecowayer.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory {

    private val BASE_URL = "http://10.0.2.2:8070/meraki/ecowayer/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getPontoColetaService(): PontoColetaService {
        return retrofit.create(PontoColetaService::class.java)
    }
}