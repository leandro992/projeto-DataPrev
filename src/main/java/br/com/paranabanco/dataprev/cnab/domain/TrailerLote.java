package br.com.paranabanco.dataprev.cnab.domain;

import java.util.Map;

/**
 * Trailer de um lote CNAB.
 */
public record TrailerLote(
        String lote,
        String quantidadeRegistros
) {
    public Map<String, String> toMap() {
        return Map.of(
                "lote", lote,
                "quantidadeRegistros", quantidadeRegistros
        );
    }

    public static TrailerLote fromMap(Map<String, String> map) {
        return new TrailerLote(
                map.get("lote"),
                map.get("quantidadeRegistros")
        );
    }
}
