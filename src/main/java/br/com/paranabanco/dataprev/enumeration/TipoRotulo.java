package br.com.paranabanco.dataprev.enumeration;

/**
 * Enum que define os tipos de rótulos de arquivo suportados pelo sistema.
 * Cada tipo tem suas regras específicas de busca e processamento.
 */
public enum TipoRotulo {
    
    /**
     * Concessão - Rótulo FHMLCON16
     * Regra: Busca arquivo com n=6 (D0000006)
     */
    CONCESSAO("FHMLCON16", "Concessão", 6),
    
    /**
     * Maciça - Rótulo FHMLMAC16
     * Regra: Busca o arquivo com maior sequência (mais alto)
     */
    MACICA("FHMLMAC16", "Maciça", null),
    
    /**
     * Especial - Rótulo FHMLCES18
     * Regra: Busca o arquivo com maior sequência (mais alto)
     */
    ESPECIAL("FHMLCES18", "Especial", null);
    
    private final String rotulo;
    private final String descricao;
    private final Integer valorN;
    
    TipoRotulo(String rotulo, String descricao, Integer valorN) {
        this.rotulo = rotulo;
        this.descricao = descricao;
        this.valorN = valorN;
    }
    
    public String getRotulo() {
        return rotulo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public Integer getValorN() {
        return valorN;
    }
    
    /**
     * Verifica se o tipo de rótulo requer um valor N específico
     */
    public boolean requerValorN() {
        return valorN != null;
    }
    
    /**
     * Busca o tipo de rótulo pelo nome do rótulo
     */
    public static TipoRotulo fromRotulo(String rotulo) {
        for (TipoRotulo tipo : values()) {
            if (tipo.rotulo.equalsIgnoreCase(rotulo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Rótulo não suportado: " + rotulo);
    }
    
    /**
     * Verifica se um rótulo é suportado pelo sistema
     */
    public static boolean isRotuloSuportado(String rotulo) {
        for (TipoRotulo tipo : values()) {
            if (tipo.rotulo.equalsIgnoreCase(rotulo)) {
                return true;
            }
        }
        return false;
    }
}
