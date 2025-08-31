package br.com.paranabanco.dataprev.infra.config.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Inicializa o database/login/usuário em DEV antes do DataSource e do Flyway.
 * Registrado via META-INF/spring.factories.
 */
@Profile("dev")
@ConditionalOnProperty(value="app.devdb.enabled", havingValue="true", matchIfMissing=false)
public class DevDatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(DevDatabaseInitializer.class);

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();

        if (!isDev(env)) {
            return;
        }
        boolean enabled = env.getProperty("app.dev.bootstrap.enabled", Boolean.class, false);
        if (!enabled) {
            return;
        }

        String adminUrl = env.getRequiredProperty("app.dev.bootstrap.admin.url");
        String adminUser = env.getRequiredProperty("app.dev.bootstrap.admin.username");
        String adminPass = env.getRequiredProperty("app.dev.bootstrap.admin.password");
        String databaseName = env.getRequiredProperty("app.dev.bootstrap.database-name");
        String appUser = env.getRequiredProperty("spring.datasource.username");
        String appPass = env.getRequiredProperty("spring.datasource.password");

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            log.warn("Driver SQLServer não encontrado ainda no classpath. Prosseguindo, pois Gradle carregará depois.");
        }

        log.info("[DEV INIT] Preparando database '{}' e usuário '{}'...", databaseName, appUser);

        try (Connection conn = DriverManager.getConnection(adminUrl, adminUser, adminPass);
             Statement st = conn.createStatement()) {

            String createDb = "IF DB_ID('" + databaseName + "') IS NULL BEGIN CREATE DATABASE " + databaseName + "; END";
            st.execute(createDb);

            String createLogin = "IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = '" + appUser + "') " +
                    "BEGIN CREATE LOGIN " + appUser + " WITH PASSWORD = '" + appPass + "', CHECK_POLICY = OFF; END";
            st.execute(createLogin);

            String createUserAndRole = String.join(" ", Arrays.asList(
                    "IF DB_ID('" + databaseName + "') IS NOT NULL BEGIN",
                    "USE " + databaseName + ";",
                    "IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = '" + appUser + "')",
                    "BEGIN CREATE USER " + appUser + " FOR LOGIN " + appUser + "; END",
                    "IF NOT EXISTS (",
                    "  SELECT 1 FROM sys.database_role_members rm",
                    "  JOIN sys.database_principals r ON r.principal_id = rm.role_principal_id AND r.name = 'db_owner'",
                    "  JOIN sys.database_principals u ON u.principal_id = rm.member_principal_id AND u.name = '" + appUser + "'",
                    ") BEGIN ALTER ROLE db_owner ADD MEMBER " + appUser + "; END",
                    "END"
            ));
            st.execute(createUserAndRole);

            log.info("[DEV INIT] Database '{}' pronto e usuário '{}' provisionado.", databaseName, appUser);
        } catch (Exception e) {
            log.warn("[DEV INIT] Falha ao preparar database DEV: {}", e.getMessage());
        }
    }

    private boolean isDev(Environment env) {
        return Arrays.asList(env.getActiveProfiles()).contains("dev");
    }
}


