package com.example.ecowayer

import com.example.ecowayer.model.DenunciaRequest
import com.example.ecowayer.service.EcoWayerApiService
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DenunciaApiTest {
    @Test
    fun denunciaDeMensagemUsaJsonSemCharsetEIncluiIdDaMensagem() {
        runBlocking {
            val cliente = OkHttpClient.Builder().addInterceptor { chain ->
                val pedido = chain.request()
                val corpo = checkNotNull(pedido.body)
                assertEquals("application/json", corpo.contentType().toString())
                val buffer = Buffer()
                corpo.writeTo(buffer)
                val json = JsonParser.parseString(buffer.readUtf8()).asJsonObject
                assertEquals(42, json.getAsJsonArray("mensagens")[0].asInt)
                assertFalse(json.has("id_anuncio"))
                Response.Builder()
                    .request(pedido)
                    .protocol(Protocol.HTTP_1_1)
                    .code(201)
                    .message("Created")
                    .body("""{"status":true,"status_code":201}""".toResponseBody("application/json".toMediaType()))
                    .build()
            }.build()
            val api = Retrofit.Builder()
                .baseUrl("http://localhost/")
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(EcoWayerApiService::class.java)

            val resposta = api.criarDenuncia(DenunciaRequest("Golpe", 1, 16, mensagens = listOf(42)))
            assertTrue(resposta.isSuccessful)
        }
    }
}
