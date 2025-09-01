package br.com.paranabanco.dataprev.validation;

import br.com.paranabanco.dataprev.enumeration.TipoRotulo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Validador físico de arquivos CNAB para os tipos suportados.
 * Valida estrutura, tamanho, fator de bloco e compatibilidade de rótulo.
 */
@Slf4j
@Component
public class CnabFileValidator {

    // Constantes de validação
    private static final int TAMANHO_REGISTRO_ESPERADO = 480;
    private static final int FATOR_BLOCO_ESPERADO = 39;



    /**
     * Valida apenas o header e o trailer de um arquivo CNAB.
     * Lê somente a primeira e a última linha para evitar processamento desnecessário
     * quando esses registros estiverem incorretos.
     */
    public ValidationResult validarHeaderTrailer(Path arquivo) {
        try (BufferedReader reader = Files.newBufferedReader(arquivo)) {
            String primeiraLinha = reader.readLine();
            if (primeiraLinha == null || primeiraLinha.length() < 8) {
                return ValidationResult.erro("Header de arquivo inexistente ou inválido");
            }

            if (!"0000000".equals(primeiraLinha.substring(0, 7)) ||
                !"0".equals(primeiraLinha.substring(7, 8))) {
                return ValidationResult.erro("Header de arquivo inválido");
            }

            String linha;
            String ultimaLinha = null;
            while ((linha = reader.readLine()) != null) {
                if (linha != null && !linha.trim().isEmpty()) {
                    ultimaLinha = linha;
                }
            }

            if (ultimaLinha == null || ultimaLinha.length() < 8) {
                return ValidationResult.erro("Trailer de arquivo inexistente ou inválido");
            }

            if (!"0000000".equals(ultimaLinha.substring(0, 7)) ||
                !"9".equals(ultimaLinha.substring(7, 8))) {
                return ValidationResult.erro("Trailer de arquivo inválido");
            }

            return ValidationResult.sucesso("Header e trailer válidos");
        } catch (IOException e) {
            return ValidationResult.erro("Erro ao validar header/trailer: " + e.getMessage());
        }
    }

    /**
     * Valida um arquivo CNAB completo
     */
    public ValidationResult validarArquivo(Path arquivo) {
        log.info("Iniciando validação física do arquivo: {}", arquivo.getFileName());
        
        try {
            // 1. Validação básica do arquivo
            ValidationResult resultadoBasico = validarArquivoBasico(arquivo);
            if (!resultadoBasico.isValido()) {
                return resultadoBasico;
            }
            
            // 2. Validação de tamanho, fator de bloco e estrutura em um único fluxo
            ValidationResult resultadoConteudo = validarConteudoArquivo(arquivo);
            if (!resultadoConteudo.isValido()) {
                return resultadoConteudo;
            }

            // 3. Validação de compatibilidade de rótulo
            ValidationResult resultadoRotulo = validarCompatibilidadeRotulo(arquivo);
            if (!resultadoRotulo.isValido()) {
                return resultadoRotulo;
            }
            
            log.info("Arquivo validado com sucesso: {}", arquivo.getFileName());
            return ValidationResult.sucesso("VALIDACAO_OK", "Arquivo validado com sucesso");
            
        } catch (Exception e) {
            log.error("Erro durante validação do arquivo: {}", arquivo.getFileName(), e);
            return ValidationResult.erro("VALIDACAO_ERRO_INTERNO", "Erro interno durante validação: " + e.getMessage());
        }
    }

    /**
     * Validação básica do arquivo
     */
    private ValidationResult validarArquivoBasico(Path arquivo) {
        if (arquivo == null) {
            return ValidationResult.erro("ARQ_NULO", "Arquivo não pode ser nulo");
        }

        if (!Files.exists(arquivo)) {
            return ValidationResult.erro("ARQ_INEXISTENTE", "Arquivo não existe: " + arquivo);
        }

        if (!Files.isRegularFile(arquivo)) {
            return ValidationResult.erro("ARQ_NAO_REGULAR", "Caminho não é um arquivo: " + arquivo);
        }

        try {
            long tamanho = Files.size(arquivo);
            if (tamanho == 0) {
                return ValidationResult.erro("ARQ_VAZIO", "Arquivo está vazio");
            }

            log.debug("Arquivo básico validado: {} (tamanho: {} bytes)", arquivo.getFileName(), tamanho);
            return ValidationResult.sucesso("ARQ_BASICO_OK", "Arquivo básico válido");

        } catch (IOException e) {
            return ValidationResult.erro("ARQ_ACESSO_ERRO", "Erro ao acessar arquivo: " + e.getMessage());
        }
    }

