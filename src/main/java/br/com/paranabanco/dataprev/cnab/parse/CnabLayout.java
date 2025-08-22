package br.com.paranabanco.dataprev.cnab.parse;

import java.util.Map;
import java.util.Optional;

/**
 * Layout used for matching and parsing CNAB lines.
 */
public interface CnabLayout {

    /**
     * Determines the record type for the given line.
     *
     * @return optional record type identifier
     */
    Optional<String> matchRecord(CnabLine line);

    /**
     * Parses the line according to the record type, returning a map of field values.
     */
    Map<String, String> parse(String recordType, CnabLine line);
}
