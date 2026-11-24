package com.example.ecowayer.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Invocation
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.ecowayer.BuildConfig
import com.example.ecowayer.model.Usuario

private class SessionCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()
    private var preferences: android.content.SharedPreferences? = null

    @Synchronized
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
    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies.filter { it.expiresAt > System.currentTimeMillis() && it.matches(url) }

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
    private val apiUrl = baseUrl.toHttpUrl()
    private val urlCarteira = apiUrl.resolve("user/carteira")!!
    private val cookieJar = SessionCookieJar()
    private val _sessaoRecusada = MutableStateFlow(false)
    val sessaoRecusada = _sessaoRecusada.asStateFlow()
    private var versaoSessao = 0L

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val pedido = chain.request()
            val servico = pedido.tag(Invocation::class.java)?.method()?.declaringClass
            val versao = if (servico == EcoWayerApiService::class.java) versaoSessaoAtiva() else null
            val resposta = chain.proceed(pedido)
            val url = resposta.request.url
            if (resposta.code == 401 && versao != null &&
                url.scheme == apiUrl.scheme && url.host == apiUrl.host && url.port == apiUrl.port
            ) {
                recusarSessao(versao)
            }
            resposta
        }
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

    @Synchronized
    fun inicializar(context: android.content.Context) {
        SessionStore.initialize(context)
        cookieJar.initialize(context, apiUrl)
        if (SessionStore.isLoggedIn() && !temCookieDeSessao()) {
            recusarSessao(versaoSessao)
        }
    }

    private fun temCookieDeSessao() =
        cookieJar.loadForRequest(urlCarteira).any { it.name == "ecowayer_token" && it.value.isNotBlank() }

    @Synchronized
    fun salvarSessao(usuario: Usuario): Boolean {
        if (!temCookieDeSessao()) return false
        versaoSessao++
        SessionStore.saveUser(usuario)
        _sessaoRecusada.value = false
        return true
    }

    @Synchronized
    private fun versaoSessaoAtiva(): Long? = versaoSessao.takeIf { SessionStore.isLoggedIn() }

    @Synchronized
    private fun recusarSessao(versao: Long) {
        if (versao != versaoSessao) return
        limparSessao()
        _sessaoRecusada.value = true
    }

    @Synchronized
    fun limparSessao() {
        versaoSessao++
        cookieJar.clear()
        SessionStore.clear()
    }
}
