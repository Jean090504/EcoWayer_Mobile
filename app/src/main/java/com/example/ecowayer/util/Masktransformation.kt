package com.example.ecowayer.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Aplica uma máscara visual. O texto guardado continua só com os dígitos.
 * '#' = posição de um dígito. Exemplos:
 *  - Data:     "##/##/####"
 *  - CPF:      "###.###.###-##"
 *  - Telefone: "(##) #####-####"
 */
class MaskTransformation(private val mask: String) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = StringBuilder()
        var i = 0
        for (m in mask) {
            if (i >= digits.length) break
            if (m == '#') {
                out.append(digits[i])
                i++
            } else {
                out.append(m)
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformed = 0
                var count = 0
                for (m in mask) {
                    if (count == offset) break
                    transformed++
                    if (m == '#') count++
                }
                return transformed.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var count = 0
                for (idx in 0 until minOf(offset, mask.length)) {
                    if (mask[idx] == '#') count++
                }
                return count.coerceAtMost(digits.length)
            }
        }

        return TransformedText(AnnotatedString(out.toString()), mapping)
    }
}