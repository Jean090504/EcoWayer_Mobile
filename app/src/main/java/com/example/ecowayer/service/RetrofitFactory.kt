package com.example.ecowayer.service

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.ecowayer.BuildConfig

private class SessionCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()
    private var preferences: android.content.SharedPreferences? = null

    fun initialize(context: android.content.Context, baseUrl: HttpUrl) {
        preferences = context.applicationContext.getSharedPreferences("ecowayer_http", android.content.Context.MODE_PRIVATE)
        cookies.clear()
        preferences?.getStringSet("cookies", emptySet()).orEmpty()
            .mapNotNull { Cookie.parse(baseUrl, it) }
            .forEach(cookies::add)
    }

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
        preferences?.edit()?.putStringSet("cookies", cookies.map { it.toString() }.toSet())?.apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies.filter { it.matches(url) }

    @Synchronized
    fun clear() {
        cookies.clear()
        preferences?.edit()?.remove("cookies")?.apply()
    }
}

object RetrofitFactory {

    // 10.0.2.2 aponta para o host da máquina quando o app roda no emulador.
    // O Backend Usuario usa a porta 3000 no ambiente local.
    private val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
    private val cookieJar = SessionCookieJar()

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getPontoColetaService(): PontoColetaService = retrofit.create(PontoColetaService::class.java)

    fun getAuthService(): AuthService = retrofit.create(AuthService::class.java)

    fun getCadastroService(): CadastroService = retrofit.create(CadastroService::class.java)

    fun getEcoWayerApi(): EcoWayerApiService = retrofit.create(EcoWayerApiService::class.java)

    fun inicializar(context: android.content.Context) {
        SessionStore.initialize(context)
        cookieJar.initialize(context, baseUrl.toHttpUrl())
    }

    fun limparSessao() {
        cookieJar.clear()
        SessionStore.clear()
    }
}
