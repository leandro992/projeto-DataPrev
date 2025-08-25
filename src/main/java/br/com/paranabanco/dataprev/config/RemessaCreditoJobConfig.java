package br.com.paranabanco.dataprev.config;

import br.com.paranabanco.dataprev.job.concessao.RemessaConcessaoWriter;
import br.com.paranabanco.dataprev.utils.CnabRecord;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RemessaCreditoJobConfig {
    @Bean
    public Job gerarArquivoConcessaoJob(JobRepository jobRepository,
                                        Step gerarRemessaConcessaoStep) {
        return new JobBuilder("gerarArquivoConcessaoJob", jobRepository)
                .start(gerarRemessaConcessaoStep)
                .build();
    }

    @Bean
    public Step gerarRemessaConcessaoStep(ItemReader<CnabRecord> reader,
                                          RemessaConcessaoWriter writer,
                                          JobRepository jobRepository,
                                          PlatformTransactionManager tx) {
        return new StepBuilder("gerarRemessaConcessaoStep", jobRepository)
                .<CnabRecord, CnabRecord>chunk(500, tx)
                .reader(reader)
                .writer(writer)
                .listener(writer)
                .build();
    }
}