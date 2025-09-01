package br.com.paranabanco.dataprev.job;

import lombok.extern.java.Log;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Log
public class SimpleTestJob {

    @Bean
    public Job testJob(JobRepository jobRepository, Step testStep) {
        return new JobBuilder("testJob", jobRepository)
                .start(testStep)
                .build();
    }

    @Bean
    public Step testStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("testStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Executando job de teste do Spring Batch!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}

