package br.com.paranabanco.dataprev.job;

import br.com.paranabanco.dataprev.service.CreditoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log
@RequiredArgsConstructor
@Profile("dev")
@Component
public class JobRunner implements CommandLineRunner {

    private final CreditoService creditoService;

    @Override
    public void run(String... args) throws Exception {
        log.info("EXECUTANDO O JOB DE REMESSA DE CRÉDITOS...");

        creditoService.processarRemessaCredito();

        log.info("JOB FINALIZADO.");
    }
}
