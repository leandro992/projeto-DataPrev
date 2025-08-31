package br.com.paranabanco.dataprev.batch.core.concessao;

import br.com.paranabanco.dataprev.cnab.CnabRecord;
import br.com.paranabanco.dataprev.infra.config.io.OutputPathResolver;
import br.com.paranabanco.dataprev.cnab.PositionalFormatter;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RemessaConcessaoWriter implements ItemWriter<CnabRecord> {

    private final OutputPathResolver outputPathResolver;
    private final PositionalFormatter positionalFormatter;
    private final Charset charset;
    private final String fileName;

    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    public RemessaConcessaoWriter(OutputPathResolver outputPathResolver,
                                  PositionalFormatter positionalFormatter,
                                  @Value("${app.cnab.charset:ISO-8859-1}") String charsetName,
                                  @Value("${app.output.fileName:FHMLCON16.D0000001.d}") String fileName) {
        this.outputPathResolver = outputPathResolver;
        this.positionalFormatter = positionalFormatter;
        this.charset = Charset.forName(charsetName);
        this.fileName = fileName;
    }

    @Override
    public void write(Chunk<? extends CnabRecord> chunk) throws Exception {
        Path dir = outputPathResolver.outboxDir();     // agora pode apontar para ./connect-volume/outbox
        Files.createDirectories(dir);                       // garante a pasta
        String outName = fileName.replace("${date}", LocalDate.now().format(DATE));
        Path file = dir.resolve(outName);

        // mantém APPEND (mínima mudança). Para atomicidade completa, troque para ItemStreamWriter.
        try (BufferedWriter bw = Files.newBufferedWriter(
                file, charset,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            for (CnabRecord rec : chunk.getItems()) {
                String line = positionalFormatter.format(rec);
                bw.write(line);
                bw.newLine();
            }
        }

        System.out.println("Arquivo gerado em: " + file.toAbsolutePath());
    }
}
