package com.example.ecowayer.ui.screens.ecopontos

import android.location.Location
import com.example.ecowayer.data.model.PontoColetaDto
import java.text.Normalizer

fun String.normalizar(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase()

fun PontoColetaDto.bairro() = endereco.firstOrNull()?.bairro.orEmpty()

fun PontoColetaDto.enderecoCurto(): String {
    val e = endereco.firstOrNull() ?: return ""
    return "${e.rua}, ${e.numero} - ${e.bairro}"
}

fun PontoColetaDto.coincideComBusca(busca: String): Boolean {
    if (busca.isBlank()) return true
    val b = busca.normalizar()
    return nome.normalizar().contains(b) ||
            bairro().normalizar().contains(b) ||
            materialAceito.any { it.nome.normalizar().contains(b) }
}

/** "PET" (chip da Home) = "Plástico" (API). */
fun String.paraMaterialApi(): String = if (this == "PET") "Plástico" else this

fun PontoColetaDto.aceita(materiais: Collection<String>): Boolean =
    materiais.isEmpty() || materiais.any { m ->
        materialAceito.any { it.nome.normalizar() == m.paraMaterialApi().normalizar() }
    }

fun distanciaMetros(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
    val r = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, r)
    return r[0]
}

fun Float.formatarDistancia(): String =
    if (this < 1000) "${this.toInt()} m" else "%.1f km".format(this / 1000)