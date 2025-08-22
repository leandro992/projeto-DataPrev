package br.com.paranabanco.dataprev.cnab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.paranabanco.dataprev.cnab.CnabRecordDef.Field;
import br.com.paranabanco.dataprev.cnab.CnabRecordDef.FieldType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CnabIntegrationTest {

    @Test
    void assembleAndParseRecord() {
        CnabRecordDef def = new CnabRecordDef(List.of(
                new Field("name", 10, FieldType.ALPHA),
                new Field("amount", 10, FieldType.NUMERIC, 2)));

        CnabAssembler assembler = new CnabAssembler(def);

        Map<String, Object> values = Map.of(
                "name", " João ",
                "amount", new BigDecimal("123.45"));

        String line = assembler.assemble(values);
        assertEquals("JOAO      0000012345", line);

        Map<String, Object> parsed = def.parse(line);
        assertEquals("JOAO", parsed.get("name"));
        assertEquals(new BigDecimal("123.45"), parsed.get("amount"));
    }
}
