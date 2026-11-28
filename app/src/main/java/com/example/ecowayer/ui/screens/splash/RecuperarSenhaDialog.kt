package com.example.ecowayer.ui.screens.splash

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.mensagemErro
import kotlinx.coroutines.launch

@Composable
fun RecuperarSenhaDialog(onClose: () -> Unit) {
    var etapa by remember { mutableStateOf(0) }
    var email by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var desafio by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Recuperar senha") },
        text = {
            Column {
                Text(when (etapa) {
                    0 -> "Informe seu e-mail para receber um código."
                    1 -> "Digite o código recebido por e-mail."
                    else -> "Crie uma nova senha."
                })
                if (etapa == 0) OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
                if (etapa == 1) OutlinedTextField(codigo, { codigo = it.filter(Char::isDigit).take(6) }, label = { Text("Código de 6 dígitos") }, modifier = Modifier.fillMaxWidth())
                if (etapa == 2) OutlinedTextField(senha, { senha = it }, label = { Text("Nova senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                if (erro != null) Text(erro!!, modifier = Modifier.padding(top = 8.dp))
                if (etapa == 1) TextButton(onClick = { scope.launch {
                    try {
                        val resposta = RetrofitFactory.getCadastroService().reenviarVerificacao(mapOf("desafio" to desafio))
                        erro = if (resposta.isSuccessful) "Código reenviado." else resposta.mensagemErro("Não foi possível reenviar.")
                    } catch (falha: Exception) { erro = falha.message }
                } }) { Text("Reenviar código") }
            }
        },
        confirmButton = {
            Button(onClick = { scope.launch {
                carregando = true
                erro = null
                try {
                    val api = RetrofitFactory.getCadastroService()
                    when (etapa) {
                        0 -> {
                            val resposta = api.iniciarVerificacao(mapOf("tipo" to "email", "destino" to email, "finalidade" to "recuperacao"))
                            desafio = resposta.body()?.desafio?.takeIf { resposta.isSuccessful && it.isNotBlank() }
                                ?: error(resposta.mensagemErro("Não foi possível enviar o e-mail."))
                            etapa = 1
                        }
                        1 -> {
                            val resposta = api.confirmarVerificacao(mapOf("desafio" to desafio, "codigo" to codigo))
                            if (!resposta.isSuccessful || resposta.body()?.confirmado != true) error(resposta.mensagemErro("Código inválido."))
                            etapa = 2
                        }
                        else -> {
                            val resposta = api.recuperarSenha(mapOf("desafio_email" to desafio, "nova_senha" to senha))
                            if (!resposta.isSuccessful) error(resposta.mensagemErro("Não foi possível alterar a senha."))
                            onClose()
                        }
                    }
                } catch (falha: Exception) { erro = falha.message ?: "Não foi possível continuar." }
                finally { carregando = false }
            } }, enabled = !carregando) { Text(if (carregando) "Aguarde..." else "Continuar") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } }
    )
}
