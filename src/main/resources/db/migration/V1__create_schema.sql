/* ============================================================
   Flyway V1__create_schema.sql  (SQL Server)
   - Sem "GO" (Flyway não precisa)
   - Idempotente (protege contra reaplicações)
   ============================================================ */

SET XACT_ABORT ON;
BEGIN TRAN;

-- Pessoa
IF OBJECT_ID('dbo.pessoa', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.pessoa
    (
        id               BIGINT            IDENTITY(1,1) NOT NULL,
        cpf              VARCHAR(11)       NOT NULL,
        nome             VARCHAR(120)      NOT NULL,
        data_nascimento  DATE              NULL,
        falecido         BIT               NOT NULL CONSTRAINT DF_pessoa_falecido DEFAULT ((0)),
        dt_atualizacao   DATETIME2(3)      NOT NULL CONSTRAINT DF_pessoa_dt_atualizacao DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT PK_pessoa PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_pessoa_cpf UNIQUE (cpf),
        -- (Opcional) Validar tamanho do CPF = 11
        CONSTRAINT CK_pessoa_cpf_len CHECK (LEN(cpf) = 11)
        -- (Opcional) Validar somente dígitos: 0-9
        -- ,CONSTRAINT CK_pessoa_cpf_digits CHECK (cpf NOT LIKE '%[^0-9]%')
    );

    -- Índice auxiliar para buscas por cpf (além do UNIQUE)
    CREATE NONCLUSTERED INDEX IX_pessoa_cpf ON dbo.pessoa (cpf);
END;
ELSE
BEGIN
    -- Se a tabela já existir, garanta que constraints essenciais existam (caso tenha sido criada diferente).
    IF NOT EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'PK_pessoa')
        ALTER TABLE dbo.pessoa ADD CONSTRAINT PK_pessoa PRIMARY KEY CLUSTERED (id);

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_pessoa_cpf' AND object_id = OBJECT_ID('dbo.pessoa'))
        ALTER TABLE dbo.pessoa ADD CONSTRAINT UQ_pessoa_cpf UNIQUE (cpf);

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_pessoa_cpf_len')
        ALTER TABLE dbo.pessoa ADD CONSTRAINT CK_pessoa_cpf_len CHECK (LEN(cpf) = 11);

    IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_pessoa_falecido')
        ALTER TABLE dbo.pessoa ADD CONSTRAINT DF_pessoa_falecido DEFAULT ((0)) FOR falecido;

    IF NOT EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_pessoa_dt_atualizacao')
        ALTER TABLE dbo.pessoa ADD CONSTRAINT DF_pessoa_dt_atualizacao DEFAULT (SYSUTCDATETIME()) FOR dt_atualizacao;

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_pessoa_cpf' AND object_id = OBJECT_ID('dbo.pessoa'))
        CREATE NONCLUSTERED INDEX IX_pessoa_cpf ON dbo.pessoa (cpf);
END;

COMMIT TRAN;
