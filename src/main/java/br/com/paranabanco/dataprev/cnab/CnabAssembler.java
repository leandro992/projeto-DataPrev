package br.com.paranabanco.dataprev.cnab;

import br.com.paranabanco.dataprev.cnab.util.FieldFormatters;
import br.com.paranabanco.dataprev.cnab.util.NumericUtils;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Assembles CNAB records from provided values applying the proper
 * formatting rules.
 */
public class CnabAssembler {

    private final CnabRecordDef definition;

    public CnabAssembler(CnabRecordDef definition) {
        this.definition = definition;
    }

    public String assemble(Map<String, Object> values) {
        StringBuilder sb = new StringBuilder();
        for (CnabRecordDef.Field f : definition.getFields()) {
            Object value = values.get(f.name());
            String formatted;
            if (f.type() == CnabRecordDef.FieldType.ALPHA) {
                formatted = FieldFormatters.formatAlphanumeric(value != null ? value.toString() : "", f.length());
            } else {
                BigDecimal bd = value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
                formatted = NumericUtils.format(bd, f.length(), f.decimals());
            }
            sb.append(formatted);
        }
        return sb.toString();
    }
}
