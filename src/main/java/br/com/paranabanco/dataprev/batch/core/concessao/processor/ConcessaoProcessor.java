package br.com.paranabanco.dataprev.batch.core.concessao.processor;

import br.com.paranabanco.dataprev.cnab.CnabLineMapper;
import br.com.paranabanco.dataprev.cnab.CnabRecord;
import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.enumeration.TipoCredito;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Processor específico para registros de concessão (FHMLCON16).
 * Converte linhas CNAB em objetos Credito seguindo as regras específicas de concessão.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConcessaoProcessor implements ItemProcessor<String, Credito> {

    private final CnabLineMapper cnabLineMapper;

    @Override
    public Credito process(@NonNull String linha) throws Exception {
        try {
            log.debug("Processando linha de concessão: {}", linha);
            
            // Valida se a linha não está vazia
            if (linha == null || linha.trim().isEmpty()) {
                log.warn("Linha vazia encontrada, ignorando");
                return null;
            }
            
            // Mapeia a linha CNAB para objeto CnabRecord
            CnabRecord cnabRecord = cnabLineMapper.mapLine(linha, 0);
            
            // Valida se é um registro válido para concessão
            if (!isRegistroConcessaoValido(cnabRecord)) {
                log.warn("Registro não é válido para concessão: {}", linha);
                return null;
            }
            
            // Converte CnabRecord para Credito
            Credito credito = converterParaCredito(cnabRecord);
            
            // Aplica regras específicas de concessão
            aplicarRegrasConcessao(credito);
            
            log.debug("Linha processada com sucesso: {}", credito.getBeneficio() != null ? credito.getBeneficio().getNumeroBeneficio() : "N/A");
            return credito;
            
        } catch (Exception e) {
            log.error("Erro ao processar linha de concessão: {}", linha, e);
            throw new RuntimeException("Erro no processamento de concessão: " + e.getMessage(), e);
        }
    }

    /**
     * Valida se o registro é válido para concessão
     */
    private boolean isRegistroConcessaoValido(CnabRecord cnabRecord) {
        // Validações específicas para concessão
        if (cnabRecord == null) {
            return false;
        }
        
        // Verifica se é um registro de detalhe (SegmentoA ou SegmentoB)
        String tipo = cnabRecord.getTipo();
        if (!"SegmentoA".equals(tipo) && !"SegmentoB".equals(tipo)) {
            log.debug("Registro não é de detalhe, ignorando: {}", tipo);
            return false;
        }
        
        // Verifica se tem dados no resto do registro
        String resto = cnabRecord.get("resto");
        if (resto == null || resto.trim().isEmpty()) {
            log.warn("Registro sem dados no campo resto");
            return false;
        }
        
        return true;
    }

    /**
     * Converte CnabRecord para Credito
     */
    private Credito converterParaCredito(CnabRecord cnabRecord) {
        // Cria um crédito básico com dados extraídos do CNAB
        Credito credito = Credito.builder()
                .tipoCredito(TipoCredito.CONCESSAO)
                .naturezaCredito("CONCESSAO")
                .unidadeMonetaria("BRL")
                .build();
        
        // Extrai dados do campo "resto" do CNAB
        String resto = cnabRecord.get("resto");
        if (resto != null && resto.length() > 0) {
            // Aqui seria feita a extração específica dos campos do CNAB
            // Por enquanto, vamos criar um benefício básico
            // TODO: Implementar extração específica dos campos CNAB
            
            log.debug("Processando registro CNAB: tipo={}, resto={}", cnabRecord.getTipo(), resto);
        }
        
        return credito;
    }

    /**
     * Aplica regras específicas de concessão
     */
    private void aplicarRegrasConcessao(Credito credito) {
        // Regra 1: Concessão sempre tem tipo CONCESSAO
        credito.setTipoCredito(TipoCredito.CONCESSAO);
        
        // Regra 2: Validação de valor mínimo para concessão
        if (credito.getValorBrutoCredito() != null && 
            credito.getValorBrutoCredito().compareTo(java.math.BigDecimal.valueOf(1.00)) < 0) {
            log.warn("Valor de concessão muito baixo: {}", credito.getValorBrutoCredito());
        }
        
        // Regra 3: Validação de data de vencimento
        if (credito.getFimValidade() == null) {
            log.warn("Data de vencimento não informada para concessão");
        }
        
        log.debug("Regras de concessão aplicadas");
    }
}
