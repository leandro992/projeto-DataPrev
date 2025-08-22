package br.com.paranabanco.dataprev.cnab.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import org.apache.commons.lang3.StringUtils;

/**
 * Utilities for dealing with numeric values in CNAB files.
 */
public final class NumericUtils {

    private NumericUtils() {}

    /**
     * Parses a numeric string that does not contain a decimal separator into a {@link BigDecimal}.
     *
     * @param value  the numeric string
     * @param scale  number of decimal places the value represents
     * @return the parsed {@link BigDecimal}
     */
    public static BigDecimal parse(String value, int scale) {
        String digits = StringUtils.defaultString(value).trim();
        if (digits.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale);
        }
        BigInteger integer = new BigInteger(digits);
        return new BigDecimal(integer, scale);
    }

    /**
     * Formats a {@link BigDecimal} value without a decimal separator and zero fills
     * it to the desired length.
     */
    public static String format(BigDecimal value, int length, int scale) {
        BigDecimal scaled = value.setScale(scale, RoundingMode.HALF_UP);
        String digits = scaled.movePointRight(scale).toPlainString();
        return StringUtils.leftPad(digits, length, '0');
    }
}
