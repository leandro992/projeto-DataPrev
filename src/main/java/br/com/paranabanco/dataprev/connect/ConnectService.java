package br.com.paranabanco.dataprev.connect;

import br.com.paranabanco.dataprev.dto.BuscarArquivoRequest;
import br.com.paranabanco.dataprev.dto.BuscarArquivoResponse;
import br.com.paranabanco.dataprev.dto.ListarArquivosResponse;
import br.com.paranabanco.dataprev.enumeration.TipoRotulo;
import br.com.paranabanco.dataprev.infra.config.io.OutputPathResolver;
import br.com.paranabanco.dataprev.utils.ConnectException;
import br.com.paranabanco.dataprev.validation.CnabFileValidator;
import br.com.paranabanco.dataprev.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectService {

    private static final Logger log = LoggerFactory.getLogger(ConnectService.class);

    private final ConnectClient client;
    private final OutputPathResolver resolver;
    private final JobLauncher jobLauncher;
    private final Job cnabJob;
    private final Job concessaoJob;
    private final CnabFileValidator cnabFileValidator;

    public ConnectService(ConnectClient client, OutputPathResolver resolver,
                          JobLauncher jobLauncher, Job cnabJob, 
                          @Qualifier("concessaoJob") Job concessaoJob,
                          CnabFileValidator cnabFileValidator) {
        this.client = client;
        this.resolver = resolver;
        this.jobLauncher = jobLauncher;
        this.cnabJob = cnabJob;
        this.concessaoJob = concessaoJob;
        this.cnabFileValidator = cnabFileValidator;
    }

    /**
     * Busca e baixa um arquivo do CONNECT com base no rótulo e, opcionalmente, na sequência D{n}.
     * Especificação solicitada: para FHMLCON16, usar n=6; demais rótulos pegam o "mais alto".
     */
    public Optional<Path> buscarArquivo(String rotulo, Integer n) {
        Optional<Path> destino = client.findAndDownload(rotulo, n, resolver.retornoDir());
        destino.ifPresent(d -> log.info("Arquivo {} baixado para {}", d.getFileName(), d));
        return destino;
    }

    /**
     * Mantém compatibilidade com a assinatura anterior; aplica a regra n=6 para FHMLCON16.
     */
    public Optional<Path> processar(String rotulo) {
        Integer n = ("FHMLCON16".equalsIgnoreCase(rotulo)) ? 6 : null;
        Optional<Path> destino = buscarArquivo(rotulo, n);
        destino.ifPresent(arquivo -> {
            ValidationResult cabecalhoTrailer = cnabFileValidator.validarHeaderTrailer(arquivo);
            if (cabecalhoTrailer.isValido()) {
                executarJobComArquivo(arquivo);
            } else {
                log.error("Arquivo {} falhou na validação inicial: {}", arquivo.getFileName(), cabecalhoTrailer.getMensagemFormatada());
            }
        });
        return destino;
    }

    /**
     * Busca arquivo específico usando o novo sistema de tipos
     */
    public BuscarArquivoResponse buscarArquivoEspecifico(BuscarArquivoRequest request) {
        try {
            // Valida o request
            request.validar();
            
            TipoRotulo tipoRotulo = TipoRotulo.fromRotulo(request.rotulo());
            log.info("Buscando arquivo para tipo: {} (rótulo: {})", tipoRotulo.getDescricao(), request.rotulo());
            
            Optional<Path> arquivoBaixado = client.buscarArquivoPorTipo(tipoRotulo, resolver.retornoDir());
            
            if (arquivoBaixado.isEmpty()) {
                log.warn("Nenhum arquivo encontrado para o rótulo: {}", request.rotulo());
                return BuscarArquivoResponse.naoEncontrado(request.rotulo());
            }
            
            Path arquivo = arquivoBaixado.get();
            Long tamanhoBytes = Files.size(arquivo);
            String hashArquivo = client.calcularHashArquivo(arquivo);
            
            log.info("Arquivo baixado com sucesso: {} (tamanho: {} bytes, hash: {})", 
                    arquivo.getFileName(), tamanhoBytes, hashArquivo);
            
            return BuscarArquivoResponse.sucesso(
                request.rotulo(),
                arquivo.getFileName().toString(),
                arquivo.toString(),
                tamanhoBytes,
                hashArquivo,
                null
            );
            
        } catch (IllegalArgumentException e) {
            log.error("Erro de validação na busca de arquivo: {}", e.getMessage());
            return BuscarArquivoResponse.erro(request.rotulo(), "REQUISICAO_INVALIDA", e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado na busca de arquivo para rótulo {}: {}", request.rotulo(), e.getMessage(), e);
            return BuscarArquivoResponse.erro(request.rotulo(), "ERRO_INTERNO", "Erro interno: " + e.getMessage());
        }
    }

    /**
     * Lista arquivos disponíveis para um tipo específico
     */
    public ListarArquivosResponse listarArquivosDisponiveis(String rotulo) {
        try {
            if (!TipoRotulo.isRotuloSuportado(rotulo)) {
                return ListarArquivosResponse.erro(rotulo, "Rótulo não suportado: " + rotulo);
            }
            
            TipoRotulo tipoRotulo = TipoRotulo.fromRotulo(rotulo);
            List<String> arquivos = client.listarArquivosPorTipo(tipoRotulo);
            
            if (arquivos.isEmpty()) {
                return ListarArquivosResponse.vazio(rotulo);
            }
            
            log.info("Encontrados {} arquivos para o rótulo: {}", arquivos.size(), rotulo);
            return ListarArquivosResponse.sucesso(rotulo, arquivos);
            
        } catch (Exception e) {
            log.error("Erro ao listar arquivos para rótulo {}: {}", rotulo, e.getMessage(), e);
            return ListarArquivosResponse.erro(rotulo, "Erro interno: " + e.getMessage());
        }
    }

    /**
     * Valida um arquivo específico sem processá-lo
     */
    public BuscarArquivoResponse validarArquivo(BuscarArquivoRequest request) {
        BuscarArquivoResponse response = buscarArquivoEspecifico(request);
        
        if ("SUCESSO".equals(response.status())) {
            try {
                Path arquivo = Path.of(response.caminhoLocal());
                log.info("Validando header e trailer do arquivo: {}", response.nomeArquivo());
                ValidationResult cabecalhoTrailer = cnabFileValidator.validarHeaderTrailer(arquivo);
                if (!cabecalhoTrailer.isValido()) {
                    log.error("Arquivo não passou na validação de header/trailer: {}", cabecalhoTrailer.getMensagemFormatada());
                    return BuscarArquivoResponse.erro(request.rotulo(),
                        "Arquivo baixado mas falhou na validação de header/trailer: " + cabecalhoTrailer.getMensagemFormatada(), null);
                }

                log.info("Iniciando validação física do arquivo: {}", response.nomeArquivo());
                ValidationResult validacao = cnabFileValidator.validarArquivo(arquivo);

                if (validacao.isValido()) {
                    log.info("Arquivo validado com sucesso: {}", validacao.getMensagemFormatada());
                    return BuscarArquivoResponse.sucesso(
                        request.rotulo(),
                        response.nomeArquivo(),
                        response.caminhoLocal(),
                        response.tamanhoBytes(),
                        response.hashArquivo(),
                        validacao.codigo()
                    );
                } else {
                    log.error("Arquivo não passou na validação: {}", validacao.getMensagemFormatada());
                    return BuscarArquivoResponse.erro(request.rotulo(),
                        validacao.codigo(),
                        "Arquivo baixado mas falhou na validação: " + validacao.getMensagemFormatada());
                }

            } catch (Exception e) {
                log.error("Erro ao validar arquivo {}: {}", response.nomeArquivo(), e.getMessage(), e);
                return BuscarArquivoResponse.erro(request.rotulo(), "VALIDACAO_ERRO", "Erro na validação: " + e.getMessage());
            }
        }
        
        return response;
    }

    /**
     * Busca e processa arquivo específico (busca + validação + execução do job)
     */
    public BuscarArquivoResponse buscarEProcessarArquivo(BuscarArquivoRequest request) {
        BuscarArquivoResponse response = buscarArquivoEspecifico(request);
        
        if ("SUCESSO".equals(response.status())) {
            try {
                Path arquivo = Path.of(response.caminhoLocal());

                // Validação rápida de header e trailer
                log.info("Validando header e trailer do arquivo: {}", response.nomeArquivo());
                ValidationResult cabecalhoTrailer = cnabFileValidator.validarHeaderTrailer(arquivo);
                if (!cabecalhoTrailer.isValido()) {
                    log.error("Arquivo não passou na validação de header/trailer: {}", cabecalhoTrailer.getMensagemFormatada());
                    return BuscarArquivoResponse.erro(request.rotulo(),
                        "Arquivo baixado mas falhou na validação de header/trailer: " + cabecalhoTrailer.getMensagemFormatada(), null);
                }

                // Validação física completa do arquivo antes do processamento
                log.info("Iniciando validação física do arquivo: {}", response.nomeArquivo());
                ValidationResult validacao = cnabFileValidator.validarArquivo(arquivo);

                if (!validacao.isValido()) {
                    log.error("Arquivo não passou na validação física: {}", validacao.getMensagemFormatada());
                    return BuscarArquivoResponse.erro(request.rotulo(),
                        validacao.codigo(),
                        "Arquivo baixado mas falhou na validação física: " + validacao.getMensagemFormatada());
                }

                log.info("Arquivo passou na validação física: {}", validacao.getMensagemFormatada());

                // Executa job específico baseado no tipo de rótulo
                TipoRotulo tipoRotulo = TipoRotulo.fromRotulo(request.rotulo());
                if (tipoRotulo == TipoRotulo.CONCESSAO) {
                    executarJobConcessao(arquivo);
                } else {
                    executarJobComArquivo(arquivo);
                }

                log.info("Job executado com sucesso para arquivo: {}", response.nomeArquivo());

                return BuscarArquivoResponse.sucesso(
                    request.rotulo(),
                    response.nomeArquivo(),
                    response.caminhoLocal(),
                    response.tamanhoBytes(),
                    response.hashArquivo(),
                    validacao.codigo()
                );
            } catch (Exception e) {
                log.error("Erro ao executar job para arquivo {}: {}", response.nomeArquivo(), e.getMessage(), e);
                return BuscarArquivoResponse.erro(request.rotulo(), "ERRO_JOB", "Arquivo baixado mas erro na execução do job: " + e.getMessage());
            }
        }

        return response;
    }

    /**
     * Executa o job específico de concessão
     */
    public JobExecution executarJobConcessao(Path arquivo) {
        try {
            String nomeArquivo = arquivo.getFileName().toString();
            log.info("Executando job de concessão para arquivo: {}", nomeArquivo);
            
            JobParameters params = new JobParametersBuilder()
                    .addString("nomeArquivo", nomeArquivo)
                    .addString("input.file", arquivo.toString())
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            
            // Define o nome do arquivo como propriedade do sistema para o reader
            System.setProperty("job.nomeArquivo", nomeArquivo);
            
            JobExecution execution = jobLauncher.run(concessaoJob, params);
            
            log.info("Job de concessão executado com sucesso. Status: {}", execution.getStatus());
            return execution;
            
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Job de concessão já em execução para o arquivo {}", arquivo, e);
            throw new ConnectException("Job de concessão já em execução", e);
        } catch (JobRestartException e) {
            log.error("Falha ao reiniciar job de concessão para o arquivo {}", arquivo, e);
            throw new ConnectException("Falha ao reiniciar job de concessão", e);
        } catch (JobParametersInvalidException e) {
            log.error("Parâmetros inválidos para o job de concessão do arquivo {}", arquivo, e);
            throw new ConnectException("Parâmetros inválidos para execução do job de concessão", e);
        } catch (Exception e) {
            log.error("Falha ao executar job de concessão para arquivo {}", arquivo, e);
            throw new ConnectException("Falha ao executar job de concessão", e);
        }
    }

    private JobExecution executarJobComArquivo(Path arquivo) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("input.file", arquivo.toString())
                    .addLong("run.id", System.currentTimeMillis()) // garante unicidade
                    .toJobParameters();
            log.info("Disparando job cnabJob para arquivo {}", arquivo);
            return jobLauncher.run(cnabJob, params);
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Job já em execução para o arquivo {}", arquivo, e);
            throw new ConnectException("Job já em execução", e);
        } catch (JobRestartException e) {
            log.error("Falha ao reiniciar job para o arquivo {}", arquivo, e);
            throw new ConnectException("Falha ao reiniciar job", e);
        } catch (JobParametersInvalidException e) {
            log.error("Parâmetros inválidos para o arquivo {}", arquivo, e);
            throw new ConnectException("Parâmetros inválidos para execução do job", e);
        } catch (Exception e) {
            log.error("Falha ao executar job para arquivo {}", arquivo, e);
            throw new ConnectException("Falha ao executar job", e);
        }
    }
}
