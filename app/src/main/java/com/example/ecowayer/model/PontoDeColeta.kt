package com.example.ecowayer.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val status: Boolean,
    val status_code: Int,
    val message: String? = null,
    val response: T? = null
)

data class LoginRequest(
    val email: String,
    val senha: String
)

data class LoginResponse(
    val usuario: Usuario? = null
)

data class Usuario(
    val id: Int = 0,
    val nome: String = "",
    val email: String = "",
    val foto: String? = null,
    val data_cadastro: String? = null,
    val imagem: ImagemPerfilApi? = null
) {
    fun urlFoto() = foto?.takeIf { it.isNotBlank() } ?: imagem?.url?.takeIf { it.isNotBlank() }
}

data class ImagemPerfilApi(val url: String? = null)

data class Genero(
    val id: Int = 0,
    val nome: String = "",
    val sigla: String = ""
)

data class GeneroLista(
    val genero: List<Genero> = emptyList()
)

data class CadastroDraft(
    val nome: String,
    val dataNascimento: String,
    val cpf: String,
    val email: String,
    val telefone: String,
    val senha: String,
    val idGenero: Int
)

data class EnderecoDraft(
    val cep: String,
    val cidade: String,
    val estado: String,
    val bairro: String,
    val rua: String,
    val numero: String,
    val complemento: String
)

data class EnderecoCadastroRequest(
    val cep: String,
    val rua: String,
    val numero: String,
    val complemento: String?,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val sigla: String,
    val id_externo: String = ""
)

data class EnderecoCriado(val id: Int = 0)

data class EnderecoResponse(val endereco: EnderecoCriado? = null)

data class UsuarioCadastroRequest(
    val nome: String,
    val cpf: String,
    val data_nascimento: String,
    val id_genero: Int,
    val email: String,
    val telefone: String,
    val senha: String,
    val id_endereco: Int,
    val desafio_email: String,
    val desafio_telefone: String
)

data class PontoColetaLista(
    val count: Int = 0,
    @SerializedName("ponto_coleta") val pontos: List<PontoColeta> = emptyList()
)

data class PontoColeta(
    val id: Int = 0,
    val nome: String = "",
    val descricao: String = "",
    val status: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerializedName("id_categoria") val idCategoria: Int = 0,
    val endereco: List<EnderecoPonto> = emptyList(),
    @SerializedName("material_aceito") val materiais: List<MaterialAceito> = emptyList()
)

data class RotaRequest(
    val origem: Coordenada,
    val destino: Coordenada
)

data class Coordenada(
    val latitude: Double,
    val longitude: Double
)

data class RotaCalculada(
    val distancia_metros: Double = 0.0,
    val duracao_segundos: Double = 0.0,
    val distancia_texto: String = "",
    val duracao_texto: String = "",
    val polyline: String = ""
)

data class GeocodificarRequest(val consulta: String)

data class LocalizacaoEncontrada(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val endereco_formatado: String = ""
)

data class BuscaLocaisRequest(val latitude: Double, val longitude: Double)

data class LocalReciclagem(
    val id: String = "",
    val nome: String = "",
    val endereco: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val maps_url: String = ""
)

data class LocaisReciclagemResponse(
    val locais: List<LocalReciclagem> = emptyList()
)

data class AnuncioApi(
    val id: Int = 0,
    val nome: String = "",
    val descricao: String? = null,
    val preco: Double? = null,
    val quantidade: Double? = null,
    val unidade: String? = null,
    val peso_kg: Double? = null,
    val status: String = "ativo",
    val data_publicacao: String? = null,
    val imagem: String? = null,
    val imagens: List<String> = emptyList(),
    val id_usuario: Int = 0,
    val id_categoria: Int = 0,
    val id_modalidade: Int = 0,
    val id_condicao_item: Int = 0,
    val categoria: String? = null,
    val categoria_nome: String? = null,
    val modalidade_nome: String? = null,
    val condicao_nome: String? = null,
    val vendedor: Usuario? = null
)

fun AnuncioApi.pertenceA(usuarioId: Int?): Boolean =
    usuarioId != null && donoId() == usuarioId

fun AnuncioApi.donoId(): Int? = id_usuario.takeIf { it > 0 } ?: vendedor?.id?.takeIf { it > 0 }

data class AnuncioLista(val anuncio: List<AnuncioApi> = emptyList())
data class AnuncioDetalhe(val anuncio: AnuncioApi? = null)
data class AnuncioOpcoes(
    val categorias: List<OpcaoApi> = emptyList(),
    val modalidades: List<OpcaoApi> = emptyList(),
    val condicoes: List<OpcaoApi> = emptyList()
)
data class OpcaoApi(val id: Int = 0, val nome: String = "")

data class TipoDenuncia(val id: Int = 0, val tipo: String = "", val nome: String = "")
data class TipoDenunciaLista(val tipo_denuncias: List<TipoDenuncia> = emptyList())
data class DenunciaRequest(
    val descricao: String,
    val id_tipo_denuncias: Int,
    val id_denunciante: Int,
    val mensagens: List<Int> = emptyList(),
    val imagens: List<Int> = emptyList(),
    val id_anuncio: Int? = null
)
data class DenunciaResponse(val denuncia: DenunciaApi? = null)
data class DenunciaApi(val id: Int = 0, val descricao: String = "", val status: String? = null)

