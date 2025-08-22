package br.com.paranabanco.dataprev.cnab.domain;

import java.util.Map;

/**
 * Representa o cabeçalho do arquivo CNAB.
 */
public record HeaderArquivo(
        String banco,
        String dataGeracao
) {
    /** Converte a entidade para um mapa. */
    public Map<String, String> toMap() {
        return Map.of(
                "banco", banco,
                "dataGeracao", dataGeracao
        );
    }

    /** Constrói a entidade a partir de um mapa. */
    public static HeaderArquivo fromMap(Map<String, String> map) {
        return new HeaderArquivo(
                map.get("banco"),
                map.get("dataGeracao")
        );
    }
}
