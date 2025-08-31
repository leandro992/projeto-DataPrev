package br.com.paranabanco.dataprev.infra.config.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Em ambiente DEV, garante de forma idempotente:
 * - criação do database (se não existir)
 * - criação do login servidor (se não existir)
 * - criação do usuário mapeado no DB e associação a db_owner (se não existir)
 *
 * Observação: usa conexão administrativa no banco "master".
 */
@Configuration
@ConditionalOnProperty(prefix = "app.dev.bootstrap", name = "enabled", havingValue = "true", matchIfMissing = false)
@Order(0)
public class DevDatabaseBootstrap {

    private static final Logger log = LoggerFactory.getLogger(DevDatabaseBootstrap.class);

    @Value("${app.dev.bootstrap.admin.url}")
    private String adminUrl;

    @Value("${app.dev.bootstrap.admin.username}")
    private String adminUsername;

    @Value("${app.dev.bootstrap.admin.password}")
    private String adminPassword;

    @Value("${spring.datasource.username}")
    private String appUser;

    @Value("${spring.datasource.password}")
    private String appPassword;

    @Value("${app.dev.bootstrap.database-name}")
    private String databaseName;

    @Value("${app.dev.bootstrap.reset-users:false}")
    private boolean resetUsers;

    @PostConstruct
    public void bootstrap() {
        log.info("[DEV BOOTSTRAP] Iniciando verificação/criação de database e usuários...");
        DataSource adminDs = buildAdminDataSource();
        JdbcTemplate jdbc = new JdbcTemplate(adminDs);

        // 1) Cria o database se não existir
        String createDbSql = "IF DB_ID('" + databaseName + "') IS NULL BEGIN CREATE DATABASE " + databaseName + "; END";
        jdbc.execute(createDbSql);

        // 2) (Opcional) Limpa usuários/logins existentes do DB para ambiente de desenvolvimento
        if (resetUsers) {
            log.warn("[DEV BOOTSTRAP] reset-users=true: removendo usuários/logins do database '{}' (exceto internos)", databaseName);
            String resetSql = String.join(" ",
                    "USE " + databaseName + ";",
                    "DECLARE @user sysname, @sql nvarchar(max);",
                    "DECLARE cur CURSOR FOR",
                    "  SELECT name FROM sys.database_principals",
                    "  WHERE type IN ('S','U','G') AND principal_id > 4",
                    "    AND name NOT IN ('dbo','guest','INFORMATION_SCHEMA','sys');",
                    "OPEN cur; FETCH NEXT FROM cur INTO @user;",
                    "WHILE @@FETCH_STATUS = 0 BEGIN",
                    "  BEGIN TRY",
                    "    DECLARE @role nvarchar(128);",
                    "    DECLARE rolecur CURSOR FOR",
                    "      SELECT r.name FROM sys.database_role_members rm",
                    "      JOIN sys.database_principals r ON r.principal_id = rm.role_principal_id",
                    "      JOIN sys.database_principals u ON u.principal_id = rm.member_principal_id",
                    "      WHERE u.name = @user;",
                    "    OPEN rolecur; FETCH NEXT FROM rolecur INTO @role;",
                    "    WHILE @@FETCH_STATUS = 0 BEGIN",
                    "      SET @sql = N'ALTER ROLE ' + QUOTENAME(@role) + N' DROP MEMBER ' + QUOTENAME(@user) + N';';",
                    "      EXEC sp_executesql @sql;",
                    "      FETCH NEXT FROM rolecur INTO @role;",
                    "    END",
                    "    CLOSE rolecur; DEALLOCATE rolecur;",
                    "  END TRY BEGIN CATCH END CATCH;",
                    "  BEGIN TRY",
                    "    SET @sql = N'DROP USER ' + QUOTENAME(@user) + N';';",
                    "    EXEC sp_executesql @sql;",
                    "  END TRY BEGIN CATCH END CATCH;",
                    "  BEGIN TRY",
                    "    IF EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = @user) AND @user <> 'sa' BEGIN",
                    "      SET @sql = N'DROP LOGIN ' + QUOTENAME(@user) + N';';",
                    "      EXEC (@sql);",
                    "    END",
                    "  END TRY BEGIN CATCH END CATCH;",
                    "  FETCH NEXT FROM cur INTO @user;",
                    "END",
                    "CLOSE cur; DEALLOCATE cur;"
            );
            jdbc.execute(resetSql);
        }

        // 3) Cria o LOGIN no servidor (se não existir)
        String createLoginSql = "IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = '" + appUser + "') " +
                "BEGIN CREATE LOGIN " + appUser + " WITH PASSWORD = '" + appPassword + "', CHECK_POLICY = OFF; END";
        jdbc.execute(createLoginSql);

        // 4) Troca de contexto para o novo DB e cria USER + adiciona ao db_owner
        String createUserAndRole =
                "IF DB_ID('" + databaseName + "') IS NOT NULL BEGIN " +
                "  USE " + databaseName + "; " +
                "  IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = '" + appUser + "') BEGIN " +
                "    CREATE USER " + appUser + " FOR LOGIN " + appUser + "; " +
                "  END " +
                "  IF NOT EXISTS (" +
                "    SELECT 1 FROM sys.database_role_members rm " +
                "    JOIN sys.database_principals r ON r.principal_id = rm.role_principal_id AND r.name = 'db_owner' " +
                "    JOIN sys.database_principals u ON u.principal_id = rm.member_principal_id AND u.name = '" + appUser + "' " +
                "  ) BEGIN " +
                "    ALTER ROLE db_owner ADD MEMBER " + appUser + "; " +
                "  END " +
                "END";

        jdbc.execute(createUserAndRole);

        log.info("[DEV BOOTSTRAP] Verificação/criação concluída. Database: {} | Login/User: {}", databaseName, appUser);
    }

    private DataSource buildAdminDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        ds.setUrl(adminUrl);
        ds.setUsername(adminUsername);
        ds.setPassword(adminPassword);
        return ds;
    }
}
