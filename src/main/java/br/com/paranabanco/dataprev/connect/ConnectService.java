package br.com.paranabanco.dataprev.connect;

import br.com.paranabanco.dataprev.infra.config.io.OutputPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ConnectService {

    private static final Logger log = LoggerFactory.getLogger(ConnectService.class);

    private final ConnectClient client;
    private final OutputPathResolver resolver;
    private final JobLauncher jobLauncher;
    private final Job cnabJob;

    public ConnectService(ConnectClient client, OutputPathResolver resolver,
                          JobLauncher jobLauncher, Job cnabJob) {
        this.client = client;
        this.resolver = resolver;
        this.jobLauncher = jobLauncher;
        this.cnabJob = cnabJob;
    }

    /**
     * Busca e baixa um arquivo do CONNECT com base no rótulo e, opcionalmente, na sequência D{n}.
     * Especificação solicitada: para FHMLCON16, usar n=6.
     * Demais rótulos (FHMLMAC16, FHMLCES18) pegam o arquivo mais "alto" por ordenação de nome.
     */
    public Optional<Path> buscarArquivo(String rotulo, Integer n) {
        // O ConnectClient já filtra para considerar somente rótulos permitidos
        List<String> arquivos = client.listarArquivos(rotulo);
        if (arquivos.isEmpty()) {
            log.info("Nenhum arquivo listado no Connect para o rótulo {}", rotulo);
            return Optional.empty();
        }

        String escolhido = selecionarArquivo(arquivos, n);
        if (escolhido == null) {
            log.info("Nenhum arquivo compatível encontrado para {} com n={}", rotulo, n);
            return Optional.empty();
        }

        Path destino = resolver.retornoDir().resolve(escolhido);
        client.baixarArquivo(escolhido, destino);
        log.info("Arquivo {} baixado para {}", escolhido, destino);
        return Optional.of(destino);
    }

    /**
     * Mantém compatibilidade com a assinatura anterior; aplica a regra n=6 para FHMLCON16.
     */
    public Optional<Path> processar(String rotulo) {
        Integer n = ("FHMLCON16".equalsIgnoreCase(rotulo)) ? 6 : null;
        Optional<Path> destino = buscarArquivo(rotulo, n);
        destino.ifPresent(this::executarJobComArquivo);
        return destino;
    }

    private void executarJobComArquivo(Path arquivo) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("input.file", arquivo.toString())
                    .addLong("run.id", System.currentTimeMillis()) // garante unicidade
                    .toJobParameters();
            log.info("Disparando job cnabJob para arquivo {}", arquivo);
            jobLauncher.run(cnabJob, params);
        } catch (Exception e) {
            log.error("Falha ao executar job para arquivo {}", arquivo, e);
            throw new RuntimeException("Falha ao executar job", e);
        }
    }

    private static String selecionarArquivo(List<String> nomes, Integer n) {
        Objects.requireNonNull(nomes, "lista de nomes não pode ser nula");
        String alvoD = (n != null) ? String.format("D%07d", n) : null; // ex.: D0000006

        // 1) Se n informado, prioriza correspondência exata do segmento D
        if (alvoD != null) {
            return nomes.stream()
                    .filter(s -> s.contains(alvoD))
                    // Preferir extensões ".d" sobre outras
                    .sorted(Comparator.<String>comparingInt(s -> s.endsWith(".d") ? 0 : 1)
                            .thenComparing(Comparator.reverseOrder()))
                    .findFirst()
                    .orElse(null);
        }

        // 2) Sem n: pega o "maior" por ordenação (assumindo nomes com sequências crescentes)
        return nomes.stream()
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .orElse(null);
    }
}
