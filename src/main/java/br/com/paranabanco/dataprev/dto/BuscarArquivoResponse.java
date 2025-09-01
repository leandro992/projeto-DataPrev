package br.com.paranabanco.dataprev.dto;

import br.com.paranabanco.dataprev.enumeration.TipoRotulo;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO para resposta de busca de arquivo no Connect
 */
public record BuscarArquivoResponse(
    
    String rotulo,
    
    TipoRotulo tipoRotulo,
    
    String nomeArquivo,
    
    String caminhoLocal,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dataDownload,
    
    String status,
    
    String mensagem,
    
    Long tamanhoBytes,
    
    String hashArquivo
) {
    
    /**
     * Cria uma resposta de sucesso
     */
    public static BuscarArquivoResponse sucesso(String rotulo, String nomeArquivo, String caminhoLocal, 
                                               Long tamanhoBytes, String hashArquivo) {
        return new BuscarArquivoResponse(
            rotulo,
            TipoRotulo.fromRotulo(rotulo),
            nomeArquivo,
            caminhoLocal,
            LocalDateTime.now(),
            "SUCESSO",
            "Arquivo encontrado e baixado com sucesso",
            tamanhoBytes,
            hashArquivo
        );
    }
    
    /**
     * Cria uma resposta de erro
     */
    public static BuscarArquivoResponse erro(String rotulo, String mensagem) {
        return new BuscarArquivoResponse(
            rotulo,
            rotulo != null ? TipoRotulo.fromRotulo(rotulo) : null,
            null,
            null,
            LocalDateTime.now(),
            "ERRO",
            mensagem,
            null,
            null
        );
    }
    
    /**
     * Cria uma resposta de arquivo não encontrado
     */
    public static BuscarArquivoResponse naoEncontrado(String rotulo) {
        return new BuscarArquivoResponse(
            rotulo,
            TipoRotulo.fromRotulo(rotulo),
            null,
            null,
            LocalDateTime.now(),
            "NAO_ENCONTRADO",
            "Nenhum arquivo encontrado para o rótulo " + rotulo,
            null,
            null
        );
    }
}
