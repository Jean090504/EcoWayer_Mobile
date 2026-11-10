package com.example.ecowayer.ui.screens.endereco

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.CampoTexto
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme
import com.example.ecowayer.util.MaskTransformation

@Composable
fun EnderecoScreen(
    onBack: () -> Unit,
    onFinalizar: () -> Unit
) {
    var cep by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var logradouro by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var complemento by remember { mutableStateOf("") }

    // --- INTEGRAÇÃO BACK-END / VIACEP (futuro) ----------------------------
    // Autopreencher ao completar o CEP (8 dígitos). Exemplo, com ViewModel:
    //
    // LaunchedEffect(cep) {
    //     if (cep.length == 8) {
    //         val end = viewModel.buscarCep(cep)   // GET https://viacep.com.br/ws/{cep}/json/
    //         if (end != null) {
    //             cidade = end.localidade
    //             estado = end.uf
    //             bairro = end.bairro
    //             logradouro = end.logradouro
    //         }
    //     }
    // }
    // ----------------------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = EcoColors.TextoPrimario)
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoColors.Verde)) { append("Seu ") }
                    withStyle(SpanStyle(color = EcoColors.Roxo)) { append("endereço") }
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CampoTexto(
                label = "CEP",
                value = cep,
                onValueChange = { cep = it },
                placeholder = "00000-000",
                keyboardType = KeyboardType.Number,
                apenasDigitos = true,
                maxLength = 8,
                visualTransformation = MaskTransformation("#####-###")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    label = "Cidade",
                    value = cidade,
                    onValueChange = { cidade = it },
                    placeholder = "",
                    modifier = Modifier.weight(2f)
                )
                CampoTexto(
                    label = "Estado",
                    value = estado,
                    onValueChange = { estado = it.uppercase() },
                    placeholder = "UF",
                    maxLength = 2,
                    modifier = Modifier.weight(1f)
                )
            }

            CampoTexto(
                label = "Bairro",
                value = bairro,
                onValueChange = { bairro = it },
                placeholder = ""
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    label = "Logradouro",
                    value = logradouro,
                    onValueChange = { logradouro = it },
                    placeholder = "",
                    modifier = Modifier.weight(2f)
                )
                CampoTexto(
                    label = "Número",
                    value = numero,
                    onValueChange = { numero = it },
                    placeholder = "24",
                    keyboardType = KeyboardType.Number,
                    apenasDigitos = true,
                    modifier = Modifier.weight(1f)
                )
            }

            CampoTexto(
                label = "Complemento",
                value = complemento,
                onValueChange = { complemento = it },
                placeholder = ""
            )
        }

        Button(
            onClick = {
                // --- INTEGRAÇÃO BACK-END (futuro) -------------------------
                // Aqui você junta os dados de TODAS as telas do cadastro
                // (nome, cpf, e-mail, telefone, senha + endereço) e envia:
                //
                // viewModel.finalizarCadastro(
                //     cep, cidade, estado, bairro, logradouro, numero, complemento
                // )                                   // POST /usuarios
                //   onSuccess -> onFinalizar()
                //   onError   -> mostrar mensagem
                // ---------------------------------------------------------
                onFinalizar() // temporário
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoColors.Verde,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .height(52.dp)
        ) {
            Text("Finalizar cadastro", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnderecoScreenPreview() {
    EcowayerTheme { EnderecoScreen(onBack = {}, onFinalizar = {}) }
}