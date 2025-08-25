package br.com.paranabanco.dataprev.utils;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.paranabanco.dataprev.domain.LinhaDoArquivo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class Reader {

    @Value("${leitura.arquivo.caminho}")
    private Resource arquivoResource;

    public List<LinhaDoArquivo> lerLinhasDoArquivo() throws IOException, EmptyFileException {
        List<LinhaDoArquivo> linhasLidas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(arquivoResource.getInputStream()))) {
            String linha;
            int numeroLinha = 1;
            while ((linha = reader.readLine()) != null) {
                linhasLidas.add(new LinhaDoArquivo(numeroLinha++, linha));
            }
        }

        if (linhasLidas.isEmpty()) {
            throw new EmptyFileException("O arquivo está vazio!");
        }

        return linhasLidas;
    }
}
