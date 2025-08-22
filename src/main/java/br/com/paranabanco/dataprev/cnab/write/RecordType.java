package br.com.paranabanco.dataprev.cnab.write;

import java.util.List;

/**
 * Representa os diferentes tipos de registros no layout CNAB 240.
 */
public enum RecordType {
    HEADER(new Field("tipo", 1), new Field("filler", 239)),
    SEGMENT(new Field("tipo", 1), new Field("valor", 10), new Field("filler", 229)),
    TRAILER(new Field("tipo", 1), new Field("quantidade", 6), new Field("valorTotal", 10), new Field("filler", 223));

    private final List<Field> fields;

    RecordType(Field... fields) {
        this.fields = List.of(fields);
    }

    public List<Field> getFields() {
        return fields;
    }

    /**
     * Definição de campo de um registro.
     * @param name   nome do campo para identificar no mapa de valores
     * @param length tamanho fixo do campo
     */
    public record Field(String name, int length) { }
}

