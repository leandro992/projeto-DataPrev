package br.com.paranabanco.dataprev.cnab;

import java.util.LinkedHashMap;
import java.util.Map;

public class CnabRecord {
    private final String tipo;                 // HeaderArquivo, HeaderLote, SegmentoA, SegmentoB, Trailer...
    private final Map<String, String> campos;  // banco, lote, tipoRegistro, resto...
    private final long lineNumber;

    public CnabRecord(String tipo, Map<String, String> campos, long lineNumber) {
        this.tipo = tipo;
        this.campos = new LinkedHashMap<>(campos);
        this.lineNumber = lineNumber;
    }

    public String getTipo() { return tipo; }
    public Map<String, String> getCampos() { return campos; }
    public long getLineNumber() { return lineNumber; }

    public String get(String key) { return campos.get(key); }
}