    /**
     * Valida tamanho dos registros, fator de bloco e estrutura em uma única passagem
     */
    private ValidationResult validarConteudoArquivo(Path arquivo) {
        try (Stream<String> linhasStream = Files.lines(arquivo)) {
            Iterator<String> iterator = linhasStream.iterator();
            int numeroLinha = 0;
            long registrosValidos = 0;
            boolean headerArquivoEncontrado = false;
            boolean headerLoteEncontrado = false;
            boolean detalheEncontrado = false;
            boolean trailerLoteEncontrado = false;
            boolean trailerArquivoEncontrado = false;

            while (iterator.hasNext()) {
                String linha = iterator.next();
                numeroLinha++;

                if (linha == null || linha.trim().isEmpty()) {
                    continue;
                }

                registrosValidos++;

                if (linha.length() != TAMANHO_REGISTRO_ESPERADO) {
                    return ValidationResult.erro(
                        String.format("Registro na linha %d não tem %d bytes. Tamanho encontrado: %d bytes",
                            numeroLinha, TAMANHO_REGISTRO_ESPERADO, linha.length())
                    );
                }

                if (linha.length() < 8) {
                    return ValidationResult.erro(
                        String.format("Linha %d muito curta para identificar tipo de registro", numeroLinha)
                        "ESTRUTURA_LINHA_CURTA",
                        String.format("Linha %d muito curta para identificar tipo de registro", i + 1)
                    );
                }

                String tipoRegistro = linha.substring(7, 8);
                String bancoLote = linha.substring(0, 7);

                if ("0".equals(tipoRegistro) && "0000000".equals(bancoLote)) {
                    if (headerArquivoEncontrado) {
                        return ValidationResult.erro("ESTRUTURA_HEADER_ARQ_DUP", "Múltiplos headers de arquivo encontrados");
                    }
                    headerArquivoEncontrado = true;
                    log.debug("Header de arquivo encontrado na linha {}", numeroLinha);

                } else if ("1".equals(tipoRegistro) && bancoLote.startsWith("0001")) {
                    if (!headerArquivoEncontrado) {
                        return ValidationResult.erro("ESTRUTURA_LOTE_ANTES_ARQ", "Header de lote encontrado antes do header de arquivo");
                    }
                    if (headerLoteEncontrado) {
                        return ValidationResult.erro("ESTRUTURA_HEADER_LOTE_DUP", "Múltiplos headers de lote encontrados");
                    }
                    headerLoteEncontrado = true;
                    log.debug("Header de lote encontrado na linha {}", numeroLinha);

                } else if ("3".equals(tipoRegistro) && bancoLote.startsWith("0001")) {
                    if (!headerLoteEncontrado) {
                        return ValidationResult.erro("ESTRUTURA_DETALHE_SEM_HEADER", "Detalhe encontrado antes do header de lote");
                    }
                    detalheEncontrado = true;
                    log.debug("Registro de detalhe encontrado na linha {}", numeroLinha);

                } else if ("5".equals(tipoRegistro) && bancoLote.startsWith("0001")) {
                    if (!detalheEncontrado) {
                        return ValidationResult.erro("ESTRUTURA_TRAILER_LOTE_SEM_DETALHE", "Trailer de lote encontrado sem registros de detalhe");
                    }
                    if (trailerLoteEncontrado) {
                        return ValidationResult.erro("ESTRUTURA_TRAILER_LOTE_DUP", "Múltiplos trailers de lote encontrados");
                    }
                    trailerLoteEncontrado = true;
                    log.debug("Trailer de lote encontrado na linha {}", numeroLinha);

                } else if ("9".equals(tipoRegistro) && "0000000".equals(bancoLote)) {
                    if (!trailerLoteEncontrado) {
                        return ValidationResult.erro("ESTRUTURA_TRAILER_ARQ_SEM_LOTE", "Trailer de arquivo encontrado sem trailer de lote");
                    }
                    if (trailerArquivoEncontrado) {
                        return ValidationResult.erro("ESTRUTURA_TRAILER_ARQ_DUP", "Múltiplos trailers de arquivo encontrados");
                    }
                    trailerArquivoEncontrado = true;
                    log.debug("Trailer de arquivo encontrado na linha {}", numeroLinha);
                }
            }

            if (!headerArquivoEncontrado) {
                return ValidationResult.erro("ESTRUTURA_HEADER_ARQ_NAO_ENCONTRADO", "Header de arquivo não encontrado");
            }
            if (!headerLoteEncontrado) {
                return ValidationResult.erro("ESTRUTURA_HEADER_LOTE_NAO_ENCONTRADO", "Header de lote não encontrado");
            }
            if (!detalheEncontrado) {
                return ValidationResult.erro("ESTRUTURA_DETALHE_NAO_ENCONTRADO", "Nenhum registro de detalhe encontrado");
            }
            if (!trailerLoteEncontrado) {
                return ValidationResult.erro("ESTRUTURA_TRAILER_LOTE_NAO_ENCONTRADO", "Trailer de lote não encontrado");
            }
            if (!trailerArquivoEncontrado) {
                return ValidationResult.erro("ESTRUTURA_TRAILER_ARQ_NAO_ENCONTRADO", "Trailer de arquivo não encontrado");
            }

            if (registrosValidos % FATOR_BLOCO_ESPERADO != 0) {
                return ValidationResult.erro(
                    String.format("Fator de bloco inválido. Esperado múltiplo de %d, encontrado %d registros",
                        FATOR_BLOCO_ESPERADO, registrosValidos)
                );
            }

            log.debug("Validação de conteúdo: OK ({} registros)", registrosValidos);
            return ValidationResult.sucesso("Conteúdo do arquivo válido");
            log.debug("Validação de estrutura: OK (HEADER → DETALHE → TRAILER)");
            return ValidationResult.sucesso("ESTRUTURA_OK", "Estrutura CNAB válida");

        } catch (IOException e) {
            return ValidationResult.erro("Erro ao processar arquivo: " + e.getMessage());
            return ValidationResult.erro("ESTRUTURA_ERRO", "Erro ao validar estrutura do arquivo: " + e.getMessage());
        }
    }

