#!/bin/bash

# Script para processar arquivos de remessa e gerar retornos
# Simula o comportamento do CONNECT

echo "🚀 Iniciando processador de arquivos CONNECT..."

# Instalar dependências
apt-get update -qq && apt-get install -y -qq gawk

# Criar diretórios
mkdir -p outbox retorno inbox

echo "✅ Diretórios criados. Monitorando pasta outbox..."

# Loop principal
while true; do
    for f in outbox/*.d; do
        if [ -e "$f" ]; then
            base=$(basename "$f")
            echo "📥 Processando arquivo: $base"
            
            # Criar recibo
            cp "$f" "retorno/RECIBO_$base"
            echo "📋 Recibo criado: RECIBO_$base"
            
            # Criar arquivo de retorno
            awk '1; END{print "RETORNO OK"}' "$f" > "retorno/RET_$base"
            echo "📤 Retorno criado: RET_$base"
            
            # Mover para arquivamento
            mv "$f" "inbox/$base"
            echo "📦 Arquivo movido para arquivamento: $base"
            
            echo "✅ Processamento concluído para: $base"
        fi
    done
    
    # Aguardar antes da próxima verificação
    sleep 2
done
