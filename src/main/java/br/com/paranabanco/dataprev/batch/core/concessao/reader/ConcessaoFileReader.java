package br.com.paranabanco.dataprev.batch.core.concessao.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

/**
 * Reader específico para arquivos de concessão (FHMLCON16).
 * Lê o arquivo linha por linha e retorna cada linha para processamento.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConcessaoFileReader implements ItemReader<String> {

    private Iterator<String> linhasIterator;
    private String nomeArquivo;
    private boolean arquivoCarregado = false;

    @Override
    public String read() throws Exception{
        // Carrega o arquivo na primeira chamada
        if (!arquivoCarregado) {
            carregarArquivo();
        }
        
        // Retorna próxima linha se existir
        if (linhasIterator != null && linhasIterator.hasNext()) {
            String linha = linhasIterator.next();
            log.debug("Lendo linha: {}", linha);
            return linha;
        }
        
        // Fim do arquivo
        log.info("Fim do arquivo de concessão: {}", nomeArquivo);
        return null;
    }

    /**
     * Carrega o arquivo de concessão para leitura
     */
    private void carregarArquivo() throws IOException {
        // Obtém o nome do arquivo dos parâmetros do job
        String nomeArquivoParam = System.getProperty("job.nomeArquivo");
        if (nomeArquivoParam == null || nomeArquivoParam.trim().isEmpty()) {
            throw new IllegalStateException("Nome do arquivo não foi fornecido como parâmetro do job");
        }
        
        this.nomeArquivo = nomeArquivoParam;
        log.info("Carregando arquivo de concessão: {}", nomeArquivo);
        
        // Constrói o caminho do arquivo
        Path caminhoArquivo = Paths.get("./connect-volume/retorno", nomeArquivo);
        
        // Verifica se o arquivo existe
        if (!Files.exists(caminhoArquivo)) {
            throw new IOException("Arquivo não encontrado: " + caminhoArquivo);
        }
        
        // Lê todas as linhas do arquivo
        List<String> linhas = Files.readAllLines(caminhoArquivo);
        
        // Filtra linhas vazias e comentários
        linhas = linhas.stream()
                .filter(linha -> linha != null && !linha.trim().isEmpty())
                .filter(linha -> !linha.trim().startsWith("#"))
                .toList();
        
        this.linhasIterator = linhas.iterator();
        this.arquivoCarregado = true;
        
        log.info("Arquivo carregado com sucesso: {} linhas válidas", linhas.size());
    }

    /**
     * Método para definir o arquivo a ser lido (usado em testes)
     */
    public void setArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        this.arquivoCarregado = false;
        this.linhasIterator = null;
    }

    /**
     * Método para definir o arquivo a ser lido usando Resource (usado em testes)
     */
    public void setResource(Resource resource) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            this.nomeArquivo = resource.getFilename();
            List<String> linhas = reader.lines()
                    .filter(linha -> linha != null && !linha.trim().isEmpty())
                    .filter(linha -> !linha.trim().startsWith("#"))
                    .toList();
            
            this.linhasIterator = linhas.iterator();
            this.arquivoCarregado = true;
            
            log.info("Resource carregado com sucesso: {} linhas válidas", linhas.size());
            
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar resource: " + e.getMessage(), e);
        }
    }
}
