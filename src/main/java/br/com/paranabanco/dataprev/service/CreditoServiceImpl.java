package br.com.paranabanco.dataprev.service;

import br.com.paranabanco.dataprev.domain.Credito;
import br.com.paranabanco.dataprev.dto.CreditoDTO;
import br.com.paranabanco.dataprev.enumeration.TipoCredito;
import br.com.paranabanco.dataprev.mapper.CreditoMapper;
import br.com.paranabanco.dataprev.repository.CreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditoServiceImpl implements CreditoService {

    private  final CreditoRepository creditoRepository;
    private  final JobLauncher jobLauncher;
    private  final Job gerarArquivoConcessaoJob;
    private final CreditoMapper creditoMapper;


    @Override
    public List<Credito> buscarTodos() {
        return creditoRepository.findAll();
    }

    @Override
    public Credito buscarPorId(Long id) {
        return creditoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credito não encotrado com ID: " + id ));
    }

    @Override
    public List<Credito> buscarPorTipoCredito(TipoCredito tipoCredito) {
        return creditoRepository.findByTipoCredito(tipoCredito);
    }

    @Override
    public CreditoDTO salvar(Credito credito) {
        return creditoMapper.creditoToCreditoDTO(creditoRepository.save(credito));
    }

    @Override
    public void excluir(Long id) {
        creditoRepository.deleteById(id);
    }

    @Override
    public void processarRemessaCredito() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            jobLauncher.run(gerarArquivoConcessaoJob, jobParameters);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar remessa de crédito", e);
        }
    }
}
