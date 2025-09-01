#!/bin/bash

echo "🚀 Testando Fluxo CONNECT - Simulação de Ambiente de Testes"
echo "=========================================================="

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker Desktop primeiro."
    exit 1
fi

# Verificar se o SQL Server está rodando
if ! docker ps | grep -q "dataprev-sqlserver"; then
    echo "🔄 Iniciando SQL Server..."
    docker compose up -d sqlserver
    echo "⏳ Aguardando SQL Server inicializar..."
    sleep 30
fi

# Iniciar ambiente CONNECT
echo "🔄 Iniciando ambiente CONNECT..."
docker compose -f docker-compose.connect.yml up -d

echo "⏳ Aguardando serviços inicializarem..."
sleep 10

# Verificar status dos containers
echo "📊 Status dos Containers:"
docker compose -f docker-compose.connect.yml ps

echo ""
echo "🔍 Verificando pastas:"
echo "📁 Outbox (remessas):"
ls -la connect-volume/outbox/ 2>/dev/null || echo "   (vazia)"

echo "📁 Retorno (retornos):"
ls -la connect-volume/retorno/ 2>/dev/null || echo "   (vazia)"

echo "📁 Inbox (arquivamento):"
ls -la connect-volume/inbox/ 2>/dev/null || echo "   (vazia)"

echo ""
echo "✅ Ambiente CONNECT configurado!"
echo ""
echo "📋 Próximos passos:"
echo "1. Execute sua aplicação: ./gradlew bootRun --args='--spring.profiles.active=dev'"
echo "2. Gere uma remessa (o arquivo aparecerá em connect-volume/outbox/)"
echo "3. O container dataprev-mock criará automaticamente:"
echo "   - RECIBO_<nome>.d (recibo de recebimento)"
echo "   - RET_<nome>.d (arquivo de retorno)"
echo "4. Os arquivos de retorno estarão em connect-volume/retorno/"
echo ""
echo "🔧 Para conectar via SFTP:"
echo "   Host: 127.0.0.1"
echo "   Porta: 2222"
echo "   Usuário: connect"
echo "   Senha: connect"
echo ""
echo "🛑 Para parar: docker compose -f docker-compose.connect.yml down"
