package br.com.paranabanco.dataprev.dto;

import br.com.paranabanco.dataprev.enumeration.TipoRotulo;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO para requisição de busca de arquivo no Connect
 */
@Slf4j
public record BuscarArquivoRequest(
    
    @NotBlank(message = "Rótulo é obrigatório")
    String rotulo,
    
    Integer n
) {
    
    /**
     * Valida se o request está correto baseado no tipo de rótulo
     */
    public void validar() {
        if (!TipoRotulo.isRotuloSuportado(rotulo)) {
            log.error("Rótulo não suportado");
            throw new IllegalArgumentException("Rótulo não suportado: " + rotulo);
        }
        
        TipoRotulo tipo = TipoRotulo.fromRotulo(rotulo);
        
        if (tipo.requerValorN() && n == null) {
            throw new IllegalArgumentException("Rótulo " + rotulo + " requer valor N específico");
        }
        
        if (!tipo.requerValorN() && n != null) {
            throw new IllegalArgumentException("Rótulo " + rotulo + " não requer valor N");
        }
        
        if (tipo.requerValorN() && !tipo.getValorN().equals(n)) {
            throw new IllegalArgumentException("Rótulo " + rotulo + " requer valor N = " + tipo.getValorN() + ", mas foi fornecido " + n);
        }
    }
    
    /**
     * Cria um request válido para o tipo de rótulo especificado
     */
    public static BuscarArquivoRequest paraTipo(TipoRotulo tipo) {
        return new BuscarArquivoRequest(tipo.getRotulo(), tipo.getValorN());
    }
}
