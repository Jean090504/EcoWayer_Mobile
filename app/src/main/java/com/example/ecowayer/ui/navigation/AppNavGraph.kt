package com.example.ecowayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ecowayer.BuildConfig
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ecowayer.ui.components.DestinoBarra
import com.example.ecowayer.service.RetrofitFactory
import com.example.ecowayer.service.EcoWayerRepository
import com.example.ecowayer.service.SessionStore
import com.example.ecowayer.model.CadastroDraft
import com.example.ecowayer.model.EnderecoCadastroRequest
import com.example.ecowayer.model.EnderecoDraft
import com.example.ecowayer.model.UsuarioCadastroRequest
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import com.example.ecowayer.ui.screens.CadastroScreen.CadastroScreen
import com.example.ecowayer.ui.screens.boasvindas.BoasVindasScreen
import com.example.ecowayer.ui.screens.carteira.CarteiraScreen
import com.example.ecowayer.ui.screens.carteira.ExtratoPontosScreen
import com.example.ecowayer.ui.screens.configuracoes.ConfiguracoesScreen
import com.example.ecowayer.ui.screens.configuracoes.DadosContaScreen
import com.example.ecowayer.ui.screens.conversa.ConversaScreen
import com.example.ecowayer.ui.screens.criarAnuncio.CriarAnuncioScreen
import com.example.ecowayer.ui.screens.denuncia.DenunciarScreen
import com.example.ecowayer.ui.screens.detalhes.DetalhesAnuncioScreen
import com.example.ecowayer.ui.screens.detalhes.EditarAnuncioScreen
import com.example.ecowayer.ui.screens.ecopontos.EcoPontosScreen
import com.example.ecowayer.ui.screens.endereco.EnderecoScreen
import com.example.ecowayer.ui.screens.entrega.RegistrarEntregaScreen
import com.example.ecowayer.ui.screens.home.HomeScreen
import com.example.ecowayer.ui.screens.assistente.AssistenteEcoScreen
import com.example.ecowayer.ui.screens.insignias.InsigniasScreen
import com.example.ecowayer.ui.screens.loja.LojaScreen
import com.example.ecowayer.ui.screens.mensagens.MensagensScreen
import com.example.ecowayer.ui.screens.meusanuncios.MeusAnunciosScreen
import com.example.ecowayer.ui.screens.notificacoes.NotificacoesScreen
import com.example.ecowayer.ui.screens.perfil.PerfilScreen
import com.example.ecowayer.ui.screens.pedidos.MeusPedidosScreen
import com.example.ecowayer.ui.screens.splash.SplashScreen
import com.example.ecowayer.ui.screens.splash.RecuperarSenhaDialog
import com.example.ecowayer.ui.screens.trocapontos.TrocarPontosScreen
import com.example.ecowayer.ui.screens.vereficacao.VerificacaoEmailScreen
import com.example.ecowayer.ui.screens.vereficacao.VerificacaoNumeroScreen

internal fun NavController.navegarPelaBarra(destino: DestinoBarra) {
    val rota = when (destino) {
        DestinoBarra.Inicio -> Routes.Home.route
        DestinoBarra.Loja -> Routes.Loja.route
        DestinoBarra.Carteira -> Routes.Carteira.route
        DestinoBarra.Perfil -> Routes.Perfil.route
    }

    val atual = currentDestination?.route
    if (atual == rota) return
    val areaCarteira = atual == Routes.Carteira.route || atual == Routes.ExtratoPontos.route
    if (areaCarteira) {
        if (!popBackStack(rota, inclusive = false)) {
            navigate(rota) {
                popUpTo(Routes.Home.route)
                launchSingleTop = true
            }
        }
        return
    }

    val telasDaBarra = listOf(Routes.Home, Routes.Loja, Routes.Carteira, Routes.Perfil)
    val saindoDaAba = telasDaBarra.any { it.route == atual }
    if (!saindoDaAba && popBackStack(rota, inclusive = false)) return
    navigate(rota) {
        popUpTo(Routes.Home.route) { saveState = saindoDaAba }
        launchSingleTop = true
        restoreState = true
    }
}

/** Botão central (QRCode) da barra inferior: abre "Registrar entrega". */
private fun NavController.abrirRegistrarEntrega() {
    navigate(Routes.RegistrarEntrega.route) {
        launchSingleTop = true   // não abre outra cópia se já estiver nela
    }
}

