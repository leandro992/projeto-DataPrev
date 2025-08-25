package br.com.paranabanco.dataprev.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.paranabanco.dataprev.domain.LinhaDoArquivo;

import java.io.IOException;
import java.util.List;

@Component
public class ReaderTest implements CommandLineRunner {

    @Autowired
    private Reader reader;

    @Override
    public void run(String... args) {
        System.out.println("--- INICIANDO LEITURA BRUTA DO ARQUIVO ---");

        try {
            List<LinhaDoArquivo> linhas = reader.lerLinhasDoArquivo();

            System.out.println("Conteúdo lido do arquivo:");
            linhas.forEach(System.out::println);

        } catch (EmptyFileException e) {
            System.err.println("Arquivo vazio: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro de I/O ao ler o arquivo: " + e.getMessage());
        }
        
        System.out.println("--- LEITURA FINALIZADA ---");
    }
}
