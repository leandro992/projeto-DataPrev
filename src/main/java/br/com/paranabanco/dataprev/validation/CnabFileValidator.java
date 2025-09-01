package br.com.paranabanco.dataprev.validation;

import br.com.paranabanco.dataprev.enumeration.TipoRotulo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Validador físico de arquivos CNAB.
 *
 * <p>O objetivo desta classe é realizar as validações básicas de um arquivo no
 * formato CNAB, garantindo que o layout esteja correto antes do processamento
 * pelos jobs de batch.</p>
 */
@Slf4j
@Component
public class CnabFileValidator {

    /** Tamanho esperado de cada registro em um arquivo CNAB. */
    static final int TAMANHO_REGISTRO_ESPERADO = 480;

    /** Quantidade de registros que compõem um bloco. */
    static final int FATOR_BLOCO_ESPERADO = 39;

    /**
     * Valida apenas o header e o trailer de um arquivo CNAB. Utilizado para
     * verificações rápidas quando não é necessário validar o conteúdo completo.
     */
    public ValidationResult validarHeaderTrailer(Path arquivo) {
        try (BufferedReader reader = Files.newBufferedReader(arquivo)) {
            String primeiraLinha = reader.readLine();
            if (primeiraLinha == null || primeiraLinha.length() < 8) {
                return ValidationResult.erro("HEADER_INVALIDO", "Header de arquivo inexistente ou inválido");
            }

            if (!"0000000".equals(primeiraLinha.substring(0, 7)) ||
                !"0".equals(primeiraLinha.substring(7, 8))) {
                return ValidationResult.erro("HEADER_INVALIDO", "Header de arquivo inválido");
            }

            String linha;
            String ultimaLinha = null;
            while ((linha = reader.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    ultimaLinha = linha;
                }
            }

            if (ultimaLinha == null || ultimaLinha.length() < 8) {
                return ValidationResult.erro("TRAILER_INVALIDO", "Trailer de arquivo inexistente ou inválido");
            }

            if (!"0000000".equals(ultimaLinha.substring(0, 7)) ||
                !"9".equals(ultimaLinha.substring(7, 8))) {
                return ValidationResult.erro("TRAILER_INVALIDO", "Trailer de arquivo inválido");
            }

            return ValidationResult.sucesso("HEADER_TRAILER_OK", "Header e trailer válidos");
        } catch (IOException e) {
            return ValidationResult.erro("ERRO_IO", "Erro ao validar header/trailer: " + e.getMessage());
        }
    }

    /**
     * Valida um arquivo CNAB completo executando três etapas:
     *  1. Validação básica do arquivo
     *  2. Validação de tamanho/estrutura e fator de bloco
     *  3. Verificação do rótulo no nome do arquivo
     */
    public ValidationResult validarArquivo(Path arquivo) {
        log.info("Iniciando validação física do arquivo: {}", arquivo);

        try {
            ValidationResult basica = validarArquivoBasico(arquivo);
            if (basica.isInvalido()) {
                return basica;
            }

            ValidationResult conteudo = validarConteudoArquivo(arquivo);
            if (conteudo.isInvalido()) {
                return conteudo;
            }

            ValidationResult rotulo = validarCompatibilidadeRotulo(arquivo);
            if (rotulo.isInvalido()) {
                return rotulo;
            }

            log.info("Arquivo validado com sucesso: {}", arquivo);
            return ValidationResult.sucesso("VALIDACAO_OK", "Arquivo validado com sucesso");
        } catch (Exception e) {
            log.error("Erro durante validação do arquivo {}", arquivo, e);
            return ValidationResult.erro("ERRO_INTERNO", "Erro interno durante validação: " + e.getMessage());
        }
    }

