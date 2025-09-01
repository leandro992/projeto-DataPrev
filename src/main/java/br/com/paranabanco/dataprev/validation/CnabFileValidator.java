package br.com.paranabanco.dataprev.validation;

import br.com.paranabanco.dataprev.enumeration.TipoRotulo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            
            // 2. Validação de tamanho de registro
            ValidationResult resultadoTamanho = validarTamanhoRegistros(arquivo);
            if (!resultadoTamanho.isValido()) {
                return resultadoTamanho;
            }
            
            // 3. Validação de fator de bloco
            ValidationResult resultadoFator = validarFatorBloco(arquivo);
            if (!resultadoFator.isValido()) {
                return resultadoFator;
            }
            
            // 4. Validação de estrutura
            ValidationResult resultadoEstrutura = validarEstrutura(arquivo);
            if (!resultadoEstrutura.isValido()) {
                return resultadoEstrutura;
            }
            
            // 5. Validação de compatibilidade de rótulo
            ValidationResult resultadoRotulo = validarCompatibilidadeRotulo(arquivo);
            if (!resultadoRotulo.isValido()) {
                return resultadoRotulo;
            }
            
            log.info("Arquivo validado com sucesso: {}", arquivo.getFileName());
            return ValidationResult.sucesso("Arquivo validado com sucesso");
            
        } catch (Exception e) {
            log.error("Erro durante validação do arquivo: {}", arquivo.getFileName(), e);
            return ValidationResult.erro("Erro interno durante validação: " + e.getMessage());
        }
    }

    /**
     * Validação básica do arquivo
     */
    private ValidationResult validarArquivoBasico(Path arquivo) {
        if (arquivo == null) {
            return ValidationResult.erro("Arquivo não pode ser nulo");
        }
        
        if (!Files.exists(arquivo)) {
            return ValidationResult.erro("Arquivo não existe: " + arquivo);
        }
        
        if (!Files.isRegularFile(arquivo)) {
            return ValidationResult.erro("Caminho não é um arquivo: " + arquivo);
        }
        
        try {
            long tamanho = Files.size(arquivo);
            if (tamanho == 0) {
                return ValidationResult.erro("Arquivo está vazio");
            }
            
            log.debug("Arquivo básico validado: {} (tamanho: {} bytes)", arquivo.getFileName(), tamanho);
            return ValidationResult.sucesso("Arquivo básico válido");
            
        } catch (IOException e) {
            return ValidationResult.erro("Erro ao acessar arquivo: " + e.getMessage());
        }
    }

    /**
     * Valida se todos os registros têm 480 bytes
     */
    private ValidationResult validarTamanhoRegistros(Path arquivo) {
        try {
            List<String> linhas = Files.readAllLines(arquivo);
            int linhaInvalida = -1;
            
            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i);
                if (linha != null && !linha.trim().isEmpty()) {
                    if (linha.length() != TAMANHO_REGISTRO_ESPERADO) {
                        linhaInvalida = i + 1;
                        break;
                    }
                }
            }
            
            if (linhaInvalida != -1) {
                return ValidationResult.erro(
                    String.format("Registro na linha %d não tem %d bytes. Tamanho encontrado: %d bytes", 
                        linhaInvalida, TAMANHO_REGISTRO_ESPERADO, 
                        linhas.get(linhaInvalida - 1).length())
                );
            }
            
            log.debug("Validação de tamanho de registro: OK ({} registros de {} bytes)", 
                linhas.size(), TAMANHO_REGISTRO_ESPERADO);
            return ValidationResult.sucesso("Todos os registros têm 480 bytes");
            
        } catch (IOException e) {
            return ValidationResult.erro("Erro ao ler arquivo para validação de tamanho: " + e.getMessage());
        }
    }

    /**
     * Valida fator de bloco (u7)
     */
    private ValidationResult validarFatorBloco(Path arquivo) {
        try {
            List<String> linhas = Files.readAllLines(arquivo);
            
            // Verifica se o número de registros é múltiplo de 39
            long registrosValidos = linhas.stream()
                .filter(linha -> linha != null && !linha.trim().isEmpty())
                .count();
            
            if (registrosValidos % FATOR_BLOCO_ESPERADO != 0) {
                return ValidationResult.erro(
                    String.format("Fator de bloco inválido. Esperado múltiplo de %d, encontrado %d registros", 
                        FATOR_BLOCO_ESPERADO, registrosValidos)
                );
            }
            
            log.debug("Validação de fator de bloco: OK ({} registros, múltiplo de {})", 
                registrosValidos, FATOR_BLOCO_ESPERADO);
            return ValidationResult.sucesso("Fator de bloco válido (u39)");
            
        } catch (IOException e) {
            return ValidationResult.erro("Erro ao validar fator de bloco: " + e.getMessage());
        }
    }

    /**
     * Valida estrutura: HEADER → DETALHE(s) → TRAILER
     */
    private ValidationResult validarEstrutura(Path arquivo) {
        try {
            List<String> linhas = Files.readAllLines(arquivo);
            boolean headerArquivoEncontrado = false;
            boolean trailerArquivoEncontrado = false;
            String loteAtual = null;
            Set<String> lotes = new HashSet<>();
            Set<String> lotesComDetalhe = new HashSet<>();
            Set<String> lotesComTrailer = new HashSet<>();

            for (int i = 0; i < linhas.size(); i++) {
                String linha = linhas.get(i);
                if (linha == null || linha.trim().isEmpty()) {
                    continue;
                }

                // Verifica se tem pelo menos 8 caracteres para ler o tipo de registro
                if (linha.length() < 8) {
                    return ValidationResult.erro(
                        String.format("Linha %d muito curta para identificar tipo de registro", i + 1)
                    );
                }

                String tipoRegistro = linha.substring(7, 8);
                String numeroLote = linha.substring(3, 7);

                // Validação de estrutura sequencial
                if ("0".equals(tipoRegistro) && "0000".equals(numeroLote)) {
                    if (headerArquivoEncontrado) {
                        return ValidationResult.erro("Múltiplos headers de arquivo encontrados");
                    }
                    headerArquivoEncontrado = true;
                    log.debug("Header de arquivo encontrado na linha {}", i + 1);

                } else if ("1".equals(tipoRegistro)) {
                    if (!headerArquivoEncontrado) {
                        return ValidationResult.erro("Header de lote encontrado antes do header de arquivo");
                    }
                    if (loteAtual != null) {
                        return ValidationResult.erro("Novo header de lote encontrado antes do trailer do lote anterior");
                    }
                    loteAtual = numeroLote;
                    if (lotes.contains(numeroLote)) {
                        return ValidationResult.erro("Múltiplos headers para o lote " + numeroLote);
                    }
                    lotes.add(numeroLote);
                    log.debug("Header de lote {} encontrado na linha {}", numeroLote, i + 1);

                } else if ("3".equals(tipoRegistro)) {
                    if (loteAtual == null || !numeroLote.equals(loteAtual)) {
                        return ValidationResult.erro("Detalhe encontrado fora do lote atual");
                    }
                    lotesComDetalhe.add(numeroLote);
                    log.debug("Registro de detalhe do lote {} encontrado na linha {}", numeroLote, i + 1);

                } else if ("5".equals(tipoRegistro)) {
                    if (loteAtual == null || !numeroLote.equals(loteAtual)) {
                        return ValidationResult.erro("Trailer de lote encontrado fora de sequência");
                    }
                    if (!lotesComDetalhe.contains(numeroLote)) {
                        return ValidationResult.erro("Trailer de lote encontrado sem registros de detalhe");
                    }
                    if (lotesComTrailer.contains(numeroLote)) {
                        return ValidationResult.erro("Múltiplos trailers para o lote " + numeroLote);
                    }
                    lotesComTrailer.add(numeroLote);
                    loteAtual = null;
                    log.debug("Trailer de lote {} encontrado na linha {}", numeroLote, i + 1);

                } else if ("9".equals(tipoRegistro) && "0000".equals(numeroLote)) {
                    if (loteAtual != null) {
                        return ValidationResult.erro("Trailer de arquivo encontrado antes do trailer do lote");
                    }
                    if (trailerArquivoEncontrado) {
                        return ValidationResult.erro("Múltiplos trailers de arquivo encontrados");
                    }
                    trailerArquivoEncontrado = true;
                    log.debug("Trailer de arquivo encontrado na linha {}", i + 1);
                }
            }

            // Validações finais
            if (!headerArquivoEncontrado) {
                return ValidationResult.erro("Header de arquivo não encontrado");
            }
            if (!trailerArquivoEncontrado) {
                return ValidationResult.erro("Trailer de arquivo não encontrado");
            }
            if (loteAtual != null) {
                return ValidationResult.erro("Trailer de lote não encontrado para o lote " + loteAtual);
            }
            for (String lote : lotes) {
                if (!lotesComDetalhe.contains(lote)) {
                    return ValidationResult.erro("Nenhum registro de detalhe encontrado para o lote " + lote);
                }
                if (!lotesComTrailer.contains(lote)) {
                    return ValidationResult.erro("Trailer de lote não encontrado para o lote " + lote);
                }
            }
            boolean lote20 = lotes.contains("0020");
            boolean lote21 = lotes.contains("0021");
            if (lote20 ^ lote21) {
                return ValidationResult.erro("Lotes 20 e 21 devem estar presentes em pares");
            }

            log.debug("Validação de estrutura: OK (HEADER → DETALHE → TRAILER)");
            return ValidationResult.sucesso("Estrutura CNAB válida");

        } catch (IOException e) {
            return ValidationResult.erro("Erro ao validar estrutura do arquivo: " + e.getMessage());
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
            return ValidationResult.erro("Não foi possível extrair rótulo do nome do arquivo: " + nomeArquivo);
        }
        
        // Verifica se o rótulo é suportado
        if (!TipoRotulo.isRotuloSuportado(rotulo)) {
            return ValidationResult.erro("Rótulo não suportado: " + rotulo);
        }
        
        TipoRotulo tipoRotulo = TipoRotulo.fromRotulo(rotulo);
        log.debug("Rótulo validado: {} ({})", rotulo, tipoRotulo.getDescricao());
        
        return ValidationResult.sucesso("Rótulo compatível: " + rotulo);
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
