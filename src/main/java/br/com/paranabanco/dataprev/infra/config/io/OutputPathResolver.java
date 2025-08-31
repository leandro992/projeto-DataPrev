package br.com.paranabanco.dataprev.infra.config.io;

import br.com.paranabanco.dataprev.cnab.AppCnabProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OutputPathResolver {
    private final AppCnabProperties props;
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    public Path outboxDir() {
        try { Files.createDirectories(props.getOutputDir()); }
        catch (Exception e) { throw new UncheckedIOException(new java.io.IOException(e)); }
        return props.getOutputDir();
    }

    public Path retornoDir() {
        try { Files.createDirectories(props.getRetornoDir()); }
        catch (Exception e) { throw new UncheckedIOException(new java.io.IOException(e)); }
        return props.getRetornoDir();
    }

    public Path nextRemessaPath() {
        String name = props.getOutputName();
        // Suporte simples a placeholder de data
        name = name.replace("${date}", LocalDate.now().format(DATE));
        return outboxDir().resolve(name);
    }

    public Path resolveBaseDir() {
        // Retorna o diretório padrão para saída quando não especificado
        return Paths.get("src/main/resources/generated");
    }
}
