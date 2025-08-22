package br.com.paranabanco.dataprev.config;

import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.RemessaCreditoDTO;
import br.com.paranabanco.dataprev.job.concessao.RemessaConcessaoWriter;
import br.com.paranabanco.dataprev.job.concessao.RemessaCreditoProcessor;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class RemessaCreditoJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final RemessaCreditoProcessor remessaCreditoProcessor;

    @Value("${dataprev.remessa.output-dir}")
    private String remessaOutputDir;

    @Bean
    public Job gerarArquivoConcessaoJob() {
        return new JobBuilder("gerarArquivoConcessaoJob", jobRepository)
                .start(gerarRemessaConcessaoStep())
                .build();
    }

    @Bean
    public Step gerarRemessaConcessaoStep() {
        RemessaConcessaoWriter writer = new RemessaConcessaoWriter(remessaOutputDir);
        return new StepBuilder("gerarRemessaConcessaoStep", jobRepository)
                .<Credito, RemessaCreditoDTO>chunk(100, transactionManager)
                .reader(concessaoItemReader())
                .processor(remessaCreditoProcessor)
                .writer(writer)
                .listener(writer)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Credito> concessaoItemReader() {
        return new JpaPagingItemReaderBuilder<Credito>()
                .name("concessaoItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Credito c WHERE c.tipoCredito = 'CONCESSAO' ORDER BY c.id")
                .pageSize(100)
                .build();
    }
}
