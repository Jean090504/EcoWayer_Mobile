package com.example.ecowayer.ui.screens.CadastroScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.CampoTexto
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.example.ecowayer.util.MaskTransformation

@Composable
fun CadastroScreen(
    onBack: () -> Unit,
    onAvancar: () -> Unit
) {
    // Estados temporários. Quando ligar na API, isso vai para um ViewModel.
    var nome by remember { mutableStateOf("") }
    var nascimento by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var aceitou by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = EcoColors.TextoPrimario
                )
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoColors.Verde)) { append("Criar ") }
                    withStyle(SpanStyle(color = EcoColors.Roxo)) { append("conta") }
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Formulário
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CampoTexto(
                label = "Nome completo",
                value = nome,
                onValueChange = { nome = it },
                placeholder = "Seu nome aqui"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    label = "Data de nascimento",
                    value = nascimento,
                    onValueChange = { nascimento = it },
                    placeholder = "DD/MM/AAAA",
                    keyboardType = KeyboardType.Number,
                    apenasDigitos = true,
                    maxLength = 8,
                    visualTransformation = MaskTransformation("##/##/####"),
                    modifier = Modifier.weight(1f)
                )
                CampoTexto(
                    label = "CPF",
                    value = cpf,
                    onValueChange = { cpf = it },
                    placeholder = "000.000.000-00",
                    keyboardType = KeyboardType.Number,
                    apenasDigitos = true,
                    maxLength = 11,
                    visualTransformation = MaskTransformation("###.###.###-##"),
                    modifier = Modifier.weight(1f)
                )
            }

            CampoTexto(
                label = "E-mail",
                value = email,
                onValueChange = { email = it },
                placeholder = "seuemail@exemplo.com",
                keyboardType = KeyboardType.Email
            )

            CampoTexto(
                label = "Telefone",
                value = telefone,
                onValueChange = { telefone = it },
                placeholder = "(00) 00000-0000",
                keyboardType = KeyboardType.Phone,
                apenasDigitos = true,
                maxLength = 11,
                visualTransformation = MaskTransformation("(##) #####-####")
            )

            CampoTexto(
                label = "Senha",
                value = senha,
                onValueChange = { senha = it },
                placeholder = "Crie uma senha",
                isPassword = true
            )

            CampoTexto(
                label = "Confirmar senha",
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it },
                placeholder = "Repita a senha",
                isPassword = true
            )

            // Aceite dos termos
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = aceitou,
                    onCheckedChange = { aceitou = it },
                    colors = CheckboxDefaults.colors(checkedColor = EcoColors.Verde)
                )
                Text(
                    text = buildAnnotatedString {
                        append("Aceito os ")
                        withStyle(
                            SpanStyle(
                                color = EcoColors.Verde,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) { append("Termos de Serviço") }
                        append(" e ")
                        withStyle(
                            SpanStyle(
                                color = EcoColors.Verde,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) { append("Privacidade") }
                    },
                    fontSize = 12.sp,
                    color = EcoColors.TextoPrimario
                )
            }
        }

        // Botão fixo embaixo
        Button(
            onClick = onAvancar,
            enabled = aceitou,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoColors.Verde,
                contentColor = Color.White,
                disabledContainerColor = EcoColors.Verde.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.8f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .height(52.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = "Avançar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CadastroScreenPreview() {
    EcowayerTheme {
        CadastroScreen(onBack = {}, onAvancar = {})
    }
}