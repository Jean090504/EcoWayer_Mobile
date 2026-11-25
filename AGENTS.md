# EcoWayer Mobile — contexto para desenvolvimento

## Objetivo do aplicativo

O projeto é um aplicativo Android chamado **Ecowayer**, voltado a reciclagem e
economia circular. Pelo código disponível, a proposta inclui localizar pontos
de coleta, registrar entregas por QR Code, acompanhar pontos/carteira,
publicar e consultar anúncios de materiais, conversar com outros usuários e
acompanhar perfil, notificações e insígnias.

Esse objetivo foi inferido exclusivamente pelas telas, rotas e modelos
presentes no repositório. Não há documentação funcional completa nem código do
backend neste projeto para confirmar todos os requisitos de negócio.

## Arquitetura identificada

- Aplicativo Android nativo, com um único módulo Gradle: `:app`.
- Activity única: `MainActivity`.
- Interface construída predominantemente com Jetpack Compose e Material 3.
- Navegação centralizada em `ui/navigation/AppNavGraph.kt`, usando
  `androidx.navigation:navigation-compose` e a classe selada `Routes`.
- Organização aproximada por camadas/papéis:
  - `ui/screens`: telas e estado local de cada fluxo;
  - `ui/components`: componentes Compose reutilizáveis;
  - `ui/theme`: tema, cores e tipografia;
  - `service`: interfaces Retrofit e repositório de rotas Mapbox;
  - `model`: modelos de resposta da API de pontos de coleta;
  - `util`: transformações auxiliares de entrada.
- Há um `EcoPontosViewModel` para carregar pontos de coleta, mas não existe uma
  camada de dados/repositório completa, injeção de dependência ou uma separação
  formal de domínio. Muitas telas mantêm dados diretamente em `remember`.
- O grafo ativo e único é `ui/navigation/AppNavGraph.kt`, usado pela
  `MainActivity`.

## Tecnologias e configuração

- Kotlin `2.0.21`.
- Android Gradle Plugin `9.0.1`.
- Gradle Wrapper `9.6.0`.
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24`.
- Java/Kotlin configurado para JVM 11.
- Compose habilitado pelo plugin Kotlin Compose; BOM Compose `2024.09.00`.
- AndroidX Core KTX, Activity Compose, Lifecycle, ViewModel Compose/KTX e
  Material 3.
- Mapbox Maps Android NDK `11.29.0` e Maps Compose `11.29.0`.
- CameraX `1.6.2`, ML Kit Barcode Scanning `17.3.0` e ZXing `3.5.3`.
- Retrofit `2.11.0`, Gson Converter `2.11.0` e OkHttp Logging Interceptor
  `4.12.0`.
- Google Play Services Location `21.3.0`.
- Testes com JUnit 4, AndroidX Test, Espresso e Compose UI Test.

As versões estão principalmente em `gradle/libs.versions.toml`, mas algumas
dependências são declaradas diretamente em `app/build.gradle.kts`.

## Organização das pastas

```text
.
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/ecowayer/
│       │   │   ├── MainActivity.kt
│       │   │   ├── model/
│       │   │   ├── service/
│       │   │   ├── ui/components/
│       │   │   ├── ui/navigation/
│       │   │   ├── ui/screens/
│       │   │   ├── ui/theme/
│       │   │   └── util/
│       │   └── res/
│       │       ├── drawable/       # logo, fundo e marcadores
│       │       ├── mipmap*/        # ícones do aplicativo
│       │       ├── values/         # strings, tema e cores
│       │       └── xml/            # regras de backup
│       ├── test/                   # testes unitários locais
│       └── androidTest/            # testes instrumentados
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

Os nomes de alguns arquivos não seguem exatamente o nome das classes, por
exemplo `TelaDeEntrada/TelaDeEntrada.kt`, `CarteiraScrenn.kt` e arquivos com
capitalização inconsistente. O pacote Kotlin é o que determina a resolução das
classes; portanto, não renomear arquivos/pastas sem verificar imports e o
histórico do projeto.

## Telas e navegação

O destino inicial é `splash`. O fluxo principal implementado no grafo ativo é:

`Splash/Login → Home` ou `Cadastro → Verificação de telefone → Verificação de
email → Endereço → Boas-vindas → Home`.

A partir da Home, a barra inferior leva a Início, Loja, Carteira e Perfil; o
botão central abre Registro de entrega. Também estão registradas as rotas:

