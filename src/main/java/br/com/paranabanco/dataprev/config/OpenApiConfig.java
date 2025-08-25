package br.com.paranabanco.dataprev.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Paraná Banco - Dataprev API")
                        .version("v1")
                        .description("APIs de concessão, remessa e retorno conforme protocolo Dataprev")
                        .contact(new Contact().name("Equipe Backend").email("")));
    }

    // Grupo para rotas de Crédito (ex.: /api/creditos/**)
    @Bean
    GroupedOpenApi creditosGroup() {
        return GroupedOpenApi.builder()
                .group("creditos")
                .pathsToMatch("/api/creditos/**")
                .build();
    }
}
