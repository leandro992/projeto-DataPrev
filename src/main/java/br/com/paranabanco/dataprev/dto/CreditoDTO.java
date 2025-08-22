package br.com.paranabanco.dataprev.dto;

import br.com.paranabanco.dataprev.enumeration.TipoCredito;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CreditoDTO(
                         Long id,
                         Long beneficioId,
                         LocalDate fimPeriodo,
                         LocalDate inicioPeriodo,
                         String naturezaCredito,
                         TipoCredito tipoCredito,
                         LocalDate dataMovimentoCredito,
                         Long orgaoPagadorId,
                         BigDecimal valorLiquidoCredito,
                         BigDecimal valorBrutoCredito) {

}
