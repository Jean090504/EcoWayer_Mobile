package com.example.ecowayer

import com.example.ecowayer.model.lerPedidoQr
import com.example.ecowayer.model.conteudoPedidoQr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PedidoQrTest {
    @Test fun aceitaSomenteCodigoDePedidoCompleto() {
        val id = "123e4567-e89b-12d3-a456-426614174000"
        assertEquals("12345678", lerPedidoQr(conteudoPedidoQr(id, "12345678"))?.codigo)
        assertNull(lerPedidoQr("ecowayer:pedido:v1:$id:1234"))
        assertNull(lerPedidoQr("ecowayer:pedido:v1:1-1-1-1-1:12345678"))
        assertNull(lerPedidoQr("https://exemplo.com/qualquer-coisa"))
    }
}
