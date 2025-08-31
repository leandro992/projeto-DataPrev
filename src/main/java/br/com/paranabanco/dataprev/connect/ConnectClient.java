package br.com.paranabanco.dataprev.connect;

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
import java.util.List;
import java.util.Set;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;

@Component
public class ConnectClient {

    private static final Logger log = LoggerFactory.getLogger(ConnectClient.class);
    // Rótulos permitidos
    private static final Set<String> ALLOWED_PREFIXES = Set.of("FHMLCON16", "FHMLMAC16", "FHMLES18");

    private final String host;
    private final String username;
    private final String password;
    private final String remoteDir;
    private final int port;

    public ConnectClient(@Value("${connect.host}") String host,
                         @Value("${connect.username}") String username,
                         @Value("${connect.password}") String password,
                         @Value("${connect.remote-dir}") String remoteDir,
                         @Value("${connect.port:22}") int port) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.remoteDir = remoteDir;
        this.port = port;
    }

    private ChannelSftp open() throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(username, host, port);
        session.setPassword(password);
        Properties config  = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        return (ChannelSftp) channel;
    }


    public List<String> listarArquivos(String rotulo) {
        ChannelSftp sftp = null;
        try {
            log.info("Conectando ao servidor Connect em {}:{}", host, port);
            sftp = open();
            // Entrar no diretório de retorno (relativo ao HOME do usuário)
            if (remoteDir != null && !remoteDir.isBlank()) {
                sftp.cd(remoteDir);
            }
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entries = sftp.ls(".");
            List<String> arquivos = entries.stream()
                    .map(ChannelSftp.LsEntry::getFilename)
                    .filter(nome -> !".".equals(nome) && !"..".equals(nome))
                    // considera somente arquivos com rótulos permitidos
                    .filter(ConnectClient::startsWithAllowed)
                    // se um rótulo foi informado, filtra pelas variações aceitas
                    .filter(nome -> matchesRotulo(nome, rotulo))
                    .collect(Collectors.toList());
            log.info("Arquivos encontrados ({}): {}", rotulo, arquivos);
            return arquivos;
        } catch (Exception e) {
            log.error("Erro ao listar arquivos no Connect", e);
            return List.of();
        } finally {
            if (sftp != null) {
                try { sftp.disconnect(); } catch (Exception ignored) {}
                try { sftp.getSession().disconnect(); } catch (Exception ignored) {}
            }
        }
    }

    private static boolean startsWithAllowed(String filename) {
        return ALLOWED_PREFIXES.stream().anyMatch(filename::startsWith);
    }

    private static boolean matchesRotulo(String filename, String rotulo) {
        if (rotulo == null || rotulo.isBlank()) return true;
        // Normaliza rótulo (somente os 3 aceitos)
        String r = rotulo.trim().toUpperCase();
        return ALLOWED_PREFIXES.contains(r) && filename.startsWith(r);
    }

    public void baixarArquivo(String nomeRemoto, Path destino) {
        ChannelSftp sftp = null;
        try {
            log.info("Baixando arquivo {} para {}", nomeRemoto, destino);
            sftp = open();
            if (remoteDir != null && !remoteDir.isBlank()) {
                sftp.cd(remoteDir);
            }
            try (InputStream in = sftp.get(nomeRemoto)) {
                Files.createDirectories(destino.getParent());
                Files.copy(in, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Download concluído: {}", destino);
        } catch (Exception e) {
            log.error("Erro ao baixar arquivo {}", nomeRemoto, e);
            throw new RuntimeException("Falha no download do arquivo " + nomeRemoto, e);
        } finally {
            if (sftp != null) {
                try { sftp.disconnect(); } catch (Exception ignored) {}
                try { sftp.getSession().disconnect(); } catch (Exception ignored) {}
            }
        }
    }

}
