package br.com.paranabanco.dataprev.cnab.parse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CnabReaderTest {

    @Test
    void readFileAndCountRecords() throws IOException {
        Path path = Path.of("src/main/resources/HMLCES18.B254.D0000001.txt");
        CnabLayout layout = new SimpleLayout();
        CnabReader reader = new CnabReader(layout);
        List<CnabRecord> records = reader.read(path);

        long headers = records.stream().filter(r -> r.type().equals("HEADER")).count();
        long segments = records.stream().filter(r -> r.type().equals("SEGMENT")).count();
        long trailers = records.stream().filter(r -> r.type().equals("TRAILER")).count();

        assertEquals(32, headers, "headers");
        assertEquals(447, segments, "segments");
        assertEquals(30, trailers, "trailers");
    }

    /**
     * Minimal layout used for tests: record type is the first character.
     */
    static class SimpleLayout implements CnabLayout {
        @Override
        public Optional<String> matchRecord(CnabLine line) {
            char c = line.content().charAt(0);
            return switch (c) {
                case '1' -> Optional.of("HEADER");
                case '2' -> Optional.of("SEGMENT");
                case '3' -> Optional.of("TRAILER");
                default -> Optional.empty();
            };
        }

        @Override
        public Map<String, String> parse(String recordType, CnabLine line) {
            return Map.of();
        }
    }
}