    /** Realiza validações básicas do arquivo. */
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
        } catch (IOException e) {
            return ValidationResult.erro("ARQ_ACESSO_ERRO", "Erro ao acessar arquivo: " + e.getMessage());
        }

        return ValidationResult.sucesso("ARQ_BASICO_OK", "Arquivo básico válido");
    }

    /**
     * Valida apenas a estrutura do arquivo CNAB.
     * <p>
     * Este método é utilizado pelos testes unitários e realiza uma verificação
     * simplificada da sequência de registros do arquivo, garantindo que os
     * headers, detalhes e trailers estejam nas posições corretas e que os lotes
     * 20 e 21 estejam presentes em pares. Não são verificadas regras de tamanho
     * de registro ou fator de bloco.</p>
     */
    private ValidationResult validarEstrutura(Path arquivo) {
        try (Stream<String> linhasStream = Files.lines(arquivo)) {
            Iterator<String> iterator = linhasStream.iterator();

            boolean headerArquivo = false;
            boolean trailerArquivo = false;
            String loteAtual = null;
            Set<String> lotes = new HashSet<>();
            Set<String> lotesComDetalhe = new HashSet<>();
            Set<String> lotesComTrailer = new HashSet<>();

            int numeroLinha = 0;
            while (iterator.hasNext()) {
                String linha = iterator.next();
                numeroLinha++;

                if (linha == null || linha.trim().isEmpty()) {
                    continue;
                }

                if (linha.length() < 8) {
                    String msg = String.format(
                        "Linha %d muito curta para identificar tipo de registro", numeroLinha);
                    return ValidationResult.erro("ESTRUTURA_LINHA_CURTA", msg);
                }

                String tipoRegistro = linha.substring(7, 8);
                String numeroLote = linha.substring(3, 7);

                if ("0".equals(tipoRegistro)) {
                    if (headerArquivo) {
                        return ValidationResult.erro("HEADER_ARQ_DUP", "Múltiplos headers de arquivo encontrados");
                    }
                    headerArquivo = true;
                } else if ("1".equals(tipoRegistro)) {
                    if (!headerArquivo) {
                        return ValidationResult.erro("LOTE_ANTES_HEADER_ARQ", "Header de lote antes do header de arquivo");
                    }
                    if (loteAtual != null) {
                        return ValidationResult.erro("LOTE_DUP", "Novo header de lote encontrado antes do trailer do lote anterior");
                    }
                    loteAtual = numeroLote;
                    if (lotes.contains(numeroLote)) {
                        return ValidationResult.erro("LOTE_REPETIDO", "Múltiplos headers para o lote " + numeroLote);
                    }
                    lotes.add(numeroLote);
                } else if ("3".equals(tipoRegistro)) {
                    if (loteAtual == null || !numeroLote.equals(loteAtual)) {
                        return ValidationResult.erro("DETALHE_FORA_LOTE", "Registro de detalhe fora do lote atual");
                    }
                    lotesComDetalhe.add(numeroLote);
                } else if ("5".equals(tipoRegistro)) {
                    if (loteAtual == null || !numeroLote.equals(loteAtual)) {
                        return ValidationResult.erro("TRAILER_LOTE_FORA_SEQ", "Trailer de lote fora de sequência");
                    }
                    lotesComTrailer.add(numeroLote);
                    loteAtual = null;
                } else if ("9".equals(tipoRegistro) && "0000".equals(numeroLote)) {
                    if (loteAtual != null) {
                        return ValidationResult.erro("TRAILER_ARQ_ANTES_LOTE", "Trailer de arquivo encontrado antes do trailer do lote");
                    }
                    if (trailerArquivo) {
                        return ValidationResult.erro("TRAILER_ARQ_DUP", "Múltiplos trailers de arquivo encontrados");
                    }
                    trailerArquivo = true;
                }
            }

            if (!headerArquivo) {
                return ValidationResult.erro("HEADER_ARQ_NAO_ENCONTRADO", "Header de arquivo não encontrado");
            }
            if (!trailerArquivo) {
                return ValidationResult.erro("TRAILER_ARQ_NAO_ENCONTRADO", "Trailer de arquivo não encontrado");
            }
            if (!lotesComDetalhe.equals(lotesComTrailer) || lotes.isEmpty()) {
                return ValidationResult.erro("ESTRUTURA_LOTE_INCOMPLETA", "Estrutura de lotes incompleta");
            }

            boolean lote20 = lotes.contains("0020");
            boolean lote21 = lotes.contains("0021");
            if (lote20 ^ lote21) {
                return ValidationResult.erro("LOTE_PAR_IMPAR", "Lotes 20 e 21 devem estar presentes em pares");
            }

            return ValidationResult.sucesso("ESTRUTURA_OK", "Estrutura CNAB válida");
        } catch (IOException e) {
            return ValidationResult.erro("ERRO_IO", "Erro ao validar estrutura do arquivo: " + e.getMessage());
        }
    }

    /**
     * Valida tamanho dos registros, sequência de registros e fator de bloco em
     * uma única passagem pelo arquivo.
     */
    private ValidationResult validarConteudoArquivo(Path arquivo) {
        try (Stream<String> linhasStream = Files.lines(arquivo)) {
            Iterator<String> iterator = linhasStream.iterator();

            int numeroLinha = 0;
            long registrosValidos = 0;
            boolean headerArquivo = false;
            boolean trailerArquivo = false;
            String loteAtual = null;
            Set<String> lotes = new HashSet<>();
            Set<String> lotesComDetalhe = new HashSet<>();
            Set<String> lotesComTrailer = new HashSet<>();

            while (iterator.hasNext()) {
                String linha = iterator.next();
                numeroLinha++;

                if (linha == null || linha.trim().isEmpty()) {
                    continue;
                }

                registrosValidos++;

                if (linha.length() != TAMANHO_REGISTRO_ESPERADO) {
                    String msg = String.format(
                        "Registro na linha %d não tem %d bytes. Tamanho encontrado: %d bytes",
                        numeroLinha, TAMANHO_REGISTRO_ESPERADO, linha.length());
                    return ValidationResult.erro("TAM_REGISTRO_INVALIDO", msg);
                }

                if (linha.length() < 8) {
                    String msg = String.format(
                        "Linha %d muito curta para identificar tipo de registro", numeroLinha);
                    return ValidationResult.erro("ESTRUTURA_LINHA_CURTA", msg);
                }

                String tipoRegistro = linha.substring(7, 8);
                String numeroLote = linha.substring(3, 7);

                if ("0".equals(tipoRegistro)) {
                    // Header de arquivo
                    if (headerArquivo) {
                        return ValidationResult.erro("HEADER_ARQ_DUP", "Múltiplos headers de arquivo encontrados");
                    }
                    headerArquivo = true;
                    log.debug("Header de arquivo encontrado na linha {}", numeroLinha);
                } else if ("1".equals(tipoRegistro)) {
                    // Header de lote
                    if (!headerArquivo) {
                        return ValidationResult.erro("LOTE_ANTES_HEADER_ARQ", "Header de lote antes do header de arquivo");
                    }
                    if (loteAtual != null) {
                        return ValidationResult.erro("LOTE_DUP", "Novo header de lote encontrado antes do trailer do lote anterior");
                    }
                    loteAtual = numeroLote;
                    if (lotes.contains(numeroLote)) {
                        return ValidationResult.erro("LOTE_REPETIDO", "Múltiplos headers para o lote " + numeroLote);
                    }
                    lotes.add(numeroLote);
                    log.debug("Header de lote {} encontrado na linha {}", numeroLote, numeroLinha);
                } else if ("3".equals(tipoRegistro)) {
                    // Detalhe
                    if (loteAtual == null || !numeroLote.equals(loteAtual)) {
                        return ValidationResult.erro("DETALHE_FORA_LOTE", "Registro de detalhe fora do lote atual");
                    }
                    lotesComDetalhe.add(numeroLote);
                } else if ("5".equals(tipoRegistro)) {
                    // Trailer de lote
                    if (loteAtual == null || !numeroLote.equals(loteAtual)) {
                        return ValidationResult.erro("TRAILER_LOTE_FORA_SEQ", "Trailer de lote fora de sequência");
                    }
                    lotesComTrailer.add(numeroLote);
                    loteAtual = null;
                } else if ("9".equals(tipoRegistro) && "0000".equals(numeroLote)) {
                    // Trailer de arquivo
                    if (loteAtual != null) {
                        return ValidationResult.erro("TRAILER_ARQ_ANTES_LOTE", "Trailer de arquivo encontrado antes do trailer do lote");
                    }
                    if (trailerArquivo) {
                        return ValidationResult.erro("TRAILER_ARQ_DUP", "Múltiplos trailers de arquivo encontrados");
                    }
                    trailerArquivo = true;
                    log.debug("Trailer de arquivo encontrado na linha {}", numeroLinha);
                }
            }

            if (!headerArquivo) {
                return ValidationResult.erro("HEADER_ARQ_NAO_ENCONTRADO", "Header de arquivo não encontrado");
            }
            if (!trailerArquivo) {
                return ValidationResult.erro("TRAILER_ARQ_NAO_ENCONTRADO", "Trailer de arquivo não encontrado");
            }
            if (!lotesComDetalhe.equals(lotesComTrailer) || lotes.isEmpty()) {
                return ValidationResult.erro("ESTRUTURA_LOTE_INCOMPLETA", "Estrutura de lotes incompleta");
            }
            if (registrosValidos % FATOR_BLOCO_ESPERADO != 0) {
                String msg = String.format(
                    "Fator de bloco inválido. Esperado múltiplo de %d, encontrado %d registros",
                    FATOR_BLOCO_ESPERADO, registrosValidos);
                return ValidationResult.erro("FATOR_BLOCO_INVALIDO", msg);
            }

            // Regra específica: lotes 20 e 21 devem existir em pares
            boolean lote20 = lotes.contains("0020");
            boolean lote21 = lotes.contains("0021");
            if (lote20 ^ lote21) {
                return ValidationResult.erro("LOTE_PAR_IMPAR", "Lotes 20 e 21 devem estar presentes em pares");
            }

            log.debug("Validação de conteúdo: OK ({} registros)", registrosValidos);
            return ValidationResult.sucesso("CONTEUDO_OK", "Conteúdo do arquivo válido");
        } catch (IOException e) {
            return ValidationResult.erro("ERRO_IO", "Erro ao processar arquivo: " + e.getMessage());
        }
    }

    /**
     * Verifica se o rótulo presente no nome do arquivo é suportado pelo
     * sistema.
     */
    private ValidationResult validarCompatibilidadeRotulo(Path arquivo) {
        String nomeArquivo = arquivo.getFileName().toString();
        String rotulo = extrairRotulo(nomeArquivo);
        if (rotulo == null) {
            return ValidationResult.erro("ROTULO_INEXTRAVEL", "Não foi possível extrair rótulo do nome do arquivo: " + nomeArquivo);
        }

        if (!TipoRotulo.isRotuloSuportado(rotulo)) {
            return ValidationResult.erro("ROTULO_NAO_SUPORTADO", "Rótulo não suportado: " + rotulo);
        }

        TipoRotulo tipoRotulo = TipoRotulo.fromRotulo(rotulo);
        log.debug("Rótulo validado: {} ({})", rotulo, tipoRotulo.getDescricao());
        return ValidationResult.sucesso("ROTULO_COMPATIVEL", "Rótulo compatível: " + rotulo);
    }

    /**
     * Extrai o rótulo do nome do arquivo, considerando apenas o prefixo antes
     * da extensão.
     */
    private String extrairRotulo(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            return null;
        }

        String nomeSemExtensao = nomeArquivo;
        int ultimoPonto = nomeArquivo.lastIndexOf('.');
        if (ultimoPonto > 0) {
            nomeSemExtensao = nomeArquivo.substring(0, ultimoPonto);
        }

        for (TipoRotulo tipo : TipoRotulo.values()) {
            if (nomeSemExtensao.startsWith(tipo.getRotulo())) {
                return tipo.getRotulo();
            }
        }
        return null;
    }
}
