package com.example.ecowayer.ui.screens.configuracoes

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowayer.model.EnderecoConta
import com.example.ecowayer.ui.components.BarraInferior
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.ui.components.ImagemRemota
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.ModoTema
import com.example.ecowayer.ui.theme.PreferenciaTema

@Composable
fun DadosContaScreen(
    onBack: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    onQrCode: () -> Unit,
    onEncerrarSessao: () -> Unit,
    viewModel: DadosContaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current
    var mostrarFoto by remember { mutableStateOf(false) }
    var confirmarDesativacao by remember { mutableStateOf(false) }

    LaunchedEffect(estado.sair) {
        if (estado.sair) {
            estado.mensagem?.let { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
            onEncerrarSessao()
        }
    }
    BackHandler(enabled = estado.enviando) {}

    val voltar = { if (!estado.enviando) onBack() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { CabecalhoConfiguracoes("Informações ", "pessoais", voltar) },
        bottomBar = {
            BarraInferior(
                DestinoBarra.Perfil,
                onSelecionar = { if (!estado.enviando) onNavegar(it) },
                onQrCode = { if (!estado.enviando) onQrCode() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val conta = estado.conta
            when {
                estado.carregando -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                conta == null -> {
                    Text(estado.mensagem ?: "Não foi possível carregar os dados.")
                    TextButton(onClick = viewModel::carregar) { Text("Tentar novamente") }
                }
                else -> {
                    val dados = estado.formulario
                    val habilitado = !estado.enviando
                    FotoConta(dados.foto_url ?: conta.imagem, habilitado) { mostrarFoto = true }
                    CampoConta("Nome completo", dados.nome, habilitado) { viewModel.editar { copy(nome = it) } }
                    CamposEndereco(dados.endereco ?: EnderecoConta(), habilitado) { novo ->
                        viewModel.editar { copy(endereco = novo) }
                    }
                    CampoConta("Telefone", dados.telefone, habilitado, tipo = KeyboardType.Phone) {
                        viewModel.editar { copy(telefone = it) }
                    }
                    CampoConta("E-mail", dados.email, habilitado, tipo = KeyboardType.Email) {
                        viewModel.editar { copy(email = it) }
                    }
                    CampoConta("Confirmar e-mail", dados.confirmar_email, habilitado, tipo = KeyboardType.Email) {
                        viewModel.editar { copy(confirmar_email = it) }
                    }
                    CampoConta("Nova senha", dados.senha.orEmpty(), habilitado, dica = "Preencha para alterar", senha = true) {
                        viewModel.editar { copy(senha = it) }
                    }
                    CampoConta("Confirmar nova senha", dados.confirmar_senha.orEmpty(), habilitado, senha = true) {
                        viewModel.editar { copy(confirmar_senha = it) }
                    }
                    if (!dados.email.trim().equals(conta.email, ignoreCase = true) || !dados.senha.isNullOrEmpty()) {
                        CampoConta("Senha atual", dados.senha_atual.orEmpty(), habilitado, senha = true) {
                            viewModel.editar { copy(senha_atual = it) }
                        }
                    }
                    estado.mensagem?.let { Text(it, fontSize = 14.sp) }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = viewModel::descartar, enabled = habilitado) { Text("Descartar") }
                        Button(
                            onClick = viewModel::salvar,
                            enabled = habilitado,
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier.fillMaxWidth(0.45f)
                        ) { Text(if (estado.enviando) "Aguarde…" else "Salvar") }
                    }
                }
            }

            SeletorTema()
            if (conta != null) {
                PainelDesativacao(!estado.enviando) { confirmarDesativacao = true }
            }
        }
    }

    if (mostrarFoto) {
        DialogFoto(
            valorInicial = estado.formulario.foto_url ?: estado.conta?.imagem.orEmpty(),
            onFechar = { mostrarFoto = false },
            onAplicar = { url ->
                viewModel.editar { copy(foto_url = url) }
                mostrarFoto = false
            }
        )
    }
    if (confirmarDesativacao) {
        AlertDialog(
            onDismissRequest = { confirmarDesativacao = false },
            title = { Text("Desativar sua conta?") },
            text = { Text("Você perderá o acesso à conta e sua sessão será encerrada.") },
            dismissButton = { TextButton(onClick = { confirmarDesativacao = false }) { Text("Cancelar") } },
            confirmButton = {
                TextButton(onClick = {
                    confirmarDesativacao = false
                    viewModel.desativar()
                }) { Text("Desativar conta", color = MaterialTheme.colorScheme.error) }
            }
        )
    }
}

@Composable
private fun FotoConta(url: String?, habilitado: Boolean, onAlterar: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val avatar = Modifier.size(108.dp).clip(CircleShape)
        if (url.isNullOrBlank()) {
            Box(avatar.background(fundoCampo()), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Person, null, Modifier.size(45.dp), tint = EcoColors.Verde)
            }
        } else {
            ImagemRemota(url, "Foto de perfil", avatar)
        }
        TextButton(onClick = onAlterar, enabled = habilitado) { Text("Alterar foto") }
    }
}

