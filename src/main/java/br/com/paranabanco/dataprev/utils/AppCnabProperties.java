package br.com.paranabanco.dataprev.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "app.cnab")
public class AppCnabProperties {
    private Resource file;
    private Charset charset = StandardCharsets.US_ASCII; // default seguro
    private String outputDir;
    private String outputName;

    public Resource getFile() { return file; }
    public void setFile(Resource file) { this.file = file; }

    public Charset getCharset() { return charset; }
    public void setCharset(Charset charset) { this.charset = charset; }

    public String getOutputDir() { return outputDir; }
    public void setOutputDir(String outputDir) { this.outputDir = outputDir; }

    public String getOutputName() { return outputName; }
    public void setOutputName(String outputName) { this.outputName = outputName; }
}
