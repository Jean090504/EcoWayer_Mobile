package com.example.ecowayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.theme.EcoColors
import com.example.ecowayer.ui.theme.EcowayerTheme

@Composable
fun LoginPanel(
    onEntrar: (email: String, senha: String) -> Unit,
    onCriarConta: () -> Unit,
    onEsqueciSenha: () -> Unit,
    onLoginGoogle: () -> Unit = {},
    carregando: Boolean = false,
    erro: String? = null,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var lembrar by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Login",
            color = EcoColors.Roxo,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        CampoTexto(
            label = "E-mail",
            value = email,
            onValueChange = { email = it },
            placeholder = "seuemail@exemplo.com",
            keyboardType = KeyboardType.Email
        )

        Spacer(Modifier.height(14.dp))

        CampoTexto(
            label = "Senha",
            value = senha,
            onValueChange = { senha = it },
            placeholder = "Digite sua senha",
            isPassword = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = lembrar,
                    onCheckedChange = { lembrar = it },
                    colors = CheckboxDefaults.colors(checkedColor = EcoColors.Verde)
                )
                Text("Lembre-me", color = EcoColors.TextoPrimario, fontSize = 13.sp)
            }
            TextButton(onClick = onEsqueciSenha) {
                Text(
                    "Esqueci a senha",
                    color = EcoColors.Roxo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        erro?.let {
            Text(
                text = it,
                color = Color(0xFFB3261E),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        Button(
            onClick = { if (!carregando) onEntrar(email.trim(), senha) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EcoColors.Roxo),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (carregando) "Entrando..." else "Entrar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { if (!carregando) onLoginGoogle() },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F3F3), contentColor = Color(0xFF202124)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Entrar com Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Não tem conta?", color = EcoColors.Placeholder, fontSize = 13.sp)
            TextButton(onClick = onCriarConta) {
                Text(
                    "Criar nova conta",
                    color = EcoColors.Verde,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPanelPreview() {
    EcowayerTheme {
        LoginPanel(onEntrar = { _, _ -> }, onCriarConta = {}, onEsqueciSenha = {})
    }
}
