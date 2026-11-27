package com.example.ecowayer.ui.screens.assistente

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowayer.R
import com.example.ecowayer.model.MensagemEco
import com.example.ecowayer.model.PerguntaEco
import com.example.ecowayer.service.RetrofitFactory
import kotlinx.coroutines.launch

private val Roxo = Color(0xFF724BC2)
private val Fundo = Color(0xFFFAF8FC)
private val Sugestoes = listOf(
    "Como encontro anúncios perto de mim?",
    "Como funcionam os EcoPontos?",
    "Como acompanho meus pontos?"
)

@Composable
fun AssistenteEcoScreen(onVoltar: () -> Unit) {
    val mensagens = remember { mutableStateListOf<MensagemEco>() }
    var texto by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    val lista = rememberLazyListState()

    fun enviar(pergunta: String) {
        val mensagem = pergunta.trim()
        if (mensagem.isEmpty() || carregando) return
        val historico = mensagens.takeLast(10).toList()
        mensagens += MensagemEco("user", mensagem)
        texto = ""
        carregando = true
        escopo.launch {
            try {
                val resposta = RetrofitFactory.getEcoWayerApi().conversarEco(PerguntaEco(mensagem, historico))
                val respostaTexto = resposta.body()?.resposta?.takeIf { resposta.isSuccessful && it.isNotBlank() }
                    ?: "Não consegui responder agora. Tente novamente."
                mensagens += MensagemEco("model", respostaTexto)
            } catch (_: Exception) {
                mensagens += MensagemEco("model", "Não consegui me conectar. Verifique sua internet e tente novamente.")
            } finally { carregando = false }
        }
    }

    LaunchedEffect(mensagens.size, carregando) {
        if (mensagens.isNotEmpty()) lista.animateScrollToItem(mensagens.lastIndex + if (carregando) 1 else 0)
    }

    Column(Modifier.fillMaxSize().background(Fundo).imePadding()) {
        Box(Modifier.fillMaxWidth().height(152.dp).background(Brush.horizontalGradient(listOf(Color(0xFF603AAF), Roxo, Color(0xFF4CAF50))))) {
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, top = 12.dp, end = 12.dp), verticalAlignment = Alignment.Top) {
                IconButton(onClick = onVoltar) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar", tint = Color.White) }
                Column(Modifier.padding(start = 4.dp, top = 5.dp)) {
                    Text("ECOWAYER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f), letterSpacing = 2.sp)
                    Text("Converse com a Eco", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Ajuda com anúncios, pontos e EcoPontos", fontSize = 12.sp, color = Color.White.copy(alpha = 0.88f))
                }
            }
            Image(painterResource(R.drawable.assistente_eco), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp).height(94.dp), contentScale = ContentScale.Fit)
        }

        LazyColumn(state = lista, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (mensagens.isEmpty()) item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(18.dp)) {
                    Text("Olá! Sou a Eco.", fontWeight = FontWeight.Bold, color = Roxo, fontSize = 17.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Posso ajudar você a explorar anúncios, entender seus pontos ou encontrar EcoPontos.", color = Color(0xFF5F5968), fontSize = 14.sp)
                    Spacer(Modifier.height(14.dp))
                    Sugestoes.forEach { sugestao ->
                        Text(sugestao, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1EBFA)).clickable { enviar(sugestao) }.padding(12.dp), color = Roxo, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                }
            }
            items(mensagens.toList()) { mensagem ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mensagem.role == "user") Arrangement.End else Arrangement.Start) {
                    Text(mensagem.text, modifier = Modifier.fillMaxWidth(0.88f).clip(RoundedCornerShape(18.dp)).background(if (mensagem.role == "user") Roxo else Color.White).padding(14.dp), color = if (mensagem.role == "user") Color.White else Color(0xFF403748), fontSize = 14.sp, lineHeight = 21.sp)
                }
            }
            if (carregando) item { Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Roxo); Text("Eco está preparando uma resposta...", Modifier.padding(start = 8.dp), fontSize = 12.sp, color = Color.Gray) } }
        }

        Row(Modifier.fillMaxWidth().background(Color.White).padding(12.dp), verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(value = texto, onValueChange = { if (it.length <= 2000) texto = it }, modifier = Modifier.weight(1f), placeholder = { Text("Pergunte à Eco...") }, maxLines = 4, shape = RoundedCornerShape(16.dp))
            IconButton(onClick = { enviar(texto) }, enabled = texto.isNotBlank() && !carregando, modifier = Modifier.padding(start = 8.dp).size(50.dp).clip(CircleShape).background(Roxo)) {
                Icon(Icons.Outlined.Send, contentDescription = "Enviar mensagem", tint = Color.White)
            }
        }
    }
}
