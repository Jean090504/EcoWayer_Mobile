package com.example.ecowayer.service

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private class SessionCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, novosCookies: List<Cookie>) {
        novosCookies.forEach { nova ->
            cookies.removeAll { antiga ->
                antiga.name == nova.name && antiga.domain == nova.domain && antiga.path == nova.path
            }
            if (!nova.persistent || nova.expiresAt > System.currentTimeMillis()) {
                cookies += nova
            }
        }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies.filter { it.matches(url) }

    @Synchronized
    fun clear() = cookies.clear()
}

object RetrofitFactory {

    // 10.0.2.2 aponta para o host da máquina quando o app roda no emulador.
    // O Backend Usuario usa a porta 3000 no ambiente local.
    private const val BASE_URL = "http://10.0.2.2:3000/meraki/ecowayer/"
    private val cookieJar = SessionCookieJar()

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getPontoColetaService(): PontoColetaService = retrofit.create(PontoColetaService::class.java)

    fun getAuthService(): AuthService = retrofit.create(AuthService::class.java)

    fun getCadastroService(): CadastroService = retrofit.create(CadastroService::class.java)

    fun limparSessao() = cookieJar.clear()
}
