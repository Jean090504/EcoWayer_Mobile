package com.example.ecowayer

import com.example.ecowayer.model.ApiResponse
import com.example.ecowayer.model.MensagemLista
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test

class MensagemApiTest {
    @Test
    fun leHistoricoComDataDeEnvioEIndicadorDeLeituraNumerico() {
        val json = """
            {
                "status": true,
                "status_code": 200,
                "response": {
                    "mensagem": [
                        {"id": 5, "texto": "Olá", "envio": "2026-11-23 09:00:00", "lida": 0},
                        {"id": 8, "texto": "Oi", "envio": "2026-11-24 10:00:00", "lida": 1}
                    ]
                }
            }
        """.trimIndent()
        val tipo = object : TypeToken<ApiResponse<MensagemLista>>() {}.type
        val historico = Gson().fromJson<ApiResponse<MensagemLista>>(json, tipo).response?.mensagem

        assertEquals(listOf(5, 8), historico?.map { it.id })
        assertEquals("2026-11-23 09:00:00", historico?.first()?.criado_em)
    }
}
