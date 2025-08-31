package br.com.paranabanco.dataprev.cnab;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "app.cnab")
public class AppCnabProperties {
    private Path outputDir = Paths.get("src/main/resources/generated");
    private String outputName = "FSUBCON10.D0000001.d";
    private Path retornoDir = Paths.get("src/main/resources/generated/mock-retorno");
    private Charset charset = StandardCharsets.ISO_8859_1;
    private Resource inputFile;

    public Path getOutputDir() { return outputDir; }
    public void setOutputDir(String dir) { this.outputDir = Paths.get(dir).toAbsolutePath().normalize(); }

    public String getOutputName() { return outputName; }
    public void setOutputName(String n) { this.outputName = n; }

    public Path getRetornoDir() { return retornoDir; }
    public void setRetornoDir(String dir) { this.retornoDir = Paths.get(dir).toAbsolutePath().normalize(); }

    public Charset getCharset() { return charset; }
    public void setCharset(String cs) { this.charset = Charset.forName(cs); }

    public Resource getInputFile() { return inputFile; }
    public void setInputFile(Resource inputFile) { this.inputFile = inputFile; }
}
