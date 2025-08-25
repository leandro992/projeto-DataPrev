package br.com.paranabanco.dataprev.utils;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OutputPathResolver {

    /** Retorna o diretório `src/main/resources/generated` (cria se não existir). */
    public Path resolveBaseDir() throws RuntimeException {
        Path dir = Paths.get(System.getProperty("user.dir"),
                "src", "main", "resources", "generated").toAbsolutePath();
        try {
            Files.createDirectories(dir);
            return dir;
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível criar o diretório de saída: " + dir, e);
        }
    }
}
