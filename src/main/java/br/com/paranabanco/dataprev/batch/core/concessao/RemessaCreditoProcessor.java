package br.com.paranabanco.dataprev.batch.core.concessao;

import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.RemessaCreditoDTO;
import org.springframework.batch.item.ItemProcessor;

public class RemessaCreditoProcessor implements ItemProcessor<Credito, RemessaCreditoDTO> {

    @Override
    public RemessaCreditoDTO process(Credito item) throws Exception {
        return RemessaCreditoDTO.builder()
                .credito(item)
                .build();
    }
}