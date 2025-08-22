package br.com.paranabanco.dataprev.cnab.parse;

import java.util.Map;

/**
 * Parsed CNAB record with type, field values and line number.
 */
public record CnabRecord(String type, Map<String, String> values, int lineNumber) {
}
