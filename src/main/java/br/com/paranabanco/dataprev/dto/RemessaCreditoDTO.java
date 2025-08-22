package br.com.paranabanco.dataprev.dto;

import br.com.paranabanco.dataprev.domain.Credito;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemessaCreditoDTO {
    private Credito credito;
}