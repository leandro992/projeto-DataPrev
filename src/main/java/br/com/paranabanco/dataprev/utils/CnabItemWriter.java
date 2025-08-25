package br.com.paranabanco.dataprev.utils;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CnabItemWriter implements ItemStreamWriter<String> {

    private final Path outputDir;
    private final String outputName;
    private final PositionalFormatter formatter;
    private BufferedWriter writer;

    public CnabItemWriter(Path outputDir, String outputName, PositionalFormatter formatter) {
        this.outputDir = outputDir;
        this.outputName = outputName;
        this.formatter = formatter;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            Path dir = resolveWritableResourcesDir(outputDir);
            Files.createDirectories(dir);

            String fileName = (outputName == null || outputName.isBlank())
                    ? defaultName()
                    : outputName;

            Path file = dir.resolve(fileName);
            writer = Files.newBufferedWriter(file,
                    formatter.getCharset(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new ItemStreamException("Falha ao abrir writer CNAB", e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException { }

    @Override
    public void close() throws ItemStreamException {
        if (writer != null) {
            try { writer.close(); } catch (IOException ignored) {}
        }
    }

    private static Path resolveWritableResourcesDir(Path configured) {
        if (configured != null) return configured;
        // padrão: src/main/resources/generated
        return Paths.get("src", "main", "resources", "generated");
    }

    private static String defaultName() {
        // Ex.: HMLCES18.B254.DyyyyMMddHHmmss.txt
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "HMLCES18.B254.D" + ts + ".txt";
    }

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        for (String line : chunk) {
            writer.write(line);
            writer.write(System.lineSeparator());
        }
        writer.flush();
    }
}
