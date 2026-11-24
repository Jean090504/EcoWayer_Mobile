# Integração do EcoWayer Mobile com o backend

O aplicativo usa Retrofit contra o Backend Usuário. A URL padrão é a porta
`3000` no host do emulador Android (`10.0.2.2`). Para executar em um aparelho
físico ou em outro endereço da rede, passe a URL completa ao Gradle:

```bash
bash ./gradlew :app:assembleDebug -PAPI_BASE_URL=http://192.168.0.10:3000/meraki/ecowayer/
```

O backend precisa aceitar a conexão na interface da rede, o firewall precisa
liberar a porta e o aparelho deve estar na mesma rede. O build local permite
HTTP para desenvolvimento; produção deve usar HTTPS.

As sessões usam o cookie `ecowayer_token` emitido pelo backend e são mantidas
no dispositivo para que o usuário não seja desconectado ao fechar o aplicativo.
O logout remove o cookie e os dados locais da sessão.

Integrações atualmente ligadas à API:

- login, cadastro, endereço e gêneros;
- pontos de coleta e mapa;
- listagem e detalhe de anúncios;
- publicação de anúncios com fotos;
- meus anúncios, exclusão e atualização de status;
- carteira baseada nos registros de pontos;
- notificações;
- conversas e envio/leitura de mensagens;
- registro de denúncias.

### Fotos de anúncios e do perfil

A edição de anúncios e de perfil envia novas fotos pelo mesmo preset sem assinatura usado no frontend
web. Configure `CLOUDINARY_CLOUD_NAME` e `CLOUDINARY_UPLOAD_PRESET` no
`local.properties` ou como propriedades Gradle. O nome da nuvem deve
corresponder ao configurado no backend. As duas propriedades são parâmetros
públicos do upload. A chave secreta do Cloudinary pertence ao servidor.
