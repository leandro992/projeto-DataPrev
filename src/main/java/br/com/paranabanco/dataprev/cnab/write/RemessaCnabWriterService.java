package br.com.paranabanco.dataprev.cnab.write;

import br.com.paranabanco.dataprev.cnab.assembler.CnabAssembler;
import br.com.paranabanco.dataprev.cnab.layout.CnabLayout;
import br.com.paranabanco.dataprev.dto.RemessaCreditoDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Facade service that exposes the {@link CnabWriter} for writing remessa files.
 * Existing components remain untouched; this service provides an alternative
 * entry point for clients that want to use the new generic writer.
 */
@Service
public class RemessaCnabWriterService {

    private final CnabWriter<RemessaCreditoDTO> writer;

    public RemessaCnabWriterService(CnabLayout<RemessaCreditoDTO> layout) {
        this.writer = new CnabWriter<>(new CnabAssembler<>(layout));
    }

    public void write(List<RemessaCreditoDTO> remessas, Path file) throws IOException {
        writer.write(remessas, file);
    }
}
