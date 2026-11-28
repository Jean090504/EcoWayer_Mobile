package com.example.ecowayer.service

import org.json.JSONObject
import retrofit2.Response

fun Response<*>.mensagemErro(padrao: String): String {
    val conteudo = errorBody()?.string().orEmpty()
    return runCatching { JSONObject(conteudo).optString("message") }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: padrao
}
