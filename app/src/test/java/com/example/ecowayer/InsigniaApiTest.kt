package com.example.ecowayer

import com.example.ecowayer.service.EcoWayerApiService
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InsigniaApiTest {
    @Test
    fun carregaCatalogoEProgressoPelasRotasDaApi() = runBlocking {
        val rotas = mutableListOf<String>()
        val cliente = OkHttpClient.Builder().addInterceptor { chain ->
            val pedido = chain.request()
            rotas += pedido.url.encodedPath
            val json = if (pedido.url.encodedPath.endsWith("/usuario_insignia"))
                """{"status":true,"status_code":200,"response":{"usuario_insignia":[{"id_insignia":7,"status":"em andamento","progresso":40}]}}"""
            else
                """{"status":true,"status_code":200,"response":{"insignia":[{"id":7,"nome":"Mestre do Vidro","meta":30,"unidade":"kg"}]}}"""
            Response.Builder().request(pedido).protocol(Protocol.HTTP_1_1).code(200)
                .message("OK").body(json.toResponseBody("application/json".toMediaType())).build()
        }.build()
        val api = Retrofit.Builder().baseUrl("http://localhost/").client(cliente)
            .addConverterFactory(GsonConverterFactory.create()).build().create(EcoWayerApiService::class.java)

        val catalogo = api.listarInsignias()
        val progresso = api.listarMinhasInsignias()
        assertTrue(catalogo.isSuccessful && progresso.isSuccessful)
        assertEquals("Mestre do Vidro", catalogo.body()?.response?.insignia?.single()?.nome)
        assertEquals(40f, progresso.body()?.response?.usuario_insignia?.single()?.progresso)
        assertEquals(listOf("/user/insignia", "/user/usuario_insignia"), rotas)
    }
}