- `EcoPontos`: mapa, busca, filtros, localização do usuário e rota;
- `Loja`, `CriarAnuncio`, `DetalhesAnuncio` e `DenunciarAnuncio`;
- `RegistrarEntrega`;
- `Carteira`;
- `Perfil`, `Insignias`, `Notificacoes` e `MeusAnuncios`;
- `Chat`/`Mensagens` e `Conversa`.

O login chama `POST /user/usuario/login` através do Retrofit e só navega para
Home após resposta de sucesso. O backend devolve a sessão em cookie HttpOnly;
o mobile usa um `CookieJar` compartilhado entre as chamadas e persiste o cookie
e os dados básicos do usuário em `SharedPreferences`. Esses arquivos são
excluídos de backup. Cadastro,
verificações e endereço avançam entre telas sem chamadas de backend
confirmadas. Recuperação de senha é apenas um TODO.

Telas/componentes localizados incluem `SplashScreen`, `LoginPanel`, cadastro,
verificações, endereço, boas-vindas, Home, EcoPontos, loja, criação/detalhes de
anúncio, denúncia, registro de entrega, carteira, perfil, insígnias,
notificações, meus anúncios, mensagens e conversa.

## Integrações existentes

### API de pontos de coleta

`RetrofitFactory` usa a URL base:

```text
http://10.0.2.2:3000/meraki/ecowayer/
```

`PontoColetaService` implementa `GET user/ponto_coleta`. A resposta esperada é
`ApiResponse<PontoColetaLista>`, com pontos, endereços e materiais aceitos.
`EcoPontosViewModel` faz a chamada ao iniciar, expõe `StateFlow` para dados/erro
e permite tentar novamente. O endereço `10.0.2.2` é próprio do emulador
Android para alcançar o host local; não é uma URL de produção.

### Mapbox

O mapa de EcoPontos usa Mapbox Compose. O código consulta a API Directions em
`https://api.mapbox.com/`, usando `MapboxOptions.accessToken`, e desenha a rota
recebida como GeoJSON. O repositório não contém configuração visível do token;
sem um token configurado, o traçado de rotas falha. A tela também abre rotas no
Google Maps ou em uma URL web de fallback.

### Câmera e QR Code

As dependências CameraX e ML Kit estão presentes e a permissão de câmera está
declarada, mas o leitor real ainda está indicado por comentários/placeholder em
`Registrarentregascreen.kt`. O QR gerado em `QrCodeDialog.kt` é desenhado com
ZXing e usa dados temporários.

### Backend do TCC

Não há módulo backend neste repositório. Além da listagem de pontos de coleta
e do login, as integrações de cadastro, verificações, anúncios, entregas,
carteira, notificações, chat, vouchers, denúncias, perfil e logout aparecem como TODO,
comentários ou dados mockados. Não foi possível confirmar os contratos desses
endpoints a partir deste projeto.

## Banco de dados e armazenamento local

Não foram encontradas dependências ou implementações de Room, SQLite ou
DataStore. A sessão HTTP e os dados básicos do usuário são persistidos em
`SharedPreferences`; os demais estados das telas são, em geral, locais e
mantidos com `remember`.

## Permissões e configurações do Manifest

`AndroidManifest.xml` declara Internet, localização fina e aproximada e câmera;
a câmera é uma feature opcional. A Activity principal é exportada e possui o
intent-filter de launcher. Tráfego HTTP é permitido apenas no manifesto de
debug; builds de release exigem uma URL HTTPS definida por `API_BASE_URL`.
As preferências de sessão são excluídas dos backups Android.

## Como executar no Android Studio

1. Abra a pasta raiz do projeto no Android Studio e aguarde a sincronização do
   Gradle.
2. Use um SDK Android compatível com API 36 e um JDK compatível com a
   configuração JVM 11/AGP do projeto.
3. Se for testar EcoPontos contra backend local no emulador, mantenha o
   backend acessível na porta `3000`; o app usa `10.0.2.2`.
4. Configure o token Mapbox conforme a política de segredos do ambiente antes
   de testar mapa/rotas. O repositório não documenta um mecanismo de injeção do
   token.
5. Selecione a configuração `app`, um emulador/dispositivo com API 24 ou
   superior e execute pelo botão Run.

Pela linha de comando, a verificação realizada neste diagnóstico foi:

```bash
bash ./gradlew :app:compileDebugKotlin
```

Ela terminou com sucesso no estado atual.

## Como testar

