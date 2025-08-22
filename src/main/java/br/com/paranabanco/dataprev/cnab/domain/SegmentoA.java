package br.com.paranabanco.dataprev.cnab.domain;

import java.util.Map;

/**
 * Segmento A de um detalhe do arquivo CNAB.
 */
public record SegmentoA(
        String agencia,
        String conta,
        String valor
) {
    public Map<String, String> toMap() {
        return Map.of(
                "agencia", agencia,
                "conta", conta,
                "valor", valor
        );
    }

    public static SegmentoA fromMap(Map<String, String> map) {
        return new SegmentoA(
                map.get("agencia"),
                map.get("conta"),
                map.get("valor")
        );
    }
}
