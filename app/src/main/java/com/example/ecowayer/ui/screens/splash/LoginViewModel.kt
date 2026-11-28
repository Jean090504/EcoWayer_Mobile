package com.example.ecowayer.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowayer.model.LoginRequest
import com.example.ecowayer.model.GoogleLoginRequest
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.mensagemErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

data class LoginUiState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val autenticado: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado

    fun entrar(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            _estado.value = LoginUiState(erro = "Informe seu e-mail e sua senha.")
            return
        }

        viewModelScope.launch {
            _estado.value = LoginUiState(carregando = true)
            try {
                val resposta = RetrofitFactory.getAuthService()
                    .login(LoginRequest(email.trim(), senha))
                    .awaitResponse()
                val corpo = resposta.body()

                if (resposta.isSuccessful && corpo?.status == true && corpo.response?.usuario != null) {
                    _estado.value = if (RetrofitFactory.salvarSessao(corpo.response.usuario)) {
                        LoginUiState(autenticado = true)
                    } else {
                        LoginUiState(erro = "O servidor não forneceu um cookie de sessão válido para a API.")
                    }
                } else {
                    _estado.value = LoginUiState(
                        erro = corpo?.message ?: resposta.mensagemErro("Não foi possível entrar. Tente novamente.")
                    )
                }
            } catch (_: Exception) {
                _estado.value = LoginUiState(
                    erro = "Não foi possível conectar ao servidor. Verifique se o backend está ativo."
                )
            }
        }
    }

    fun entrarComGoogle(credential: String) {
        if (credential.isBlank()) {
            _estado.value = LoginUiState(erro = "O Google não retornou uma credencial válida.")
            return
        }

        viewModelScope.launch {
            _estado.value = LoginUiState(carregando = true)
            try {
                val resposta = RetrofitFactory.getAuthService()
                    .loginGoogle(GoogleLoginRequest(credential))
                    .awaitResponse()
                val corpo = resposta.body()

                if (resposta.isSuccessful && corpo?.status == true && corpo.response?.usuario != null) {
                    _estado.value = if (RetrofitFactory.salvarSessao(corpo.response.usuario)) {
                        LoginUiState(autenticado = true)
                    } else {
                        LoginUiState(erro = "O servidor não forneceu um cookie de sessão válido para a API.")
                    }
                } else {
                    _estado.value = LoginUiState(
                        erro = corpo?.message ?: resposta.mensagemErro("Não foi possível entrar com Google. Tente novamente.")
                    )
                }
            } catch (_: Exception) {
                _estado.value = LoginUiState(
                    erro = "Não foi possível conectar ao servidor. Verifique se o backend está ativo."
                )
            }
        }
    }

    fun consumirSucesso() {
        _estado.value = LoginUiState()
    }

    fun reportarErroGoogle(mensagem: String) {
        _estado.value = LoginUiState(erro = mensagem)
    }
}
