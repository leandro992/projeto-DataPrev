package br.com.paranabanco.dataprev.cnab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CnabReaderWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void readWriteReadCnabFile() throws IOException {
        Path originalPath = Paths.get("src", "main", "resources", "HMLCES18.B254.D0000001.txt");

        // Read original records
        List<String> originalLines = CnabReader.read(originalPath);
        assertFalse(originalLines.isEmpty(), "Arquivo original deve conter registros");
        originalLines.forEach(line -> assertEquals(240, line.length(), "Cada registro deve ter tamanho 240"));

        long originalSum = sumDigits(originalLines);
        int originalCount = originalLines.size();

        // Write to temporary file
        Path tempFile = tempDir.resolve("cnab-temp.txt");
        CnabWriter.write(tempFile, originalLines);

        // Read back
        List<String> rereadLines = CnabReader.read(tempFile);
        rereadLines.forEach(line -> assertEquals(240, line.length(), "Cada registro deve ter tamanho 240"));
        assertEquals(originalCount, rereadLines.size(), "Contagem de registros deve ser mantida");

        long rereadSum = sumDigits(rereadLines);
        assertEquals(originalSum, rereadSum, "Somatório dos dígitos deve ser mantido");

        assertEquals(originalLines, rereadLines, "Conteúdo reescrito deve ser igual ao original");
    }

    private long sumDigits(List<String> lines) {
        long sum = 0;
        for (String line : lines) {
            for (char c : line.toCharArray()) {
                if (Character.isDigit(c)) {
                    sum += c - '0';
                }
            }
        }
        return sum;
    }
}
