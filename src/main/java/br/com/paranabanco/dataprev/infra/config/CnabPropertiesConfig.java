package br.com.paranabanco.dataprev.infra.config;

import br.com.paranabanco.dataprev.cnab.AppCnabProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;

@Configuration
public class CnabPropertiesConfig {

    @Value("${app.cnab.file}")
    private Resource inputFile;

    private final AppCnabProperties appCnabProperties;

    public CnabPropertiesConfig(AppCnabProperties appCnabProperties) {
        this.appCnabProperties = appCnabProperties;
    }

    @PostConstruct
    public void configureInputFile() {
        appCnabProperties.setInputFile(inputFile);
    }
}
