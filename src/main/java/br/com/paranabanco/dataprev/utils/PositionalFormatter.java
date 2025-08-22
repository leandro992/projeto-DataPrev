package br.com.paranabanco.dataprev.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public final class PositionalFormatter {
    public static String format(String value, int length) {
        return StringUtils.rightPad(value != null ? value : "", length, ' ');
    }

    public static String format(Number value, int length) {
        String numberStr = value != null ? value.toString().replace(".", "") : "0";
        return StringUtils.leftPad(numberStr, length, '0');
    }
}
