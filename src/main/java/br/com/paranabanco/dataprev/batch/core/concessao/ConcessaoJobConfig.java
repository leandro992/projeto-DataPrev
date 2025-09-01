package br.com.paranabanco.dataprev.batch.core.concessao;

import br.com.paranabanco.dataprev.batch.core.concessao.processor.ConcessaoProcessor;
import br.com.paranabanco.dataprev.batch.core.concessao.reader.ConcessaoFileReader;
import br.com.paranabanco.dataprev.batch.core.concessao.writer.ConcessaoWriter;
import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.enumeration.TipoRotulo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuração do job específico para processamento de arquivos de concessão (FHMLCON16).
 * Este job processa arquivos CNAB de concessão seguindo as regras específicas do lote 20.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConcessaoJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ConcessaoFileReader concessaoFileReader;
    private final ConcessaoProcessor concessaoProcessor;
    private final ConcessaoWriter concessaoWriter;

    /**
     * Job principal para processamento de concessão
     */
    @Bean
    public Job concessaoJob() {
        return new JobBuilder("concessaoJob", jobRepository)
                .start(validarArquivoStep())
                .next(processarConcessaoStep())
                .next(finalizarProcessamentoStep())
                .build();
    }

    /**
     * Step 1: Validação do arquivo de concessão
     */
    @Bean
    public Step validarArquivoStep() {
        return new StepBuilder("validarArquivoStep", jobRepository)
                .tasklet(validarArquivoTasklet(), transactionManager)
                .build();
    }

    /**
     * Step 2: Processamento dos registros de concessão
     */
    @Bean
    public Step processarConcessaoStep() {
        return new StepBuilder("processarConcessaoStep", jobRepository)
                .<String, Credito>chunk(100, transactionManager)
                .reader(concessaoFileReader)
                .processor(concessaoProcessor)
                .writer(concessaoWriter)
                .build();
    }

    /**
     * Step 3: Finalização do processamento
     */
    @Bean
    public Step finalizarProcessamentoStep() {
        return new StepBuilder("finalizarProcessamentoStep", jobRepository)
                .tasklet(finalizarProcessamentoTasklet(), transactionManager)
                .build();
    }

    /**
     * Tasklet para validação do arquivo
     */
    @Bean
    public Tasklet validarArquivoTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== INICIANDO VALIDAÇÃO DO ARQUIVO DE CONCESSÃO ===");
            
            String nomeArquivo = chunkContext.getStepContext()
                    .getJobParameters()
                    .get("nomeArquivo")
                    .toString();
            
            log.info("Validando arquivo: {}", nomeArquivo);
            
            // Validações específicas para concessão
            if (!nomeArquivo.startsWith(TipoRotulo.CONCESSAO.getRotulo())) {
                throw new IllegalArgumentException("Arquivo não é do tipo concessão: " + nomeArquivo);
            }
            
            log.info("Arquivo de concessão validado com sucesso: {}", nomeArquivo);
            return RepeatStatus.FINISHED;
        };
    }

    /**
     * Tasklet para finalização do processamento
     */
    @Bean
    public Tasklet finalizarProcessamentoTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== FINALIZANDO PROCESSAMENTO DE CONCESSÃO ===");
            
            String nomeArquivo = chunkContext.getStepContext()
                    .getJobParameters()
                    .get("nomeArquivo")
                    .toString();
            
            log.info("Processamento de concessão finalizado para arquivo: {}", nomeArquivo);
            log.info("Registros processados com sucesso");
            
            return RepeatStatus.FINISHED;
        };
    }
}
