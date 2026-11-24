package com.example.ecowayer.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ModoTema(val rotulo: String) {
    Sistema("Padrão do sistema"),
    Claro("Tema claro"),
    Escuro("Tema escuro")
}

object PreferenciaTema {
    private val _modo = MutableStateFlow(ModoTema.Sistema)
    val modo = _modo.asStateFlow()

    fun carregar(contexto: Context) {
        val valor = preferencias(contexto).getString("modo", null)
        _modo.value = ModoTema.entries.firstOrNull { it.name == valor } ?: ModoTema.Sistema
    }

    fun salvar(contexto: Context, modo: ModoTema) {
        preferencias(contexto).edit().putString("modo", modo.name).apply()
        _modo.value = modo
    }

    private fun preferencias(contexto: Context) =
        contexto.getSharedPreferences("ecowayer_tema", Context.MODE_PRIVATE)
}
