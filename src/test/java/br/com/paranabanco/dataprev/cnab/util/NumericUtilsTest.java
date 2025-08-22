package br.com.paranabanco.dataprev.cnab.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class NumericUtilsTest {

    @Test
    void parseMonetaryValue() {
        BigDecimal value = NumericUtils.parse("0000012345", 2);
        assertEquals(new BigDecimal("123.45"), value);
    }

    @Test
    void formatMonetaryValue() {
        String formatted = NumericUtils.format(new BigDecimal("123.45"), 10, 2);
        assertEquals("0000012345", formatted);
    }
}
