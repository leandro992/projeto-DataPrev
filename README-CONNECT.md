# 🚀 Ambiente CONNECT - Simulação de Testes

Este projeto implementa uma simulação completa do ambiente CONNECT para testes locais, **sem modificar o código existente**.

## 🎯 O que é Simulado

- **Servidor SFTP CONNECT** (porta 2222)
- **Processamento automático** de remessas → retornos
- **Estrutura de pastas** idêntica ao ambiente real
- **Fluxo completo** de arquivos

## 📁 Estrutura de Pastas

```
connect-volume/
├── outbox/     # 📤 Remessas do banco → CONNECT
├── retorno/    # 📥 Retornos do CONNECT → banco  
└── inbox/      # 📦 Arquivamento (opcional)
```

## 🚀 Como Usar

### 1. Pré-requisitos
- Docker Desktop rodando
- Projeto compilando sem erros

### 2. Iniciar Ambiente CONNECT
```bash
# Inicia apenas o ambiente CONNECT (mantém SQL Server existente)
docker compose -f docker-compose.connect.yml up -d

# Ou use o script automatizado
./test-connect-flow.sh
```

### 3. Executar Aplicação
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4. Fluxo Automático
1. ✅ Sua aplicação gera remessa → `connect-volume/outbox/`
2. 🔄 Container `dataprev-mock` processa automaticamente:
   - Cria `RECIBO_<nome>.d` (recibo)
   - Cria `RET_<nome>.d` (retorno)
   - Move arquivo original para `connect-volume/inbox/`
3. 📥 Arquivos de retorno ficam em `connect-volume/retorno/`

## 🔧 Configuração

### application-dev.yml (já configurado ✅)
```yaml
app:
  cnab:
    outputDir: ./connect-volume/outbox        # remessas
    outputName: FSUBCON10.D0000001.d         # nome do arquivo
    retornoDir: ./connect-volume/retorno      # retornos
    charset: ISO-8859-1
```

### Portas
- **SQL Server**: 1433 (existente)
- **CONNECT SFTP**: 2222 (novo)
- **Aplicação**: 8080

## 📡 Teste SFTP (Opcional)

Conectar ao "CONNECT" simulado:
```bash
sftp -P 2222 connect@127.0.0.1
# Senha: connect

# Comandos úteis:
ls                    # listar arquivos
cd outbox            # ir para pasta de remessas
cd retorno           # ir para pasta de retornos
get arquivo.txt      # baixar arquivo
put arquivo.txt      # enviar arquivo
```

## 🧪 Testando o Fluxo

### 1. Verificar Status
```bash
docker compose -f docker-compose.connect.yml ps
```

### 2. Monitorar Logs
```bash
# Logs do gerador de retorno
docker compose -f docker-compose.connect.yml logs -f dataprev-mock

# Logs do SFTP
docker compose -f docker-compose.connect.yml logs -f connect-sftp
```

### 3. Verificar Arquivos
```bash
# Remessas geradas
ls -la connect-volume/outbox/

# Retornos processados
ls -la connect-volume/retorno/

# Arquivamento
ls -la connect-volume/inbox/
```

## 🛑 Encerrando

```bash
# Parar apenas ambiente CONNECT
docker compose -f docker-compose.connect.yml down

# Parar tudo (incluindo SQL Server)
docker compose down
```

## 🔍 Troubleshooting

### Container não inicia
```bash
# Verificar logs
docker compose -f docker-compose.connect.yml logs

# Verificar portas
netstat -an | grep 2222
```

### Arquivos não são processados
```bash
# Verificar permissões das pastas
ls -la connect-volume/

# Verificar logs do dataprev-mock
docker compose -f docker-compose.connect.yml logs dataprev-mock
```

### SFTP não conecta
```bash
# Verificar se container está rodando
docker ps | grep connect-sftp

# Testar conectividade
telnet 127.0.0.1 2222
```

## 📋 Vantagens desta Implementação

✅ **Zero modificação** no código existente  
✅ **Configuração automática** via docker-compose  
✅ **Processamento em tempo real** de arquivos  
✅ **Compatível** com macOS e Windows  
✅ **Isolado** do ambiente de desenvolvimento  
✅ **Fácil de testar** e debugar  

## 🎉 Resultado

Agora você tem um ambiente CONNECT completo rodando localmente, simulando exatamente o comportamento do ambiente real, sem precisar modificar uma linha do seu código Spring Boot!