    /**
     * Valida compatibilidade do rótulo com o tipo de arquivo
     */
    private ValidationResult validarCompatibilidadeRotulo(Path arquivo) {
        String nomeArquivo = arquivo.getFileName().toString();
        
        // Extrai o rótulo do nome do arquivo
        String rotulo = extrairRotulo(nomeArquivo);
        if (rotulo == null) {
            return ValidationResult.erro("ROTULO_INEXTRAVEL", "Não foi possível extrair rótulo do nome do arquivo: " + nomeArquivo);
        }

        // Verifica se o rótulo é suportado
        if (!TipoRotulo.isRotuloSuportado(rotulo)) {
            return ValidationResult.erro("ROTULO_NAO_SUPORTADO", "Rótulo não suportado: " + rotulo);
        }
        
        TipoRotulo tipoRotulo = TipoRotulo.fromRotulo(rotulo);
        log.debug("Rótulo validado: {} ({})", rotulo, tipoRotulo.getDescricao());
        
        return ValidationResult.sucesso("ROTULO_COMPATIVEL", "Rótulo compatível: " + rotulo);
    }

    /**
     * Extrai o rótulo do nome do arquivo
     */
    private String extrairRotulo(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            return null;
        }
        
        // Remove extensão se existir
        String nomeSemExtensao = nomeArquivo;
        int ultimoPonto = nomeArquivo.lastIndexOf('.');
        if (ultimoPonto > 0) {
            nomeSemExtensao = nomeArquivo.substring(0, ultimoPonto);
        }
        
        // Procura por padrões de rótulo conhecidos
        for (TipoRotulo tipo : TipoRotulo.values()) {
            if (nomeSemExtensao.startsWith(tipo.getRotulo())) {
                return tipo.getRotulo();
            }
        }
        
        return null;
    }
}
