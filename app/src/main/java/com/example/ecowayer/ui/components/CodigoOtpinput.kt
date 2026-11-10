package com.example.ecowayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.ui.theme.EcoColors

/**
 * Campo de código com N caixinhas (padrão 6), como no protótipo.
 * Um único BasicTextField invisível cuida da digitação; as caixas só desenham.
 */
@Composable
fun CodigoOtpInput(
    codigo: String,
    onCodigoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    tamanho: Int = 6
) {
    BasicTextField(
        value = codigo,
        onValueChange = { novo ->
            if (novo.length <= tamanho && novo.all { it.isDigit() }) onCodigoChange(novo)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(tamanho) { i ->
                    val preenchido = i < codigo.length
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 40.dp, height = 46.dp)
                            .border(
                                BorderStroke(
                                    1.5.dp,
                                    if (preenchido) EcoColors.Verde else Color(0xFFE0E0E0)
                                ),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text(
                            text = if (preenchido) codigo[i].toString() else "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoColors.TextoPrimario
                        )
                    }
                }
            }
        }
    )
}