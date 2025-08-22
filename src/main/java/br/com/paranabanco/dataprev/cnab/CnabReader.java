package br.com.paranabanco.dataprev.cnab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility class to read CNAB files as lists of records.
 */
public class CnabReader {

    private final Path file;

    public CnabReader(Path file) {
        this.file = file;
    }

    /**
     * Reads all records from the configured file.
     */
    public List<String> readAll() throws IOException {
        return Files.readAllLines(file);
    }

    /**
     * Convenience static method to read all records from a file.
     */
    public static List<String> read(Path file) throws IOException {
        return Files.readAllLines(file);
    }
}
