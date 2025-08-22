package br.com.paranabanco.dataprev.cnab.domain;

import java.util.Map;

/**
 * Trailer do arquivo CNAB.
 */
public record TrailerArquivo(
        String quantidadeLotes,
        String quantidadeRegistros
) {
    public Map<String, String> toMap() {
        return Map.of(
                "quantidadeLotes", quantidadeLotes,
                "quantidadeRegistros", quantidadeRegistros
        );
    }

    public static TrailerArquivo fromMap(Map<String, String> map) {
        return new TrailerArquivo(
                map.get("quantidadeLotes"),
                map.get("quantidadeRegistros")
        );
    }
}
