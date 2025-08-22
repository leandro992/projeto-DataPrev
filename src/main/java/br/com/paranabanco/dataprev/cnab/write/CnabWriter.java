package br.com.paranabanco.dataprev.cnab.write;

import br.com.paranabanco.dataprev.cnab.assembler.CnabAssembler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generic writer that persists the lines produced by a {@link CnabAssembler}
 * into a file.
 */
public class CnabWriter<T> {

    private final CnabAssembler<T> assembler;

    public CnabWriter(CnabAssembler<T> assembler) {
        this.assembler = assembler;
    }

    public void write(List<T> items, Path file) throws IOException {
        Files.createDirectories(file.getParent());
        List<String> lines = assembler.assemble(items);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
