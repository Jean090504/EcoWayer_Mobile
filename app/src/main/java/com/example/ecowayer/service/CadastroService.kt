package com.example.ecowayer.service

import com.example.ecowayer.model.ApiResponse
import com.example.ecowayer.model.EnderecoCadastroRequest
import com.example.ecowayer.model.EnderecoResponse
import com.example.ecowayer.model.GeneroLista
import com.example.ecowayer.model.UsuarioCadastroRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CadastroService {
    @GET("user/genero")
    suspend fun listarGeneros(): ApiResponse<GeneroLista>

    @POST("user/endereco")
    fun criarEndereco(@Body request: EnderecoCadastroRequest): Call<ApiResponse<EnderecoResponse>>

    @POST("user/usuario")
    fun criarUsuario(@Body request: UsuarioCadastroRequest): Call<ApiResponse<Any>>
}
