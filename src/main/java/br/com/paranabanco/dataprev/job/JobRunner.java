package br.com.paranabanco.dataprev.job;

import br.com.paranabanco.dataprev.service.CreditoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Log
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app.jobs", name = "runOnStartup", havingValue = "true", matchIfMissing = true)
public class JobRunner {

    private final CreditoService creditoService;
    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws Exception {
        log.info("EXECUTANDO O JOB DE REMESSA DE CRÉDITOS...");
        ensureSpringBatchSchema();
        // A presença de JobRepository/JobExplorer força a infraestrutura do Spring Batch
        // a estar inicializada (e o schema criado via spring.batch.jdbc.initialize-schema)
        creditoService.processarRemessaCredito();
        log.info("JOB FINALIZADO.");
    }

    private void ensureSpringBatchSchema() {
        try {
            Integer exists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='dbo' AND TABLE_NAME='BATCH_JOB_INSTANCE'",
                    new SingleColumnRowMapper<>(Integer.class)
            );
            if (exists != null && exists == 0) {
                log.info("Spring Batch schema não encontrado. Aplicando schema padrão SQL Server...");
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-sqlserver.sql"));
                populator.execute(dataSource);
                log.info("Schema do Spring Batch criado.");
            }
        } catch (Exception e) {
            log.warning("Falha ao garantir schema do Spring Batch: " + e.getMessage());
        }
    }
}
