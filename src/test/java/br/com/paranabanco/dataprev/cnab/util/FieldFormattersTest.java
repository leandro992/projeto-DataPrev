package br.com.paranabanco.dataprev.cnab.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FieldFormattersTest {

    @Test
    void alphanumericFormattingRemovesDiacriticsAndUppercases() {
        String formatted = FieldFormatters.formatAlphanumeric(" João ", 10);
        assertEquals("JOAO      ", formatted);
    }

    @Test
    void zeroFillPadsWithZeros() {
        assertEquals("00123", FieldFormatters.zeroFill("123", 5));
    }
}
