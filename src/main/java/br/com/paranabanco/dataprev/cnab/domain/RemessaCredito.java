package br.com.paranabanco.dataprev.cnab.domain;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO agregador contendo as informações necessárias para geração
 * dos arquivos de remessa de crédito em layout CNAB 240.
 */
@Data
@Builder
public class RemessaCredito {

    /** Registro de header do arquivo. */
    private Map<String, String> header;

    /** Lista de registros de detalhe (segmentos). */
    @Builder.Default
    private List<Map<String, String>> segmentos = new ArrayList<>();

    /** Lista de trailers do arquivo. */
    @Builder.Default
    private List<Map<String, String>> trailers = new ArrayList<>();
}

