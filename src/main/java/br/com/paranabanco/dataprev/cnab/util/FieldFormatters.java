package br.com.paranabanco.dataprev.cnab.util;

import java.text.Normalizer;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility helpers for formatting CNAB fields.
 */
public final class FieldFormatters {

    private FieldFormatters() {}

    /**
     * Pads the provided value with zeros on the left until the requested length is met.
     */
    public static String zeroFill(String value, int length) {
        String content = value != null ? value : "";
        return StringUtils.leftPad(content, length, '0');
    }

    /**
     * Trims the value, converts to upper case and removes diacritics.
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().toUpperCase();
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    /**
     * Formats an alphanumeric field applying {@link #normalize(String)} and padding
     * the result with spaces on the right to match the desired length.
     */
    public static String formatAlphanumeric(String value, int length) {
        String normalized = normalize(value);
        return StringUtils.rightPad(normalized, length, ' ');
    }
}
