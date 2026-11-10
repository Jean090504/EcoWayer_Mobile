package com.example.ecowayer.ui.screens.vereficacao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.components.CodigoOtpInput
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

/** Tela única reaproveitada para verificação de número e de e-mail. */
@Composable
fun VerificacaoScreen(
    tituloVerde: String,          // "Verificação de "
    tituloRoxo: String,           // "número" ou "e-mail"
    subtitulo: String,
    onBack: () -> Unit,
    onVerificar: (codigo: String) -> Unit,
    onReenviar: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }

    // --- INTEGRAÇÃO BACK-END (futuro) -------------------------------------
    // Mova estes estados para um ViewModel (ex.: VerificacaoViewModel):
    //
    // var carregando by remember { mutableStateOf(false) }
    // var erro by remember { mutableStateOf<String?>(null) }
    //
    // Se quiser exibir o erro, coloque um Text(erro!!, color = Color.Red)
    // logo abaixo do CodigoOtpInput.
    // ----------------------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
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
                    withStyle(SpanStyle(color = EcoColors.Verde)) { append(tituloVerde) }
                    withStyle(SpanStyle(color = EcoColors.Roxo)) { append(tituloRoxo) }
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Confirmar código", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = subtitulo,
                fontSize = 12.sp,
                color = EcoColors.TextoPrimario,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            CodigoOtpInput(codigo = codigo, onCodigoChange = { codigo = it })

            Button(
                onClick = { onVerificar(codigo) },
                enabled = codigo.length == 6,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoColors.Verde,
                    contentColor = Color.White,
                    disabledContainerColor = EcoColors.Verde.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(48.dp)
            ) {
                Text("Verificar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Row(modifier = Modifier.padding(top = 28.dp)) {
                Text("Não recebeu o código? ", fontSize = 12.sp, color = EcoColors.TextoPrimario)
                Text(
                    text = "Reenviar código",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoColors.Roxo,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onReenviar() }
                )
            }
        }
    }
}

@Composable
fun VerificacaoNumeroScreen(
    onBack: () -> Unit,
    onVerificado: () -> Unit
) {
    VerificacaoScreen(
        tituloVerde = "Verificação de ",
        tituloRoxo = "número",
        subtitulo = "Enviamos um código de 6 dígitos para o seu número",
        onBack = onBack,
        onVerificar = { codigo ->
            // --- INTEGRAÇÃO BACK-END (futuro) ---------------------------
            // viewModel.verificarTelefone(codigo)   // POST /verificacao/telefone
            //   onSuccess -> onVerificado()
            //   onError   -> erro = "Código inválido"
            // -------------------------------------------------------------
            onVerificado() // temporário: avança direto
        },
        onReenviar = {
            // viewModel.reenviarCodigoTelefone()    // POST /verificacao/telefone/reenviar
        }
    )
}

@Composable
fun VerificacaoEmailScreen(
    onBack: () -> Unit,
    onVerificado: () -> Unit
) {
    VerificacaoScreen(
        tituloVerde = "Verificação de ",
        tituloRoxo = "e-mail",
        subtitulo = "Enviamos um código de 6 dígitos para o seu e-mail",
        onBack = onBack,
        onVerificar = { codigo ->
            // --- INTEGRAÇÃO BACK-END (futuro) ---------------------------
            // viewModel.verificarEmail(codigo)      // POST /verificacao/email
            //   onSuccess -> onVerificado()
            //   onError   -> erro = "Código inválido"
            // -------------------------------------------------------------
            onVerificado() // temporário: avança direto
        },
        onReenviar = {
            // viewModel.reenviarCodigoEmail()       // POST /verificacao/email/reenviar
        }
    )
}

@Preview(showBackground = true)
@Composable
fun VerificacaoNumeroPreview() {
    EcowayerTheme { VerificacaoNumeroScreen(onBack = {}, onVerificado = {}) }
}