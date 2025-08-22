package br.com.paranabanco.dataprev.job.concessao;

import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.RemessaCreditoDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class RemessaCreditoProcessor implements ItemProcessor<Credito, RemessaCreditoDTO> {

    @Override
    public RemessaCreditoDTO process(Credito item) throws Exception {
        return RemessaCreditoDTO.builder()
                .credito(item)
                .build();
    }
}