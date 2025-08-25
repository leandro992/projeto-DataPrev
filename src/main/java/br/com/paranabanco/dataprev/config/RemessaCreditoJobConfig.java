package br.com.paranabanco.dataprev.config;

import br.com.paranabanco.dataprev.utils.CnabRecord;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RemessaCreditoJobConfig {
    @Bean
    public Job gerarArquivoConcessaoJob(JobRepository jobRepository,
                                        @Qualifier("gerarRemessaConcessaoStep") Step gerarRemessaConcessaoStep) {
        return new JobBuilder("gerarArquivoConcessaoJob", jobRepository)
                .start(gerarRemessaConcessaoStep)   // <- passa o bean Step, sem chamar método
                .build();
    }

    @Bean
    public Step gerarRemessaConcessaoStep(@Qualifier("dummyCnabReader") ItemReader<CnabRecord> reader,
                                          @Qualifier("dummyCnabProcessor") ItemProcessor<CnabRecord, CnabRecord> processor,
                                          @Qualifier("dummyCnabWriter") ItemWriter<CnabRecord> writer,
                                          JobRepository jobRepository,
                                          PlatformTransactionManager tx) {
        return new StepBuilder("gerarRemessaConcessaoStep", jobRepository)
                .<CnabRecord, CnabRecord>chunk(500, tx)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(writer)
                .build();
    }

    // Beans dummy para satisfazer as dependências
    @Bean
    @Qualifier("dummyCnabReader")
    public ItemReader<CnabRecord> dummyCnabReader() {
        return new ItemReader<CnabRecord>() {
            @Override
            public CnabRecord read() throws Exception {
                return null; // Implementação dummy
            }
        };
    }

    @Bean
    @Qualifier("dummyCnabProcessor")
    public ItemProcessor<CnabRecord, CnabRecord> dummyCnabProcessor() {
        return item -> item; // Pass-through
    }

    @Bean
    @Qualifier("dummyCnabWriter")
    public ItemWriter<CnabRecord> dummyCnabWriter() {
        return new ItemWriter<CnabRecord>() {
            @Override
            public void write(Chunk<? extends CnabRecord> chunk) throws Exception {
                // Implementação dummy - não faz nada
            }
        };
    }
}