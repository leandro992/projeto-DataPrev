package br.com.paranabanco.dataprev.config;

import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.RemessaCreditoDTO;
import br.com.paranabanco.dataprev.job.concessao.RemessaConcessaoWriter;
import br.com.paranabanco.dataprev.job.concessao.RemessaCreditoProcessor;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RemessaCreditoJobConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private RemessaCreditoProcessor remessaCreditoProcessor;

    @Bean
    public Job gerarArquivoConcessaoJob() {
        return new JobBuilder("gerarArquivoConcessaoJob", jobRepository)
                .start(gerarRemessaConcessaoStep())
                .build();
    }

    @Bean
    public Step gerarRemessaConcessaoStep() {
        RemessaConcessaoWriter writer = new RemessaConcessaoWriter("build/output");
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