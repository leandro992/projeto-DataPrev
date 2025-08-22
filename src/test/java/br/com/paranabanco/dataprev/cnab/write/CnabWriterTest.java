package br.com.paranabanco.dataprev.cnab.write;

import br.com.paranabanco.dataprev.cnab.layout.RemessaConcessaoCnabLayout;
import br.com.paranabanco.dataprev.domain.AgenciaINSS;
import br.com.paranabanco.dataprev.domain.Banco;
import br.com.paranabanco.dataprev.domain.Beneficio;
import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.domain.OrgaoPagador;
import br.com.paranabanco.dataprev.dto.RemessaCreditoDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CnabWriterTest {

    @Test
    void shouldGenerateFileUsingNewWriter() throws IOException {
        Banco banco = Banco.builder().codigoBanco("001").nomeBanco("Banco").build();
        OrgaoPagador orgao = OrgaoPagador.builder().codigoOrgaoPagador("123456").banco(banco).build();
        AgenciaINSS agencia = AgenciaINSS.builder().codigoAgencia("12345678").nomeAgencia("Agencia").build();

        Beneficio beneficio = Beneficio.builder()
                .numeroBeneficio("1234567890")
                .orgaoPagador(orgao)
                .agenciaInss(agencia)
                .inConcJudSemCpf(false)
                .build();

        Credito credito = Credito.builder()
                .beneficio(beneficio)
                .fimPeriodo(LocalDate.now())
                .inicioPeriodo(LocalDate.now().minusDays(30))
                .naturezaCredito("01")
                .dataMovimentoCredito(LocalDate.now())
                .orgaoPagador(orgao)
                .valorLiquidoCredito(BigDecimal.TEN)
                .build();

        RemessaCreditoDTO dto = RemessaCreditoDTO.builder().credito(credito).build();

        RemessaCnabWriterService service = new RemessaCnabWriterService(new RemessaConcessaoCnabLayout());
        Path tempFile = Files.createTempFile("remessa", ".txt");
        service.write(List.of(dto), tempFile);

        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(6, lines.size());
        assertTrue(lines.get(0).startsWith("/"));
        assertTrue(lines.get(1).startsWith("<"));
        assertTrue(lines.get(2).startsWith("\\"));
        assertTrue(lines.get(3).startsWith("/"));
        assertTrue(lines.get(4).startsWith("<"));
        assertTrue(lines.get(5).startsWith("\\"));
    }
}
