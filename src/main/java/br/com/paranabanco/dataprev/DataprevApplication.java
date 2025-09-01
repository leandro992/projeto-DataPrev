package br.com.paranabanco.dataprev;

import br.com.paranabanco.dataprev.cnab.AppCnabProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppCnabProperties.class)
public class DataprevApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataprevApplication.class, args);
	}

}
