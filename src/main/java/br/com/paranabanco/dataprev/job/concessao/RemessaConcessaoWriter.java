package br.com.paranabanco.dataprev.job.concessao;

import br.com.paranabanco.dataprev.utils.CnabRecord;
import br.com.paranabanco.dataprev.utils.OutputPathResolver;
import br.com.paranabanco.dataprev.utils.PositionalFormatter;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


@Component
public class RemessaConcessaoWriter implements ItemWriter<CnabRecord> {

    private final OutputPathResolver outputPathResolver;
    private final PositionalFormatter positionalFormatter;
    private final Charset charset;
    private final String fileName;

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
        Path dir = outputPathResolver.resolveBaseDir(); // -> resources/generated
        Path file = dir.resolve(fileName);

        // APPEND para suportar múltiplos chunks; se quiser “limpar” a cada execução,
        // apague o arquivo no início do job com um listener.
        try (BufferedWriter bw = Files.newBufferedWriter(
                file, charset,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            for (CnabRecord rec : chunk.getItems()) {
                String line = positionalFormatter.format(rec); // Formatter chama o mapper internamente
                bw.write(line);
                bw.newLine();
            }
        }

        System.out.println("Arquivo gerado em: " + file.toAbsolutePath());
    }

}