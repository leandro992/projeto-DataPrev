package br.com.paranabanco.dataprev.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.paranabanco.dataprev.domain.LinhaDoArquivo;

import java.util.List;

@Component
public class ReaderTest implements CommandLineRunner {

    @Autowired
    private Reader reader;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- INICIANDO LEITURA BRUTA DO ARQUIVO ---");

        try {
            List<LinhaDoArquivo> linhas = reader.lerLinhasDoArquivo();
            
            System.out.println("Conteúdo lido do arquivo:");
            linhas.forEach(System.out::println);

        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
        
        System.out.println("--- LEITURA FINALIZADA ---");
    }
}