data class ConversaApi(
    val id: Int = 0,
    val id_anuncio: Int = 0,
    val id_anunciante: Int = 0,
    val id_interessado: Int = 0,
    val anunciante: String? = null,
    val interessado: String? = null,
    val foto_anunciante: String? = null,
    val foto_interessado: String? = null,
    val nome_contato: String? = null,
    val contato: Usuario? = null,
    val ultima_mensagem: String? = null,
    @com.google.gson.annotations.SerializedName(value = "ultimo_envio", alternate = ["data_ultima_mensagem"])
    val data_ultima_mensagem: String? = null,
    val nao_lidas: Int = 0
) {
    fun outroParticipante(idUsuario: Int?): Usuario {
        val participante = when (idUsuario) {
            id_anunciante -> Usuario(id_interessado, interessado.orEmpty(), foto = foto_interessado)
            id_interessado -> Usuario(id_anunciante, anunciante.orEmpty(), foto = foto_anunciante)
            else -> Usuario()
        }
        return participante.copy(
            nome = participante.nome.ifBlank {
                nome_contato.orEmpty().ifBlank { contato?.nome.orEmpty() }
            },
            foto = participante.foto?.takeIf { it.isNotBlank() } ?: contato?.foto
        )
    }
}
data class ConversaLista(val conversa: List<ConversaApi> = emptyList())
data class MensagemApi(
    val id: Int = 0,
    val id_conversa: Int = 0,
    val id_remetente: Int = 0,
    val texto: String = "",
    @SerializedName(value = "envio", alternate = ["criado_em"])
    val criado_em: String? = null
)
data class MensagemLista(
    @SerializedName(value = "mensagem", alternate = ["mensagens"])
    val mensagem: List<MensagemApi>? = null
)
data class MensagemRequest(val id_conversa: Int, val texto: String)
data class EntregaApi(val id: Int = 0, val id_usuario: Int = 0, val id_anuncio: Int? = null, val peso: Double? = null, val status: String? = null, val data_entrega: String? = null)
data class EntregaLista(val entrega: List<EntregaApi> = emptyList())
data class PontoRegistro(val id: Int = 0, val id_usuario: Int = 0, val qntd_pontos: Double = 0.0, val id_categoria: Int? = null, val id_entrega: Int? = null, val data_registro: String? = null)
data class PontoRegistroLista(val pontos: List<PontoRegistro> = emptyList())

data class CarteiraDados(
    val resumo: ResumoCarteiraApi = ResumoCarteiraApi(),
    val missoes: List<MissaoCarteiraApi> = emptyList(),
    val vouchers: List<VoucherCarteiraApi> = emptyList(),
    val extrato: List<MovimentoCarteiraApi> = emptyList()
)

data class ResumoCarteiraApi(
    val pontos: Int = 0,
    val nivel_atual: NivelImpactoApi? = null,
    val proximo_nivel: NivelImpactoApi? = null,
    val progresso: Float = 0f,
    val pontos_para_proximo_nivel: Int = 0
)

data class NivelImpactoApi(
    val id: Int = 0,
    val nome: String = "",
    val numero: Int = 0,
    val meta: Double = 0.0,
    val unidade: String = ""
)

data class MissaoCarteiraApi(
    val id: Int = 0,
    val id_missao: Int = 0,
    val titulo: String = "",
    val descricao: String = "",
    val meta: Int = 0,
    val periodo_dias: Int = 0,
    val pontos_bonus: Int = 0,
    val progresso: Int = 0,
    val status: String = "",
    val data_inicio: String? = null,
    val data_fim: String? = null
)

data class VoucherCarteiraApi(
    val id: Int = 0,
    val slug_api: String = "",
    val nome: String = "",
    val valor_reais: Double = 0.0,
    val pontos_resgate: Int = 0,
    val imagem_url: String? = null,
    val categoria: String = "Gift card",
    val status: String = "ativo"
)

data class MovimentoCarteiraApi(
    val id: Int = 0,
    val qntd_pontos: Int = 0,
    val data_registro: String? = null,
    val id_categoria: Int? = null,
    val id_entrega: Int? = null,
    val categoria_nome: String? = null
)

data class NotificacaoApi(val id: Int = 0, val titulo: String? = null, val descricao: String? = null, val lida: Boolean = false, val criado_em: String? = null)
data class NotificacaoLista(val notificacao: List<NotificacaoApi> = emptyList())

data class EnderecoPonto(
    val id: Int = 0,
    val cep: String = "",
    val rua: String = "",
    val numero: String = "",
    val complemento: String? = null,
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = "",
    val sigla: String = "",
    @SerializedName("id_externo") val idExterno: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class MaterialAceito(
    val id: Int = 0,
    val nome: String = "",
    val icone: String = ""
)
