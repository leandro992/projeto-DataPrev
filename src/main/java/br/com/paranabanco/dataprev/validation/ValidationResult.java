package br.com.paranabanco.dataprev.validation;

/**
 * Resultado de uma validação de arquivo CNAB
 */
public record ValidationResult(
    boolean valido,
    String codigo,
    String mensagem,
    String detalhes
) {
    
    /**
     * Cria um resultado de sucesso
     */
    public static ValidationResult sucesso(String codigo, String mensagem) {
        return new ValidationResult(true, codigo, mensagem, null);
    }
    
    /**
     * Cria um resultado de sucesso com detalhes
     */
    public static ValidationResult sucesso(String codigo, String mensagem, String detalhes) {
        return new ValidationResult(true, codigo, mensagem, detalhes);
    }
    
    /**
     * Cria um resultado de erro
     */
    public static ValidationResult erro(String codigo, String mensagem) {
        return new ValidationResult(false, codigo, mensagem, null);
    }
    
    /**
     * Cria um resultado de erro com detalhes
     */
    public static ValidationResult erro(String codigo, String mensagem, String detalhes) {
        return new ValidationResult(false, codigo, mensagem, detalhes);
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
        String base = (codigo != null && !codigo.trim().isEmpty()) ?
            String.format("[%s] %s", codigo, mensagem) : mensagem;
        if (detalhes != null && !detalhes.trim().isEmpty()) {
            return String.format("%s - Detalhes: %s", base, detalhes);
        }
        return base;
    }
}

