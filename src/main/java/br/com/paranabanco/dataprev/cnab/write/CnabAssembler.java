package br.com.paranabanco.dataprev.cnab.write;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Responsável por montar registros CNAB 240 a partir de um {@link RecordType}
 * e dos valores informados.
 */
public class CnabAssembler {

    /**
     * Monta uma linha CNAB 240 com base no tipo de registro informado.
     *
     * @param type   tipo de registro
     * @param values valores dos campos
     * @return linha do arquivo com 240 caracteres
     */
    public String assemble(RecordType type, Map<String, String> values) {
        StringBuilder sb = new StringBuilder(240);
        for (RecordType.Field field : type.getFields()) {
            String value = values.getOrDefault(field.name(), "");
            if (value.length() > field.length()) {
                value = value.substring(0, field.length());
            }
            sb.append(StringUtils.rightPad(value, field.length()));
        }
        return StringUtils.rightPad(sb.toString(), 240);
    }
}

