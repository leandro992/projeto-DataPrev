package br.com.paranabanco.dataprev.cnab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility class to write CNAB records to a file.
 */
public class CnabWriter {

    private final Path file;

    public CnabWriter(Path file) {
        this.file = file;
    }

    /**
     * Writes the provided records to the configured file.
     */
    public void write(List<String> lines) throws IOException {
        Files.write(file, lines);
    }

    /**
     * Convenience static method to write records to a file.
     */
    public static void write(Path file, List<String> lines) throws IOException {
        Files.write(file, lines);
    }
}
