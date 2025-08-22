package br.com.paranabanco.dataprev.cnab.write;

import br.com.paranabanco.dataprev.cnab.domain.RemessaCredito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Realiza a gravação de arquivos CNAB 240.
 */
public class CnabWriter {

    private final CnabAssembler assembler;

    public CnabWriter(CnabAssembler assembler) {
        this.assembler = assembler;
    }

    /**
     * Escreve o conteúdo da {@link RemessaCredito} no caminho informado
     * utilizando o charset US_ASCII.
     */
    public void write(RemessaCredito remessa, Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.US_ASCII)) {
            if (remessa.getHeader() != null) {
                writer.write(assembler.assemble(RecordType.HEADER, remessa.getHeader()));
                writer.newLine();
            }
            for (Map<String, String> segmento : remessa.getSegmentos()) {
                writer.write(assembler.assemble(RecordType.SEGMENT, segmento));
                writer.newLine();
            }
            for (Map<String, String> trailer : remessa.getTrailers()) {
                writer.write(assembler.assemble(RecordType.TRAILER, trailer));
                writer.newLine();
            }
        }
    }
}

