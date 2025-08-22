package br.com.paranabanco.dataprev.cnab.domain;

import java.util.Map;

/**
 * Segmento B de um detalhe do arquivo CNAB.
 */
public record SegmentoB(
        String cpf,
        String endereco
) {
    public Map<String, String> toMap() {
        return Map.of(
                "cpf", cpf,
                "endereco", endereco
        );
    }

    public static SegmentoB fromMap(Map<String, String> map) {
        return new SegmentoB(
                map.get("cpf"),
                map.get("endereco")
        );
    }
}
