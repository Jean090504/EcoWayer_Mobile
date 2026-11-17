package com.example.ecowayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ecowayer.BuildConfig

fun String?.urlPublica(): String? {
    val valor = this?.trim().orEmpty()
    if (valor.isBlank()) return null
    if (valor.startsWith("http://") || valor.startsWith("https://")) return valor
    val origem = BuildConfig.API_BASE_URL.substringBefore("/meraki/").trimEnd('/')
    return "$origem/${valor.trimStart('/')}"
}

@Composable
fun ImagemRemota(
    url: String?,
    descricao: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(modifier = modifier.background(Color(0xFFE0E0E0))) {
        url.urlPublica()?.let { imagem ->
            AsyncImage(
                model = imagem,
                contentDescription = descricao,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