private fun String.toIsoDate(): String {
    val digitos = filter(Char::isDigit)
    return if (digitos.length == 8) {
        "${digitos.substring(4, 8)}-${digitos.substring(2, 4)}-${digitos.substring(0, 2)}"
    } else {
        this
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var cadastro by remember { mutableStateOf<CadastroDraft?>(null) }
    var cadastroCarregando by remember { mutableStateOf(false) }
    var cadastroErro by remember { mutableStateOf<String?>(null) }
    var desafioTelefone by remember { mutableStateOf<String?>(null) }
    var desafioEmail by remember { mutableStateOf<String?>(null) }
    var mostrarRecuperacao by remember { mutableStateOf(false) }
    var publicando by remember { mutableStateOf(false) }
    var erroPublicacao by remember { mutableStateOf<String?>(null) }
    val sessaoRecusada by RetrofitFactory.sessaoRecusada.collectAsState()
    val modoAutoLogin = remember { BuildConfig.DEBUG && BuildConfig.DEV_AUTO_LOGIN && !sessaoRecusada }
    var autoLoginPronto by remember { mutableStateOf(!modoAutoLogin || SessionStore.isLoggedIn()) }
    var autoLoginFalhou by remember { mutableStateOf(false) }

    LaunchedEffect(modoAutoLogin) {
        if (modoAutoLogin && !SessionStore.isLoggedIn()) {
            try {
                val resposta = RetrofitFactory.getAuthService().login(
                    com.example.ecowayer.model.LoginRequest(
                        BuildConfig.DEV_AUTO_LOGIN_EMAIL,
                        BuildConfig.DEV_AUTO_LOGIN_PASSWORD
                    )
                ).awaitResponse()
                val usuario = resposta.body()?.response?.usuario
                if (!resposta.isSuccessful || resposta.body()?.status != true || usuario == null) {
                    autoLoginFalhou = true
                } else {
                    autoLoginFalhou = !RetrofitFactory.salvarSessao(usuario)
                }
            } catch (_: Exception) {
                autoLoginFalhou = true
            } finally {
                autoLoginPronto = true
            }
        }
    }

    if (!autoLoginPronto) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val destinoInicial = remember {
        if (SessionStore.isLoggedIn() && !autoLoginFalhou) Routes.Home.route else Routes.Splash.route
    }
    LaunchedEffect(sessaoRecusada) {
        if (sessaoRecusada && navController.currentDestination?.route != Routes.Splash.route) {
            listOf(Routes.Home, Routes.Loja, Routes.Carteira, Routes.Perfil).forEach {
                navController.clearBackStack(it.route)
            }
            navController.navigate(Routes.Splash.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                sessaoRecusada = sessaoRecusada,
                onEntrar = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onCriarConta = { navController.navigate(Routes.Cadastro.route) },
                onEsqueciSenha = { mostrarRecuperacao = true }
            )
            if (mostrarRecuperacao) RecuperarSenhaDialog(onClose = { mostrarRecuperacao = false })
        }

        composable(Routes.Cadastro.route) {
            CadastroScreen(
                onBack = { navController.popBackStack() },
                erro = cadastroErro,
                carregando = cadastroCarregando,
                onAvancar = { dados ->
                    cadastro = dados
                    cadastroErro = null
                    scope.launch {
                        cadastroCarregando = true
                        try {
                            val resposta = RetrofitFactory.getCadastroService().iniciarVerificacao(
                                mapOf("tipo" to "telefone", "destino" to dados.telefone, "finalidade" to "cadastro")
                            )
                            desafioTelefone = resposta.body()?.desafio?.takeIf { resposta.isSuccessful && it.isNotBlank() }
                                ?: error(resposta.body()?.message ?: "Não foi possível enviar o código por SMS.")
                            navController.navigate(Routes.VerificacaoNumero.route)
                        } catch (falha: Exception) { cadastroErro = falha.message ?: "Não foi possível enviar o código por SMS." }
                        finally { cadastroCarregando = false }
                    }
                }
            )
        }

        composable(Routes.VerificacaoNumero.route) {
            VerificacaoNumeroScreen(
                onBack = { navController.popBackStack() },
                erro = cadastroErro,
                carregando = cadastroCarregando,
                onVerificar = { codigo -> scope.launch {
                    cadastroCarregando = true
                    cadastroErro = null
                    try {
                        val resposta = RetrofitFactory.getCadastroService().confirmarVerificacao(mapOf("desafio" to (desafioTelefone ?: ""), "codigo" to codigo))
                        if (!resposta.isSuccessful || resposta.body()?.confirmado != true) error(resposta.body()?.message ?: "Código de telefone inválido.")
                        val email = cadastro?.email ?: error("Dados do cadastro não encontrados.")
                        val envio = RetrofitFactory.getCadastroService().iniciarVerificacao(mapOf("tipo" to "email", "destino" to email, "finalidade" to "cadastro"))
                        desafioEmail = envio.body()?.desafio?.takeIf { envio.isSuccessful && it.isNotBlank() }
                            ?: error(envio.body()?.message ?: "Não foi possível enviar o e-mail.")
                        navController.navigate(Routes.VerificacaoEmail.route)
                    } catch (falha: Exception) { cadastroErro = falha.message ?: "Não foi possível verificar o telefone." }
                    finally { cadastroCarregando = false }
                } },
                onReenviar = { scope.launch {
                    try { val resposta = RetrofitFactory.getCadastroService().reenviarVerificacao(mapOf("desafio" to (desafioTelefone ?: "")))
                        if (!resposta.isSuccessful) error(resposta.body()?.message ?: "Não foi possível reenviar o SMS.")
                        cadastroErro = null
                    } catch (falha: Exception) { cadastroErro = falha.message }
                } }
            )
        }

        composable(Routes.VerificacaoEmail.route) {
            VerificacaoEmailScreen(
                onBack = { navController.popBackStack() },
                erro = cadastroErro,
                carregando = cadastroCarregando,
                onVerificar = { codigo -> scope.launch {
                    cadastroCarregando = true
                    cadastroErro = null
                    try {
                        val resposta = RetrofitFactory.getCadastroService().confirmarVerificacao(mapOf("desafio" to (desafioEmail ?: ""), "codigo" to codigo))
                        if (!resposta.isSuccessful || resposta.body()?.confirmado != true) error(resposta.body()?.message ?: "Código de e-mail inválido.")
                        navController.navigate(Routes.Endereco.route)
                    } catch (falha: Exception) { cadastroErro = falha.message ?: "Não foi possível verificar o e-mail." }
                    finally { cadastroCarregando = false }
                } },
                onReenviar = { scope.launch {
                    try { val resposta = RetrofitFactory.getCadastroService().reenviarVerificacao(mapOf("desafio" to (desafioEmail ?: "")))
                        if (!resposta.isSuccessful) error(resposta.body()?.message ?: "Não foi possível reenviar o e-mail.")
                        cadastroErro = null
                    } catch (falha: Exception) { cadastroErro = falha.message }
                } }
            )
        }

        composable(Routes.Endereco.route) {
            EnderecoScreen(
                onBack = { navController.popBackStack() },
                carregando = cadastroCarregando,
                erro = cadastroErro,
                onFinalizar = { endereco ->
                    val dados = cadastro ?: run {
                        cadastroErro = "Os dados do cadastro não foram encontrados."
                        return@EnderecoScreen
                    }
                    scope.launch {
                        cadastroCarregando = true
                        cadastroErro = null
                        try {
                            val servico = RetrofitFactory.getCadastroService()
                            val respostaEndereco = servico.criarEndereco(
                                EnderecoCadastroRequest(
                                    cep = endereco.cep,
                                    rua = endereco.rua,
                                    numero = endereco.numero,
                                    complemento = endereco.complemento.ifBlank { null },
                                    bairro = endereco.bairro,
                                    cidade = endereco.cidade,
                                    estado = endereco.estado,
                                    sigla = endereco.estado,
                                    id_externo = "mobile-${endereco.cep}-${endereco.numero}"
                                )
                            ).awaitResponse()
                            val idEndereco = respostaEndereco.body()?.response?.endereco?.id
                                ?.takeIf { it > 0 }
                                ?: error(respostaEndereco.body()?.message ?: "Não foi possível criar o endereço.")

                            val respostaUsuario = servico.criarUsuario(
                                UsuarioCadastroRequest(
                                    nome = dados.nome,
                                    cpf = dados.cpf,
                                    data_nascimento = dados.dataNascimento.toIsoDate(),
                                    id_genero = dados.idGenero,
                                    email = dados.email,
                                    telefone = dados.telefone,
                                    senha = dados.senha,
                                    id_endereco = idEndereco,
                                    desafio_email = desafioEmail ?: "",
                                    desafio_telefone = desafioTelefone ?: ""
                                )
                            ).awaitResponse()
                            if (!respostaUsuario.isSuccessful || respostaUsuario.body()?.status != true) {
                                error(respostaUsuario.body()?.message ?: "Não foi possível criar a conta.")
                            }

                            val login = RetrofitFactory.getAuthService()
                                .login(com.example.ecowayer.model.LoginRequest(dados.email, dados.senha))
                                .awaitResponse()
                            val usuario = login.body()?.response?.usuario
                            if (!login.isSuccessful || login.body()?.status != true ||
                                usuario == null || !RetrofitFactory.salvarSessao(usuario)
                            ) {
                                error("Conta criada. Faça login para entrar no aplicativo.")
                            }

                            navController.navigate(Routes.BoasVindas.route) {
                                popUpTo(Routes.Splash.route) { inclusive = true }
                            }
                        } catch (erro: Exception) {
                            cadastroErro = erro.message ?: "Não foi possível concluir o cadastro."
                        } finally {
                            cadastroCarregando = false
                        }
                    }
                }
            )
        }

        composable(Routes.BoasVindas.route) {
            BoasVindasScreen(
                onFinalizar = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.BoasVindas.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onBuscarEcoPontos = { navController.navigate(Routes.EcoPontos.criar()) },
                onVerExtrato = { navController.navigate(Routes.ExtratoPontos.route) },
                onTrocarPontos = { navController.navigate(Routes.TrocarPontos.route) },
                onAbrirEco = { navController.navigate(Routes.AssistenteEco.route) },
                onIrParaEcoPonto = { ponto ->
                    navController.navigate(Routes.EcoPontos.criar(ponto.id))
                }
            )
        }

        composable(Routes.AssistenteEco.route) {
            AssistenteEcoScreen(onVoltar = { navController.popBackStack() })
        }

        composable(
            route = Routes.EcoPontos.route,
            arguments = listOf(navArgument("pontoId") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { entrada ->
            EcoPontosScreen(
                pontoInicialId = entrada.arguments?.getInt("pontoId") ?: 0,
                onVoltar = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        // Uma única rota da Loja, com todos os callbacks juntos
        composable(Routes.Loja.route) {
            LojaScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onCriarAnuncio = { navController.navigate(Routes.CriarAnuncio.route) },
                onAbrirAnuncio = { anuncio ->
                    navController.navigate(Routes.DetalhesAnuncio.criar(anuncio.id))
                }
            )
        }

        composable(Routes.CriarAnuncio.route) {
            CriarAnuncioScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                publicando = publicando,
                erro = erroPublicacao,
                onPublicar = { anuncio ->
                    scope.launch {
                        publicando = true
                        erroPublicacao = null
                        try {
                            val normalizar = { valor: String -> java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD).replace("\\p{Mn}+".toRegex(), "").lowercase() }
                            val repositorio = EcoWayerRepository(context)
                            val opcoes = repositorio.carregarOpcoes()
                            val categoria = opcoes.categorias.firstOrNull { normalizar(it.nome) == normalizar(anuncio.categoria) }?.id
                                ?: opcoes.categorias.firstOrNull()?.id ?: error("Nenhuma categoria cadastrada.")
                            val modalidadeNome = when (anuncio.modalidade) {
                                com.example.ecowayer.ui.screens.criarAnuncio.Modalidade.Doacao -> "doacao"
                                com.example.ecowayer.ui.screens.criarAnuncio.Modalidade.Troca -> "troca"
                                com.example.ecowayer.ui.screens.criarAnuncio.Modalidade.Venda -> "venda"
                            }
                            val modalidade = opcoes.modalidades.firstOrNull { normalizar(it.nome) == modalidadeNome }?.id
                                ?: opcoes.modalidades.firstOrNull()?.id ?: error("Nenhuma modalidade cadastrada.")
                            val condicao = opcoes.condicoes.firstOrNull { normalizar(it.nome) == normalizar(anuncio.condicao) }?.id
                                ?: opcoes.condicoes.firstOrNull()?.id ?: error("Nenhuma condição cadastrada.")
                            repositorio.publicarAnuncio(
                                nome = anuncio.titulo,
                                descricao = anuncio.descricao,
                                categoria = categoria,
                                modalidade = modalidade,
                                condicao = condicao,
                                preco = anuncio.valor.replace(',', '.'),
                                quantidade = anuncio.quantidade,
                                unidade = anuncio.unidade,
                                imagens = anuncio.imagens
                            )
                            navController.popBackStack()
                        } catch (erro: Exception) {
                            erroPublicacao = erro.message ?: "Não foi possível publicar o anúncio."
                        } finally {
                            publicando = false
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.DetalhesAnuncio.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            DetalhesAnuncioScreen(
                anuncioId = entrada.arguments?.getInt("id") ?: 0,
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onDenunciar = { id -> navController.navigate(Routes.DenunciarAnuncio.criar(id)) },
                onEditar = { id -> navController.navigate(Routes.EditarAnuncio.criar(id)) },
                onAbrirPedidos = { navController.navigate(Routes.MeusPedidos.route) },
                onAbrirConversa = { id -> navController.navigate(Routes.Conversa.criar(id)) }
            )
        }



        composable(
            route = Routes.EditarAnuncio.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            EditarAnuncioScreen(
                anuncioId = entrada.arguments?.getInt("id") ?: 0,
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(Routes.RegistrarEntrega.route) {
            RegistrarEntregaScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { /* já está nesta tela */ }
            )
        }

        composable(Routes.MeusPedidos.route) {
            MeusPedidosScreen(
                onVoltar = { navController.popBackStack() },
                onEscanearQr = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(Routes.TrocarPontos.route) {
            TrocarPontosScreen(
                onVoltar = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(Routes.Carteira.route) {
            CarteiraScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onVerExtrato = { navController.navigate(Routes.ExtratoPontos.route) }
            )
        }

        composable(Routes.ExtratoPontos.route) {
            ExtratoPontosScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(Routes.Perfil.route) {
            PerfilScreen(
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onInsignias = { navController.navigate(Routes.Insignias.route) },
                onNotificacoes = { navController.navigate(Routes.Notificacoes.route) },
                onTrocasPontos = { navController.navigate(Routes.Carteira.route) },
                onMeusAnuncios = { navController.navigate(Routes.MeusAnuncios.route) },
                onMeusPedidos = { navController.navigate(Routes.MeusPedidos.route) },
                onConfiguracoes = { navController.navigate(Routes.Configuracoes.route) },
                onChat = { navController.navigate(Routes.Chat.route) },
                onSair = {
                    RetrofitFactory.limparSessao()
                    navController.navigate(Routes.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Configuracoes.route) {
            ConfiguracoesScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onDadosConta = { navController.navigate(Routes.DadosConta.route) }
            )
        }

        composable(Routes.DadosConta.route) {
            DadosContaScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onEncerrarSessao = {
                    RetrofitFactory.limparSessao()
                    listOf(Routes.Home, Routes.Loja, Routes.Carteira, Routes.Perfil).forEach {
                        navController.clearBackStack(it.route)
                    }
                    navController.navigate(Routes.Splash.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Insignias.route) {
            InsigniasScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(Routes.Notificacoes.route) {
            NotificacoesScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        // Dentro do NavHost (antes estava fora, depois do fechamento do NavHost)
        composable(Routes.MeusAnuncios.route) {
            MeusAnunciosScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onCriarAnuncio = { navController.navigate(Routes.CriarAnuncio.route) },
                onAbrirAnuncio = { anuncio ->
                    navController.navigate(Routes.DetalhesAnuncio.criar(anuncio.id))
                }
            )
        }

        composable(Routes.Chat.route) {
            MensagensScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() },
                onAbrirConversa = { conversa ->
                    navController.navigate(Routes.Conversa.criar(conversa.id))
                }
            )
        }

        composable(
            route = Routes.Conversa.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            ConversaScreen(
                conversaId = entrada.arguments?.getInt("id") ?: 0,
                onBack = { navController.popBackStack() },
                onDenunciarMensagem = { id ->
                    navController.navigate(Routes.DenunciarMensagem.criar(id))
                }
            )
        }

        composable(
            route = Routes.DenunciarMensagem.route,
            arguments = listOf(navArgument("mensagemId") { type = NavType.IntType })
        ) {
            DenunciarScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }

        composable(
            route = Routes.DenunciarAnuncio.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) {
            DenunciarScreen(
                onBack = { navController.popBackStack() },
                onNavegar = { destino -> navController.navegarPelaBarra(destino) },
                onQrCode = { navController.abrirRegistrarEntrega() }
            )
        }
    }
}
