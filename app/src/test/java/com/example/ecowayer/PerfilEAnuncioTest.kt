package com.example.ecowayer

import com.example.ecowayer.model.AnuncioApi
import com.example.ecowayer.model.Usuario
import com.example.ecowayer.model.pertenceA
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PerfilEAnuncioTest {
    @Test
    fun usaImagemAninhadaQuandoApiDePerfilNaoRetornaFotoDireta() {
        val usuario = Gson().fromJson(
            """{"id":16,"nome":"Usuário","imagem":{"id":5,"url":"/uploads/perfil.png"}}""",
            Usuario::class.java
        )
        assertEquals("/uploads/perfil.png", usuario.urlFoto())
    }

    @Test
    fun identificaMesmoDonoNaListaENoDetalheDoAnuncio() {
        val lista = AnuncioApi(id = 1, id_usuario = 16)
        val detalhe = AnuncioApi(id = 1, vendedor = Usuario(id = 16))

        assertTrue(lista.pertenceA(16))
        assertTrue(detalhe.pertenceA(16))
        assertFalse(detalhe.pertenceA(17))
        assertFalse(AnuncioApi(id = 1).pertenceA(null))
    }
}
