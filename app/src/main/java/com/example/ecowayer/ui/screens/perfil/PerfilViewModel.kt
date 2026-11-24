package com.example.ecowayer.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.Usuario
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.service.buscarAnunciosDoUsuario
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val usuario: Usuario? = null,
    val dataCadastro: String? = null,
    val carregando: Boolean = true,
    val erro: Boolean = false,
    val anunciosPublicados: Int? = null
)

class PerfilViewModel : ViewModel() {
    private val _estado = MutableStateFlow(PerfilUiState())
    val estado = _estado.asStateFlow()

    private var carregando = false

    fun carregar() {
        val usuarioLocal = SessionStore.currentUser()
        if (carregando) return
        if (usuarioLocal == null) {
            _estado.value = PerfilUiState(carregando = false)
            return
        }
        carregando = true
        _estado.update {
            if (it.usuario?.id == usuarioLocal.id) {
                it.copy(usuario = it.usuario.copy(foto = usuarioLocal.urlFoto() ?: it.usuario.urlFoto()), erro = false)
            } else {
                PerfilUiState(usuario = usuarioLocal)
            }
        }

        viewModelScope.launch {
            try {
                coroutineScope {
                    launch { carregarUsuario(usuarioLocal) }
                    launch { contarAnuncios(usuarioLocal.id) }
                }
            } finally {
                carregando = false
            }
        }
    }

    private suspend fun carregarUsuario(usuarioLocal: Usuario) {
        try {
            val resposta = RetrofitFactory.getEcoWayerApi().buscarMeuPerfil()
            val usuario = resposta.body()?.response?.usuario
            check(resposta.isSuccessful && usuario != null && usuario.id == usuarioLocal.id)
            val atualizado = usuario.copy(foto = usuario.urlFoto() ?: usuarioLocal.urlFoto())
            if (SessionStore.currentUser()?.id == atualizado.id) SessionStore.saveUser(atualizado)
            _estado.update {
                it.copy(
                    usuario = atualizado,
                    dataCadastro = atualizado.data_cadastro?.let(::formatarDataCadastro),
                    carregando = false,
                    erro = false
                )
            }
        } catch (erro: CancellationException) {
            throw erro
        } catch (_: Exception) {
            _estado.update { it.copy(carregando = false, erro = true) }
        }
    }

    private suspend fun contarAnuncios(usuarioId: Int) {
        try {
            val anuncios = RetrofitFactory.getEcoWayerApi().buscarAnunciosDoUsuario(usuarioId)
            _estado.update { it.copy(anunciosPublicados = anuncios.size) }
        } catch (erro: CancellationException) {
            throw erro
        } catch (_: Exception) {
            // Mantém a última contagem carregada e omite o total se ainda for desconhecido.
        }
    }

}

private fun formatarDataCadastro(valor: String): String? {
    val dataApi = valor.take(10)
    if (!dataApi.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) return null
    val locale = Locale.forLanguageTag("pt-BR")
    val formatoApi = SimpleDateFormat("yyyy-MM-dd", locale).apply { isLenient = false }
    val data = runCatching { formatoApi.parse(dataApi) }.getOrNull() ?: return null
    return "Membro desde " + SimpleDateFormat("dd/MM/yyyy", locale).format(data)
}
