package br.com.paranabanco.dataprev.utils;

// 1. ADICIONE ESTE IMPORT
import org.springframework.core.io.Resource; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.paranabanco.dataprev.domain.LinhaDoArquivo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class Reader {

    @Value("${leitura.arquivo.caminho}")
    private Resource arquivoResource;

    public List<LinhaDoArquivo> lerLinhasDoArquivo() throws Exception {
        Path caminho = Path.of(arquivoResource.getURI());
        
        List<String> todasAsLinhas = Files.readAllLines(caminho);
        
        if (todasAsLinhas.isEmpty()) {
            throw new Exception("O arquivo está vazio!");
        }

        List<LinhaDoArquivo> linhasLidas = new ArrayList<>();
        for (int i = 0; i < todasAsLinhas.size(); i++) {
            linhasLidas.add(new LinhaDoArquivo(i + 1, todasAsLinhas.get(i)));
        }

        return linhasLidas;
    }
}