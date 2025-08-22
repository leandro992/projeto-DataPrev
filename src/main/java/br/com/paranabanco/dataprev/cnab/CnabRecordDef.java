package br.com.paranabanco.dataprev.cnab;

import br.com.paranabanco.dataprev.cnab.util.FieldFormatters;
import br.com.paranabanco.dataprev.cnab.util.NumericUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Definition of a CNAB record used for parsing positional files.
 */
public class CnabRecordDef {

    private final List<Field> fields;

    public CnabRecordDef(List<Field> fields) {
        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    /**
     * Parses a line according to the configured fields applying the adequate
     * formatters.
     */
    public Map<String, Object> parse(String line) {
        Map<String, Object> values = new HashMap<>();
        int pos = 0;
        for (Field f : fields) {
            int end = Math.min(pos + f.length(), line.length());
            String raw = line.substring(pos, end);
            Object value;
            if (f.type() == FieldType.ALPHA) {
                value = FieldFormatters.normalize(raw);
            } else {
                value = NumericUtils.parse(raw, f.decimals());
            }
            values.put(f.name(), value);
            pos += f.length();
        }
        return values;
    }

    /** Field definition within a record. */
    public record Field(String name, int length, FieldType type, int decimals) {
        public Field(String name, int length, FieldType type) {
            this(name, length, type, 0);
        }
    }

    /** Supported field types. */
    public enum FieldType {
        ALPHA,
        NUMERIC
    }
}
