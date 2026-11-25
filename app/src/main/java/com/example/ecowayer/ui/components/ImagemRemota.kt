package com.example.ecowayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
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
    var carregada by remember(url) { mutableStateOf(false) }
    Box(modifier = modifier.background(Color(0xFFE0E0E0))) {
        if (!carregada) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = Color(0xFF9AA39B),
                modifier = Modifier.align(Alignment.Center).fillMaxSize(0.32f)
            )
        }
        url.urlPublica()?.let { imagem ->
            AsyncImage(
                model = imagem,
                contentDescription = descricao,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onState = { state -> carregada = state is AsyncImagePainter.State.Success }
            )
        }
    }
}
