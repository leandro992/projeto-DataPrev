package br.com.paranabanco.dataprev.job.concessao;

import br.com.paranabanco.dataprev.domain.Beneficio;
import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.RemessaCreditoDTO;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemessaConcessaoWriter implements ItemWriter<RemessaCreditoDTO> {

    private final String outputFilePath;
    private final List<Credito> accumulatedCredits = new ArrayList<>();

    public RemessaConcessaoWriter(String outputDirectory) {
        this.outputFilePath = Paths.get(outputDirectory, "FSUBCON1n.txt").toString();
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        accumulatedCredits.clear();
    }

    @Override
    public void write(Chunk<? extends RemessaCreditoDTO> chunk) throws Exception {
        for (RemessaCreditoDTO item : chunk.getItems()) {
            accumulatedCredits.add(item.getCredito());
        }
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) throws IOException {
        Path path = Paths.get(outputFilePath);
        Files.createDirectories(path.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            // --- GERAÇÃO DO LOTE 20 ---
            writer.write(RemessaConcessaoLayoutBuilder.buildHeaderLote20e21("20", "01")); // Meio de Pagto 01 = Cartão Magnético
            writer.newLine();

            BigDecimal totalValorLote20 = BigDecimal.ZERO;
            for (Credito credito : accumulatedCredits) {
                writer.write(RemessaConcessaoLayoutBuilder.buildDetalheLote20(credito));
                writer.newLine();
                totalValorLote20 = totalValorLote20.add(credito.getValorLiquidoCredito());
            }

            writer.write(RemessaConcessaoLayoutBuilder.buildTrailerLote20(accumulatedCredits.size(), totalValorLote20));
            writer.newLine();

            // --- GERAÇÃO DO LOTE 21 ---
            Map<String, Beneficio> uniqueBeneficios = new ConcurrentHashMap<>();
            for (Credito credito : accumulatedCredits) {
                uniqueBeneficios.put(credito.getBeneficio().getNumeroBeneficio(), credito.getBeneficio());
            }

            writer.write(RemessaConcessaoLayoutBuilder.buildHeaderLote20e21("21", "01"));
            writer.newLine();

            for (Beneficio beneficio : uniqueBeneficios.values()) {
                writer.write(RemessaConcessaoLayoutBuilder.buildDetalheLote21(beneficio));
                writer.newLine();
            }

            writer.write(RemessaConcessaoLayoutBuilder.buildTrailerLote21(uniqueBeneficios.size()));
            writer.newLine();
        }
        System.out.println("Arquivo de Remessa de Concessão gerado em: " + outputFilePath);
    }
}