@Composable
private fun DialogFoto(valorInicial: String, onFechar: () -> Unit, onAplicar: (String) -> Unit) {
    var url by remember { mutableStateOf(valorInicial) }
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Alterar foto") },
        text = {
            CampoConta("Link HTTPS da foto", url, true, tipo = KeyboardType.Uri, onChange = { url = it })
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } },
        confirmButton = {
            TextButton(onClick = { onAplicar(url.trim()) }, enabled = url.trim().startsWith("https://", true)) {
                Text("Aplicar")
            }
        }
    )
}

@Composable
private fun CamposEndereco(endereco: EnderecoConta, habilitado: Boolean, onChange: (EnderecoConta) -> Unit) {
    var menuUf by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("Localização", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(0.8f)) {
                TextButton(
                    onClick = { menuUf = true },
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth().background(fundoCampo(), RoundedCornerShape(9.dp)),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Text(endereco.sigla?.takeIf { it.isNotBlank() } ?: "UF do estado")
                    Icon(Icons.Outlined.KeyboardArrowDown, null)
                }
                DropdownMenu(expanded = menuUf, onDismissRequest = { menuUf = false }) {
                    estados.forEach { (sigla, nome) ->
                        DropdownMenuItem(text = { Text("$sigla • $nome") }, onClick = {
                            onChange(endereco.copy(sigla = sigla, estado = nome))
                            menuUf = false
                        })
                    }
                }
            }
            CampoConta("Cidade", endereco.cidade.orEmpty(), habilitado, Modifier.weight(1.2f), mostrarRotulo = false) {
                onChange(endereco.copy(cidade = it))
            }
        }
        CampoConta("Rua", endereco.rua.orEmpty(), habilitado, mostrarRotulo = false) {
            onChange(endereco.copy(rua = it))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoConta("Número", endereco.numero.orEmpty(), habilitado, Modifier.weight(0.7f), mostrarRotulo = false) {
                onChange(endereco.copy(numero = it))
            }
            CampoConta("Complemento", endereco.complemento.orEmpty(), habilitado, Modifier.weight(1.3f), mostrarRotulo = false) {
                onChange(endereco.copy(complemento = it))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoConta("Bairro", endereco.bairro.orEmpty(), habilitado, Modifier.weight(1f), mostrarRotulo = false) {
                onChange(endereco.copy(bairro = it))
            }
            CampoConta("CEP", endereco.cep.orEmpty(), habilitado, Modifier.weight(1f), tipo = KeyboardType.Number, mostrarRotulo = false) {
                onChange(endereco.copy(cep = it))
            }
        }
    }
}

@Composable
private fun CampoConta(
    rotulo: String,
    valor: String,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    dica: String? = null,
    senha: Boolean = false,
    tipo: KeyboardType = KeyboardType.Text,
    mostrarRotulo: Boolean = true,
    onChange: (String) -> Unit
) {
    val fundo = fundoCampo()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (mostrarRotulo) Text(rotulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        TextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            singleLine = true,
            placeholder = { Text(dica ?: rotulo, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = if (senha) KeyboardType.Password else tipo),
            visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fundo,
                unfocusedContainerColor = fundo,
                disabledContainerColor = fundo,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SeletorTema() {
    val contexto = LocalContext.current
    val atual by PreferenciaTema.modo.collectAsState()
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tema do aplicativo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        ModoTema.entries.forEach { modo ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(fundoCampo())
                    .selectable(atual == modo, role = Role.RadioButton, onClick = { PreferenciaTema.salvar(contexto, modo) })
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(modo.rotulo, color = MaterialTheme.colorScheme.primary)
                RadioButton(selected = atual == modo, onClick = null)
            }
        }
    }
}

@Composable
private fun PainelDesativacao(habilitado: Boolean, onClick: () -> Unit) {
    val cores = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cores.errorContainer).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Warning, null, tint = cores.onErrorContainer)
            Text("Desativar conta", fontWeight = FontWeight.Bold, color = cores.onErrorContainer)
        }
        Text(
            "A desativação bloqueia o acesso à sua conta.",
            textAlign = TextAlign.Center,
            color = cores.onErrorContainer,
            fontSize = 13.sp
        )
        Button(
            onClick = onClick,
            enabled = habilitado,
            colors = ButtonDefaults.buttonColors(containerColor = cores.error, contentColor = cores.onError),
            shape = RoundedCornerShape(9.dp)
        ) { Text("Desativar conta") }
    }
}

@Composable
private fun fundoCampo(): Color {
    val cores = MaterialTheme.colorScheme
    return if (cores.surface.luminance() > 0.5f) Color(0xFFF6F4FC) else cores.surfaceVariant
}

private val estados = listOf(
    "AC" to "Acre", "AL" to "Alagoas", "AP" to "Amapá", "AM" to "Amazonas", "BA" to "Bahia",
    "CE" to "Ceará", "DF" to "Distrito Federal", "ES" to "Espírito Santo", "GO" to "Goiás",
    "MA" to "Maranhão", "MT" to "Mato Grosso", "MS" to "Mato Grosso do Sul", "MG" to "Minas Gerais",
    "PA" to "Pará", "PB" to "Paraíba", "PR" to "Paraná", "PE" to "Pernambuco", "PI" to "Piauí",
    "RJ" to "Rio de Janeiro", "RN" to "Rio Grande do Norte", "RS" to "Rio Grande do Sul",
    "RO" to "Rondônia", "RR" to "Roraima", "SC" to "Santa Catarina", "SP" to "São Paulo",
    "SE" to "Sergipe", "TO" to "Tocantins"
)
