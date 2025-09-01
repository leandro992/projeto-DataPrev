package br.com.paranabanco.dataprev.connect;

import br.com.paranabanco.dataprev.enumeration.TipoRotulo;
import br.com.paranabanco.dataprev.utils.ConnectException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;

@Component
public class ConnectClient {

    private static final Logger log = LoggerFactory.getLogger(ConnectClient.class);
    // Rótulos permitidos - agora usando o enum
    private static final Set<String> ALLOWED_PREFIXES = Set.of(
        TipoRotulo.CONCESSAO.getRotulo(),
        TipoRotulo.MACICA.getRotulo(),
        TipoRotulo.ESPECIAL.getRotulo()
    );

    private final String host;
    private final String username;
    private final String password;
    private final String remoteDir;
    private final int port;
    private final int timeoutMs;

    public ConnectClient(@Value("${connect.host}") String host,
                         @Value("${connect.username}") String username,
                         @Value("${connect.password}") String password,
                         @Value("${connect.remote-dir}") String remoteDir,
                         @Value("${connect.port:22}") int port,
                         @Value("${connect.timeout-ms:30000}") int timeoutMs) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.remoteDir = remoteDir;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }

    private ChannelSftp open() throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(username, host, port);
        session.setPassword(password);
        Properties config  = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect(timeoutMs);
        Channel channel = session.openChannel("sftp");
        channel.connect();
        return (ChannelSftp) channel;
    }

    public List<String> listarArquivos(String rotulo) {
        return withSftp(sftp -> {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entries = sftp.ls(".");
            List<String> arquivos = entries.stream()
                    .map(ChannelSftp.LsEntry::getFilename)
                    .filter(nome -> !".".equals(nome) && !"..".equals(nome))
                    .filter(ConnectClient::startsWithAllowed)
                    .filter(nome -> matchesRotulo(nome, rotulo))
                    .collect(Collectors.toList());
            log.info("Arquivos encontrados ({}): {}", rotulo, arquivos);
            return arquivos;
        });
    }

    @FunctionalInterface
    private interface SftpFunction<T> {
        T apply(ChannelSftp sftp) throws Exception;
    }

    private <T> T withSftp(SftpFunction<T> function) {
        ChannelSftp sftp = null;
        try {
            log.info("Conectando ao servidor Connect em {}:{}", host, port);
            sftp = open();
            if (remoteDir != null && !remoteDir.isBlank()) {
                sftp.cd(remoteDir);
            }
            return function.apply(sftp);
        } catch (Exception e) {
            log.error("Erro na operação SFTP", e);
            throw new ConnectException("Erro na operação SFTP", e);
        } finally {
            if (sftp != null) {
                try { sftp.disconnect(); } catch (Exception ignored) {}
                try { sftp.getSession().disconnect(); } catch (Exception ignored) {}
            }
        }
    }

    public Optional<Path> findAndDownload(String rotulo, Integer n, Path destinoDir) {
        return withSftp(sftp -> {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entries = sftp.ls(".");
            List<String> arquivos = entries.stream()
                    .map(ChannelSftp.LsEntry::getFilename)
                    .filter(nome -> !".".equals(nome) && !"..".equals(nome))
                    .filter(ConnectClient::startsWithAllowed)
                    .filter(nome -> matchesRotulo(nome, rotulo))
                    .collect(Collectors.toList());

            if (arquivos.isEmpty()) {
                log.info("Nenhum arquivo listado no Connect para o rótulo {}", rotulo);
                return Optional.empty();
            }

            String escolhido = selecionarArquivo(arquivos, n);
            if (escolhido == null) {
                log.info("Nenhum arquivo compatível encontrado para {} com n={}", rotulo, n);
                return Optional.empty();
            }

            Path destino = destinoDir.resolve(escolhido);
            try (InputStream in = sftp.get(escolhido)) {
                Files.createDirectories(destino.getParent());
                Files.copy(in, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Download concluído: {}", destino);
            return Optional.of(destino);
        });
    }

    /**
     * Busca e baixa arquivo específico por tipo de rótulo
     */
    public Optional<Path> buscarArquivoPorTipo(TipoRotulo tipoRotulo, Path destinoDir) {
        log.info("Buscando arquivo para tipo: {} (rótulo: {})", tipoRotulo.getDescricao(), tipoRotulo.getRotulo());
        return findAndDownload(tipoRotulo.getRotulo(), tipoRotulo.getValorN(), destinoDir);
    }

    /**
     * Lista arquivos disponíveis para um tipo de rótulo específico
     */
    public List<String> listarArquivosPorTipo(TipoRotulo tipoRotulo) {
        log.info("Listando arquivos para tipo: {} (rótulo: {})", tipoRotulo.getDescricao(), tipoRotulo.getRotulo());
        return listarArquivos(tipoRotulo.getRotulo());
    }

    /**
     * Calcula hash SHA-256 do arquivo baixado
     */
    public String calcularHashArquivo(Path arquivo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(arquivo));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.warn("Erro ao calcular hash do arquivo {}: {}", arquivo, e.getMessage());
            return null;
        }
    }

    private static String selecionarArquivo(List<String> nomes, Integer n) {
        Objects.requireNonNull(nomes, "lista de nomes não pode ser nula");
        String alvoD = (n != null) ? String.format("D%07d", n) : null;

        if (alvoD != null) {
            return nomes.stream()
                    .filter(s -> s.contains(alvoD))
                    .sorted(Comparator.<String>comparingInt(s -> s.endsWith(".d") ? 0 : 1)
                            .thenComparing(Comparator.reverseOrder()))
                    .findFirst()
                    .orElse(null);
        }

        return nomes.stream()
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .orElse(null);
    }

    private static String stripKnownWrappers(String filename) {
        if (filename == null) return null;
        String name = filename;
        if (name.startsWith("RET_")) name = name.substring(4);
        if (name.startsWith("RECIBO_")) name = name.substring(7);
        String lower = name.toLowerCase();
        if (lower.endsWith(".d")) {
            name = name.substring(0, name.length() - 2); // remove sufixo .d
        }
        return name;
    }

    private static boolean startsWithAllowed(String filename) {
        String base = stripKnownWrappers(filename);
        return ALLOWED_PREFIXES.stream().anyMatch(base::startsWith);
    }

    private static boolean matchesRotulo(String filename, String rotulo) {
        if (rotulo == null || rotulo.isBlank()) return true;
        // Normaliza rótulo (somente os 3 aceitos)
        String r = rotulo.trim().toUpperCase();
        String base = stripKnownWrappers(filename);
        return ALLOWED_PREFIXES.contains(r) && base.startsWith(r);
    }

    public void baixarArquivo(String nomeRemoto, Path destino) {
        withSftp(sftp -> {
            log.info("Baixando arquivo {} para {}", nomeRemoto, destino);
            try (InputStream in = sftp.get(nomeRemoto)) {
                Files.createDirectories(destino.getParent());
                Files.copy(in, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Download concluído: {}", destino);
            return null;
        });
    }

}
