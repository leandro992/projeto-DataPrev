package br.com.paranabanco.dataprev.cnab.domain;

import java.util.Map;

/**
 * Representa o cabeçalho de um lote CNAB.
 */
public record HeaderLote(
        String banco,
        String lote
) {
    public Map<String, String> toMap() {
        return Map.of(
                "banco", banco,
                "lote", lote
        );
    }

    public static HeaderLote fromMap(Map<String, String> map) {
        return new HeaderLote(
                map.get("banco"),
                map.get("lote")
        );
    }
}
