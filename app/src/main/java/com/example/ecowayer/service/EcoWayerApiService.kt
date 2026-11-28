package com.example.ecowayer.service

import com.example.ecowayer.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EcoWayerApiService {
    @POST("user/ia/assistant-chat")
    suspend fun conversarEco(@Body body: PerguntaEco): Response<RespostaEco>

    @POST("user/pagamento/teste/preferencia")
    suspend fun iniciarCompraTeste(@Body body: Map<String, Int>): Response<PreferenciaPedidoResponse>

    @GET("user/pedidos")
    suspend fun listarPedidos(): Response<PedidosResponse>

    @POST("user/pedidos/{id}/atualizar")
    suspend fun atualizarPedido(@Path("id") id: String): Response<PedidoResponse>

    @POST("user/pedidos/{id}/codigo")
    suspend fun gerarCodigoPedido(@Path("id") id: String): Response<CodigoPedidoResponse>

    @POST("user/pedidos/{id}/problema")
    suspend fun relatarProblemaPedido(@Path("id") id: String, @Body body: Map<String, String>): Response<PedidoConfirmacaoResponse>

    @POST("user/pedidos/{id}/confirmar")
    suspend fun confirmarPedido(@Path("id") id: String, @Body body: Map<String, String>): Response<PedidoConfirmacaoResponse>

    @POST("user/mapa/geocodificar")
    suspend fun geocodificar(@Body body: GeocodificarRequest): Response<ApiResponse<LocalizacaoEncontrada>>

    @POST("user/mapa/locais-reciclagem")
    suspend fun buscarLocaisReciclagem(@Body body: BuscaLocaisRequest): Response<ApiResponse<LocaisReciclagemResponse>>

    @POST("user/rota")
    suspend fun calcularRota(@Body body: RotaRequest): Response<ApiResponse<RotaCalculada>>

    @GET("user/usuario")
    suspend fun buscarMeuPerfil(): Response<ApiResponse<UsuarioResponse>>

    @GET("user/usuario/{id}/perfil")
    suspend fun buscarDadosConta(@Path("id") id: Int): Response<ApiResponse<DadosContaResponse>>

    @PATCH("user/usuario/{id}/perfil")
    suspend fun atualizarDadosConta(
        @Path("id") id: Int,
        @Body dados: AtualizarContaRequest
    ): Response<ApiResponse<DadosContaResponse>>

    @DELETE("user/usuario/{id}")
    suspend fun desativarConta(@Path("id") id: Int): Response<ApiResponse<Any>>

    @GET("user/usuario/{id}")
    suspend fun buscarUsuario(@Path("id") id: Int): Response<ApiResponse<UsuarioResponse>>

    @GET("user/anuncio")
    suspend fun listarAnuncios(): Response<ApiResponse<AnuncioLista>>

    @GET("user/anuncio/{id}")
    suspend fun buscarAnuncio(@Path("id") id: Int): Response<ApiResponse<AnuncioDetalhe>>

    @GET("user/anuncio/opcoes")
    suspend fun opcoesAnuncio(): Response<ApiResponse<AnuncioOpcoes>>

    @PUT("user/anuncio/{id}/publicar")
    suspend fun editarAnuncioPublicado(
        @Path("id") id: Int,
        @Body dados: EditarAnuncioRequest
    ): Response<ApiResponse<AnuncioDetalhe>>

    @DELETE("user/anuncio/{id}")
    suspend fun excluirAnuncio(@Path("id") id: Int): Response<ApiResponse<Any>>

    @Headers("Content-Type: application/json")
    @PUT("user/anuncio/{id}")
    suspend fun atualizarAnuncio(@Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any?>): Response<ApiResponse<AnuncioDetalhe>>

    @POST("user/anuncio/publicar")
    suspend fun publicarAnuncio(@Body dados: CriarAnuncioRequest): Response<ApiResponse<AnuncioDetalhe>>

    @GET("user/tipo_denuncias")
    suspend fun listarTiposDenuncia(): Response<ApiResponse<TipoDenunciaLista>>

    @Headers("Content-Type: application/json")
    @POST("user/denuncias")
    suspend fun criarDenuncia(@Body body: DenunciaRequest): Response<ApiResponse<DenunciaResponse>>

    @GET("user/conversa")
    suspend fun listarConversas(): Response<ApiResponse<ConversaLista>>

    @GET("user/conversa/{id}")
    suspend fun buscarConversa(@Path("id") id: Int): Response<ApiResponse<ConversaResponse>>

    @Headers("Content-Type: application/json")
    @POST("user/conversa")
    suspend fun criarConversa(@Body body: Map<String, Int>): Response<ApiResponse<ConversaResponse>>

    @GET("user/mensagens")
    suspend fun listarMensagens(@Query("id_conversa") conversaId: Int): Response<ApiResponse<MensagemLista>>

    @GET("user/mensagens/{id}")
    suspend fun buscarMensagem(@Path("id") id: Int): Response<ApiResponse<MensagemResponse>>

    @POST("user/mensagens")
    suspend fun enviarMensagem(@Body body: MensagemRequest): Response<ApiResponse<MensagemResponse>>

    @PUT("user/mensagens/lidas")
    suspend fun marcarMensagensLidas(@Body body: Map<String, Int>): Response<ApiResponse<Any>>

    @GET("user/pontos")
    suspend fun listarPontos(): Response<ApiResponse<PontoRegistroLista>>

    @GET("user/carteira")
    suspend fun buscarCarteira(): Response<ApiResponse<CarteiraDados>>

    @GET("user/entrega")
    suspend fun listarEntregas(): Response<ApiResponse<EntregaLista>>

    @POST("user/entrega")
    suspend fun criarEntrega(@Body body: Map<String, @JvmSuppressWildcards Any?>): Response<ApiResponse<EntregaResponse>>

    @GET("user/notificacao")
    suspend fun listarNotificacoes(@Query("nao_lidas") somenteNaoLidas: Boolean? = null): Response<ApiResponse<NotificacaoLista>>

    @PUT("user/notificacao/{id}")
    suspend fun marcarNotificacao(@Path("id") id: Int, @Body body: Map<String, Boolean>): Response<ApiResponse<Any>>

    @GET("user/insignia")
    suspend fun listarInsignias(): Response<ApiResponse<InsigniaCatalogoLista>>

    @GET("user/usuario_insignia")
    suspend fun listarMinhasInsignias(): Response<ApiResponse<InsigniaProgressoLista>>

    @GET("user/voucher_catalogo")
    suspend fun listarVouchers(): Response<ApiResponse<CatalogoBeneficios>>

    @GET("user/voucher")
    suspend fun listarResgates(): Response<ApiResponse<ResgatesExtratoApi>>

    @GET("user/voucher/saldo")
    suspend fun consultarSaldoResgate(): Response<ApiResponse<SaldoResgate>>

    @POST("user/voucher/resgatar")
    suspend fun resgatarVoucher(@Body pedido: ResgateRequest): Response<ApiResponse<ResgateResposta>>
}

data class UsuarioResponse(val usuario: Usuario? = null)
data class ConversaResponse(val conversa: ConversaApi? = null)
data class MensagemResponse(val mensagem: MensagemApi? = null)
data class EntregaResponse(val entrega: EntregaApi? = null)
