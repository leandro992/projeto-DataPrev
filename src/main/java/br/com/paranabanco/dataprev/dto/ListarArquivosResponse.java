package br.com.paranabanco.dataprev.dto;

import br.com.paranabanco.dataprev.enumeration.TipoRotulo;

import java.util.List;

/**
 * DTO para resposta de listagem de arquivos disponíveis no Connect
 */
public record ListarArquivosResponse(
    
    String rotulo,
    
    TipoRotulo tipoRotulo,
    
    List<String> arquivosDisponiveis,
    
    Integer totalArquivos,
    
    String status,
    
    String mensagem
) {
    
    /**
     * Cria uma resposta de sucesso com lista de arquivos
     */
    public static ListarArquivosResponse sucesso(String rotulo, List<String> arquivos) {
        return new ListarArquivosResponse(
            rotulo,
            TipoRotulo.fromRotulo(rotulo),
            arquivos,
            arquivos.size(),
            "SUCESSO",
            "Arquivos listados com sucesso"
        );
    }
    
    /**
     * Cria uma resposta de erro
     */
    public static ListarArquivosResponse erro(String rotulo, String mensagem) {
        return new ListarArquivosResponse(
            rotulo,
            rotulo != null ? TipoRotulo.fromRotulo(rotulo) : null,
            List.of(),
            0,
            "ERRO",
            mensagem
        );
    }
    
    /**
     * Cria uma resposta quando nenhum arquivo é encontrado
     */
    public static ListarArquivosResponse vazio(String rotulo) {
        return new ListarArquivosResponse(
            rotulo,
            TipoRotulo.fromRotulo(rotulo),
            List.of(),
            0,
            "VAZIO",
            "Nenhum arquivo encontrado para o rótulo " + rotulo
        );
    }
}
