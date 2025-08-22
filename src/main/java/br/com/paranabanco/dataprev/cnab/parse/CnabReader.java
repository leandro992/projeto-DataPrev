package br.com.paranabanco.dataprev.cnab.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads CNAB files producing {@link CnabRecord} instances.
 */
public class CnabReader {

    private final CnabLayout layout;

    public CnabReader(CnabLayout layout) {
        this.layout = layout;
    }

    public List<CnabRecord> read(Path path) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            return read(br);
        }
    }

    public List<CnabRecord> read(Reader reader) throws IOException {
        BufferedReader br = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        List<CnabRecord> records = new ArrayList<>();
        String line;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
            lineNumber++;
            if (line.isEmpty()) {
                continue;
            }
            CnabLine cnabLine = new CnabLine(line);
            Optional<String> typeOpt = layout.matchRecord(cnabLine);
            if (typeOpt.isEmpty()) {
                continue;
            }
            String type = typeOpt.get();
            Map<String, String> values = layout.parse(type, cnabLine);
            records.add(new CnabRecord(type, values, lineNumber));
        }
        return records;
    }
}