- Teste unitário local: `bash ./gradlew test`.
- Teste instrumentado: `bash ./gradlew connectedAndroidTest`, com dispositivo ou
  emulador conectado.
- Testes Compose/UI devem ser adicionados conforme os fluxos forem ligados ao
  backend. Atualmente existem apenas os testes de exemplo gerados pelo
  template (`ExampleUnitTest` e `ExampleInstrumentedTest`).
- Testes manuais importantes incluem login/cadastro, navegação da barra
  inferior, permissões de localização/câmera, carregamento de EcoPontos,
  filtros, rota Mapbox, fallback do Google Maps e comportamento sem rede.

## Padrões e regras para alterações futuras

- Preservar Kotlin + Jetpack Compose e usar o tema `EcowayerTheme`.
- Manter a navegação no grafo ativo `ui/navigation/AppNavGraph.kt` e declarar
  novas rotas em `Routes`; não adicionar rotas somente ao grafo legado de
  `ui/components`.
- Separar UI, estado e acesso a dados: novas chamadas de API devem usar
  serviços/repositórios e ViewModels, com estados explícitos de carregamento,
  sucesso e erro.
- Não colocar tokens, senhas, URLs privadas ou credenciais diretamente no
  código-fonte. Definir uma estratégia segura e documentada para configurações
  por ambiente.
- Não considerar dados mockados como implementação de backend. Ao substituir
  mocks, definir modelos, contratos, tratamento de erros, autenticação e
  testes.
- Solicitar permissões em tempo de execução e tratar recusa, ausência de
  hardware e ausência de aplicativos externos.
- Manter compatibilidade com `minSdk 24` e revisar impacto de mudanças em
  `compileSdk`, AGP, Kotlin e dependências.
- Evitar renomear apenas para corrigir capitalização sem atualizar pacotes,
  imports e referências; preferir nomes consistentes em novas classes.
- Não remover ou alterar dados/fluxos existentes sem verificar as telas
  consumidoras e sem testes correspondentes.
- Antes de concluir alterações, executar ao menos compilação Kotlin e testes
  relevantes, registrando limitações ambientais quando não for possível.

## Diagnóstico atual

### Pontos positivos

- O projeto compila a tarefa `:app:compileDebugKotlin` com sucesso.
- Há uma base visual significativa em Compose, tema próprio, componentes
  reutilizáveis e navegação centralizada.
- EcoPontos é o fluxo mais integrado: possui API, ViewModel, filtros, mapa,
  localização, marcadores e consulta de rota.
- Há testes de unidade e instrumentação mínimos, além de previews Compose.

### Problemas e limitações observados

- A autenticação do login está conectada, mas a maior parte das operações de
  negócio ainda não está conectada à API; várias telas usam mocks e estados
  locais.
- A sessão é persistida localmente em preferências privadas, com exclusão das
  regras de backup Android.
- O leitor CameraX/ML Kit está preparado por dependências, mas não implementado
  no fluxo de registro de entrega.
- O token Mapbox não possui configuração documentada no repositório.
- O manifesto de debug permite cleartext apenas para o backend local; release
  exige uma URL HTTPS.
- Ainda há inconsistências entre caminhos de arquivos, nomes e pacotes.
- O ViewModel de EcoPontos está em `ui/screens/ecopontos`, mas declara o pacote
  `com.example.ecowayer.ui.ecopontos`; compila, porém reduz a clareza da
  organização.
- Há avisos de depreciação em `CarteiraScrenn.kt` e `Perfilscreen.kt`.
- Não há testes específicos dos fluxos, ViewModels, serialização, navegação,
  permissões ou integração com a API.

### Sugestões, sem implementação nesta etapa

1. Definir e documentar os contratos do backend, autenticação e ambientes.
2. Criar uma camada de dados consistente, ViewModels por fluxo e tratamento
   padronizado de estados/erros.
3. Implementar sessão segura e persistência necessária, preferencialmente com
   armazenamento apropriado para tokens.
4. Concluir CameraX/ML Kit e validar QR Code no backend.
5. Configurar Mapbox por ambiente/segredo e remover cleartext de produção.
6. Consolidar o grafo de navegação, padronizar nomes/pacotes e remover código
   legado após confirmar que não é usado.
7. Substituir mocks gradualmente por dados reais e adicionar testes unitários,
   instrumentados e de UI para os fluxos críticos.
8. Atualizar APIs/deprecações e centralizar versões de dependências no catálogo
   Gradle.

