package br.com.paranabanco.dataprev.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Log
public class HealthController {

    private final JobLauncher jobLauncher;
    private final Job testJob;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("springBatch", "OK");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @GetMapping("/batch-test")
    public Map<String, Object> testBatch() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution execution = jobLauncher.run(testJob, jobParameters);
            
            result.put("status", "SUCCESS");
            result.put("jobExecutionId", execution.getId());
            result.put("exitStatus", execution.getExitStatus().getExitCode());
            result.put("timestamp", System.currentTimeMillis());
            
            log.info("Job de teste executado com sucesso: " + execution.getId());
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            log.severe("Erro ao executar job de teste: " + e.getMessage());
        }
        
        return result;
    }
}

