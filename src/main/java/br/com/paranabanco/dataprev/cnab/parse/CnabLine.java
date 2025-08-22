package br.com.paranabanco.dataprev.cnab.parse;

import java.util.Objects;

/**
 * Wrapper for a CNAB line with fixed length of 240 columns.
 */
public record CnabLine(String content) {

    public static final int LENGTH = 240;

    public CnabLine {
        Objects.requireNonNull(content, "content");
        if (content.length() != LENGTH) {
            throw new IllegalArgumentException(
                    "CNAB line must have exactly " + LENGTH + " characters, got " + content.length());
        }
    }

    /**
     * Returns a substring using 1-based positions. End index is inclusive.
     */
    public String slice(int start, int end) {
        int from = Math.max(0, start - 1);
        int to = Math.min(content.length(), end);
        return content.substring(from, to);
    }
}
