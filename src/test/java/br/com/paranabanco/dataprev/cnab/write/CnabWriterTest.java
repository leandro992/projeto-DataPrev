package br.com.paranabanco.dataprev.cnab.write;

import br.com.paranabanco.dataprev.cnab.domain.RemessaCredito;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CnabWriterTest {

    @Test
    void geraArquivoComTraillerCorreto() throws Exception {
        CnabAssembler assembler = new CnabAssembler();
        CnabWriter writer = new CnabWriter(assembler);

        List<Map<String, String>> segmentos = List.of(
                Map.of("tipo", "1", "valor", "100"),
                Map.of("tipo", "1", "valor", "200")
        );

        int total = segmentos.stream().mapToInt(s -> Integer.parseInt(s.get("valor"))).sum();
        int count = segmentos.size();

        Map<String, String> trailer = Map.of(
                "tipo", "9",
                "quantidade", String.valueOf(count),
                "valorTotal", String.valueOf(total)
        );

        RemessaCredito remessa = RemessaCredito.builder()
                .header(Map.of("tipo", "0"))
                .segmentos(segmentos)
                .trailers(List.of(trailer))
                .build();

        Path tempFile = Files.createTempFile("cnab", ".txt");
        writer.write(remessa, tempFile);

        List<String> lines = Files.readAllLines(tempFile, StandardCharsets.US_ASCII);
        assertEquals(1 + segmentos.size() + 1, lines.size());
        for (String line : lines) {
            assertEquals(240, line.length());
        }

        String trailerLine = lines.get(lines.size() - 1);
        int qtd = Integer.parseInt(trailerLine.substring(1, 7).trim());
        int valorTotal = Integer.parseInt(trailerLine.substring(7, 17).trim());
        assertEquals(count, qtd);
        assertEquals(total, valorTotal);
    }
}

