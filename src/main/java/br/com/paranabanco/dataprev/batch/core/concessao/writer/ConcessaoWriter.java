package br.com.paranabanco.dataprev.batch.core.concessao.writer;

import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.repository.CreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

/**
 * Writer específico para salvar registros de concessão processados.
 * Salva os créditos de concessão no banco de dados com validações específicas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConcessaoWriter implements ItemWriter<Credito> {

    private final CreditoRepository creditoRepository;

    @Override
    @Transactional
    public void write(@NonNull Chunk<? extends Credito> chunk) throws Exception {
        @SuppressWarnings("unchecked")
        List<Credito> creditos = (List<Credito>) chunk.getItems();
        
        if (creditos.isEmpty()) {
            log.debug("Nenhum crédito para salvar");
            return;
        }
        
        log.info("Salvando {} créditos de concessão", creditos.size());
        
        for (Credito credito : creditos) {
            try {
                // Validações antes de salvar
                if (!isCreditoValido(credito)) {
                    log.warn("Crédito inválido, ignorando");
                    continue;
                }
                
                // Salva o crédito
                log.debug("Salvando crédito de concessão");
                salvarNovoCredito(credito);
                
            } catch (Exception e) {
                log.error("Erro ao salvar crédito de concessão", e);
                throw new RuntimeException("Erro ao salvar crédito de concessão: " + e.getMessage(), e);
            }
        }
        
        log.info("Créditos de concessão salvos com sucesso: {}", creditos.size());
    }

    /**
     * Valida se o crédito está válido para ser salvo
     */
    private boolean isCreditoValido(Credito credito) {
        if (credito == null) {
            log.warn("Crédito é nulo");
            return false;
        }
        
        if (credito.getTipoCredito() == null) {
            log.warn("Tipo de crédito não informado");
            return false;
        }
        
        if (credito.getBeneficio() == null) {
            log.warn("Benefício não informado");
            return false;
        }
        
        return true;
    }



    /**
     * Salva novo crédito
     */
    private void salvarNovoCredito(Credito credito) {
        // Salva no banco
        Credito creditoSalvo = creditoRepository.save(credito);
        log.debug("Novo crédito de concessão salvo com ID: {}", creditoSalvo.getId());
    }
}
