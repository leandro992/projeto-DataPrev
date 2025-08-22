package br.com.paranabanco.dataprev.cnab.parse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for validating CNAB files and records.
 */
public final class CnabValidator {

    private CnabValidator() {
    }

    /**
     * Ensures all lines have the expected 240 characters.
     */
    public static void validateLineLength(List<CnabLine> lines) {
        int index = 1;
        for (CnabLine line : lines) {
            if (line.content().length() != CnabLine.LENGTH) {
                throw new IllegalArgumentException("Linha " + index + " possui tamanho " + line.content().length());
            }
            index++;
        }
    }

    /**
     * Validates trailer totals against the provided segments.
     *
     * @param segments records representing segments (details)
     * @param trailers trailer records
     * @param amountField field name holding amount values
     * @param countField field name holding record counts
     */
    public static void validateTrailerTotals(List<CnabRecord> segments,
                                             List<CnabRecord> trailers,
                                             String amountField,
                                             String countField) {
        BigDecimal sum = segments.stream()
                .map(r -> new BigDecimal(r.values().getOrDefault(amountField, "0")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = segments.size();
        for (CnabRecord trailer : trailers) {
            BigDecimal trailerSum = new BigDecimal(trailer.values().getOrDefault(amountField, "0"));
            int trailerCount = Integer.parseInt(trailer.values().getOrDefault(countField, "0"));
            if (count != trailerCount || sum.compareTo(trailerSum) != 0) {
                throw new IllegalStateException("Trailer invalido na linha " + trailer.lineNumber());
            }
        }
    }

    /**
     * Checks mandatory fields in a record.
     */
    public static void requireFields(CnabRecord record, String... fields) {
        Map<String, String> values = record.values();
        for (String field : fields) {
            String value = values.get(field);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Campo obrigatorio ausente: " + field +
                        " na linha " + record.lineNumber());
            }
        }
    }
}
