package br.com.paranabanco.dataprev.cnab.layout;

import br.com.paranabanco.dataprev.domain.Beneficio;
import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.RemessaCreditoDTO;
import br.com.paranabanco.dataprev.job.concessao.RemessaConcessaoLayoutBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CNAB layout implementation that delegates to {@link RemessaConcessaoLayoutBuilder}.
 * This allows the new {@code CnabWriter} to reuse the existing layout logic without
 * modifying the legacy builder.
 */
public class RemessaConcessaoCnabLayout implements CnabLayout<RemessaCreditoDTO> {

    @Override
    public List<String> build(List<RemessaCreditoDTO> remessas) {
        List<String> lines = new ArrayList<>();

        // --- LOTE 20 ---
        lines.add(RemessaConcessaoLayoutBuilder.buildHeaderLote20e21("20", "01"));
        BigDecimal totalValorLote20 = BigDecimal.ZERO;
        for (RemessaCreditoDTO dto : remessas) {
            Credito credito = dto.getCredito();
            lines.add(RemessaConcessaoLayoutBuilder.buildDetalheLote20(credito));
            totalValorLote20 = totalValorLote20.add(
                    Optional.ofNullable(credito.getValorLiquidoCredito()).orElse(BigDecimal.ZERO)
            );
        }
        lines.add(RemessaConcessaoLayoutBuilder.buildTrailerLote20(remessas.size(), totalValorLote20));

        // --- LOTE 21 ---
        Map<String, Beneficio> uniqueBeneficios = new LinkedHashMap<>();
        for (RemessaCreditoDTO dto : remessas) {
            Credito credito = dto.getCredito();
            uniqueBeneficios.put(credito.getBeneficio().getNumeroBeneficio(), credito.getBeneficio());
        }
        lines.add(RemessaConcessaoLayoutBuilder.buildHeaderLote20e21("21", "01"));
        for (Beneficio beneficio : uniqueBeneficios.values()) {
            lines.add(RemessaConcessaoLayoutBuilder.buildDetalheLote21(beneficio));
        }
        lines.add(RemessaConcessaoLayoutBuilder.buildTrailerLote21(uniqueBeneficios.size()));

        return lines;
    }
}
