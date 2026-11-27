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

data class DesafioContatoResponse(val desafio: String = "", val message: String? = null)
data class ConfirmacaoContatoResponse(val confirmado: Boolean = false, val message: String? = null)
data class ContatoErro(val message: String? = null)

interface CadastroService {
    @POST("user/usuario/verificacao/iniciar")
    suspend fun iniciarVerificacao(@Body body: Map<String, String>): retrofit2.Response<DesafioContatoResponse>

    @POST("user/usuario/verificacao/confirmar")
    suspend fun confirmarVerificacao(@Body body: Map<String, String>): retrofit2.Response<ConfirmacaoContatoResponse>

    @POST("user/usuario/verificacao/reenviar")
    suspend fun reenviarVerificacao(@Body body: Map<String, String>): retrofit2.Response<DesafioContatoResponse>

    @POST("user/usuario/recuperar-senha")
    suspend fun recuperarSenha(@Body body: Map<String, String>): retrofit2.Response<ConfirmacaoContatoResponse>
    @GET("user/genero")
    suspend fun listarGeneros(): ApiResponse<GeneroLista>

    @POST("user/endereco")
    fun criarEndereco(@Body request: EnderecoCadastroRequest): Call<ApiResponse<EnderecoResponse>>

    @POST("user/usuario")
    fun criarUsuario(@Body request: UsuarioCadastroRequest): Call<ApiResponse<Any>>
}
