package com.example.ecowayer.model

data class MensagemEco(val role: String, val text: String)
data class PerguntaEco(val message: String, val history: List<MensagemEco>)
data class RespostaEco(val resposta: String?)
