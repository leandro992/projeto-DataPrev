package br.com.paranabanco.dataprev.utils;

import br.com.paranabanco.dataprev.cnab.PositionalFormatter;
import br.com.paranabanco.dataprev.infra.config.io.OutputPathResolver;
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
    private final OutputPathResolver resolver;
    private BufferedWriter writer;

    public CnabItemWriter(Path outputDir, String outputName, PositionalFormatter formatter,
                          OutputPathResolver resolver) {
        this.outputDir = outputDir;
        this.outputName = outputName;
        this.formatter = formatter;
        this.resolver = resolver;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            Path dir;
            if (outputDir != null) {
                dir = outputDir;
                Files.createDirectories(dir);
            } else {
                dir = resolver.resolveBaseDir();
            }

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

    private static String defaultName() {
        // Ex.: FHMLES18.B254.DyyyyMMddHHmmss.txt (Especial)
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "FHMLES18.B254.D" + ts + ".txt";
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
