package br.com.paranabanco.dataprev.validation;

/**
 * Resultado de uma validação de arquivo CNAB
 */
public record ValidationResult(
    boolean valido,
    String mensagem,
    String detalhes
) {
    
    /**
     * Cria um resultado de sucesso
     */
    public static ValidationResult sucesso(String mensagem) {
        return new ValidationResult(true, mensagem, null);
    }
    
    /**
     * Cria um resultado de sucesso com detalhes
     */
    public static ValidationResult sucesso(String mensagem, String detalhes) {
        return new ValidationResult(true, mensagem, detalhes);
    }
    
    /**
     * Cria um resultado de erro
     */
    public static ValidationResult erro(String mensagem) {
        return new ValidationResult(false, mensagem, null);
    }
    
    /**
     * Cria um resultado de erro com detalhes
     */
    public static ValidationResult erro(String mensagem, String detalhes) {
        return new ValidationResult(false, mensagem, detalhes);
    }
    
    /**
     * Verifica se a validação foi bem-sucedida
     */
    public boolean isValido() {
        return valido;
    }
    
    /**
     * Verifica se a validação falhou
     */
    public boolean isInvalido() {
        return !valido;
    }
    
    /**
     * Retorna a mensagem formatada
     */
    public String getMensagemFormatada() {
        if (detalhes != null && !detalhes.trim().isEmpty()) {
            return String.format("%s - Detalhes: %s", mensagem, detalhes);
        }
        return mensagem;
    }
}

